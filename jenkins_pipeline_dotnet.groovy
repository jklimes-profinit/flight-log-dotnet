#!/usr/bin/env groovy
pipeline {
    agent any
    parameters {
        booleanParam defaultValue: false, description: 'Whether to skip tests.', name: 'SKIP_TESTS'
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '5'))
    }
    triggers {
        pollSCM 'H/3 * * * *'
    }
    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                git 'https://github.com/jklimes-profinit/flight-log-dotnet.git'
            }
        }
        stage('Build') {
            steps {
                dir('.') {
                    sh "dotnet build"
                }
            }
        }
        stage('Test') {
            when {
                not {
                    expression { params.SKIP_TEST }
                }
            }
            steps {
                warnError('Tests failed') {
                    script {
                        sh 'dotnet test --filter DisplayName~FlightLogNet.Tests.Operation --logger "trx" '
                    }
                }
                step([$class: 'MSTestPublisher', testResultsFile: "**/*.trx", failOnError: true, keepLongStdio: true])
            }
        }
    }
}

