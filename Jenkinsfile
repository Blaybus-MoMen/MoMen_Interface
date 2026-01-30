// ============================================================
// Momen - Jenkins Pipeline (Spring Boot, Gradle, Docker)
// ============================================================

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '30'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        JAVA_VERSION = '21'
        APP_NAME = 'momen'
    }

    parameters {
        choice(
            name: 'BRANCH',
            choices: ['develop', 'main', 'master'],
            description: '빌드할 브랜치'
        )
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'prod'],
            description: '배포 환경'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: '테스트 건너뛰기 (비권장)'
        )
        booleanParam(
            name: 'DOCKER_BUILD',
            defaultValue: true,
            description: 'Docker 이미지 빌드'
        )
        string(
            name: 'DOCKER_REGISTRY',
            defaultValue: '',
            description: 'Docker 레지스트리 (예: registry.example.com 또는 빈 값 시 로컬만 빌드)'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    if (params.BRANCH) {
                        sh "git checkout ${params.BRANCH} || true"
                        sh "git pull origin ${params.BRANCH} || true"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew clean build --no-daemon -x test
                '''
            }
        }

        stage('Test') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                sh './gradlew test --no-daemon'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
                }
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                sh './gradlew dependencyCheckAnalyze --no-daemon'
                publishHTML target: [
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'build/reports/dependency-check',
                    reportFiles: 'dependency-check-report.html',
                    reportName: 'OWASP Dependency Check Report'
                ]
            }
        }

        stage('Package') {
            steps {
                sh './gradlew bootJar --no-daemon'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            when {
                expression { params.DOCKER_BUILD }
            }
            steps {
                script {
                    def tag = "${params.ENVIRONMENT}-${env.BUILD_NUMBER}"
                    def imageName = "${params.DOCKER_REGISTRY ? params.DOCKER_REGISTRY + '/' : ''}${APP_NAME}:${tag}"
                    env.DOCKER_IMAGE_TAG = tag
                    env.DOCKER_IMAGE_FULL = imageName
                }
                sh '''
                    docker build -t ${DOCKER_IMAGE_FULL} .
                    docker tag ${DOCKER_IMAGE_FULL} ${APP_NAME}:latest
                '''
            }
        }

        stage('Docker Push') {
            when {
                expression { params.DOCKER_BUILD && params.DOCKER_REGISTRY?.trim() }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-registry-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin ${DOCKER_REGISTRY}
                        docker push ${DOCKER_IMAGE_FULL}
                        docker push ${APP_NAME}:latest
                    '''
                }
            }
        }

        stage('Deploy') {
            when {
                expression { params.ENVIRONMENT in ['dev', 'staging', 'prod'] }
            }
            steps {
                echo "배포 환경: ${params.ENVIRONMENT}"
                // 실제 배포는 환경별 스크립트/플러그인으로 확장
                // 예: sshPublisher, kubectl, ansible 등
                script {
                    if (params.ENVIRONMENT == 'dev') {
                        echo 'Dev 배포: JAR 아카이브 완료. 필요 시 여기서 서버 배포 스크립트 호출.'
                    } else if (params.ENVIRONMENT == 'staging') {
                        echo 'Staging 배포: Docker 이미지 푸시 완료. 필요 시 kubectl/helm 또는 서버 배포.'
                    } else if (params.ENVIRONMENT == 'prod') {
                        echo 'Prod 배포: 수동 승인 또는 별도 파이프라인에서 처리 권장.'
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs(deleteDirs: true)
        }
        success {
            echo "빌드 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
        failure {
            echo "빌드 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
    }
}
