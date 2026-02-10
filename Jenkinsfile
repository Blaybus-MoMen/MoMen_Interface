// ============================================================
// Momen - Jenkins Pipeline
// Jenkins(Docker) + 같은 서버에서 Docker Compose 배포
// ============================================================

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 20, unit: 'MINUTES')
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        APP_NAME = 'momen'
        APP_PORT = '8089'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build \
                        -t ${APP_NAME}:build-${env.BUILD_NUMBER} \
                        -t ${APP_NAME}:latest \
                        .
                """
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([file(credentialsId: 'momen-env-file', variable: 'ENV_FILE')]) {
                    sh '''
                        cp "$ENV_FILE" .env
                        docker compose down --remove-orphans || true
                        docker compose up -d
                        rm -f .env
                    '''
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    def maxRetries = 24
                    def healthy = false

                    for (int i = 1; i <= maxRetries; i++) {
                        try {
                            sh "curl -sf http://host.docker.internal:${APP_PORT}/actuator/health"
                            healthy = true
                            echo "Health check 성공 (${i}/${maxRetries})"
                            break
                        } catch (Exception e) {
                            echo "Health check 대기 중... (${i}/${maxRetries})"
                            sleep 5
                        }
                    }

                    if (!healthy) {
                        sh 'docker logs momen-api --tail 50'
                        error 'Health check 실패 - 애플리케이션이 시작되지 않았습니다'
                    }
                }
            }
        }

        stage('Cleanup') {
            steps {
                sh 'docker image prune -f || true'
            }
        }
    }

    post {
        success {
            echo """
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            ✅ 배포 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER}
            🌐 http://100.50.98.194:${APP_PORT}
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            """
        }
        failure {
            echo "❌ 배포 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            // 실패 시 이전 이미지로 롤백
            sh """
                docker compose down --remove-orphans || true
                if docker image inspect ${APP_NAME}:build-\$((\${BUILD_NUMBER} - 1)) > /dev/null 2>&1; then
                    docker tag ${APP_NAME}:build-\$((\${BUILD_NUMBER} - 1)) ${APP_NAME}:latest
                    docker compose up -d || true
                    echo "⏪ 이전 빌드로 롤백 완료"
                fi
            """
        }
        always {
            cleanWs(deleteDirs: true)
        }
    }
}
