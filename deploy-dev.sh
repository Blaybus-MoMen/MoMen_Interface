#!/bin/bash

# 오류 발생 시 즉시 중단
set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Momen 개발 서버 배포"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ── 환경 설정 ──────────────────────────────────────────────
IMAGE_NAME="momen"
CONTAINER_NAME="momen"
HOST_PORT=8089
CONTAINER_PORT=8080

# 개발 서버 정보
DEV_SERVER_HOST="${DEV_SERVER_HOST:-221.148.101.200}"
DEV_SERVER_USER="${DEV_SERVER_USER:-root}"
DEV_SERVER_PATH="${DEV_SERVER_PATH:-/root/momen}"

# ── 배포 방법 선택 ────────────────────────────────────────
echo ""
echo "배포 방법을 선택하세요:"
echo "1) 로컬에서 이미지 빌드 후 서버로 전송 (권장)"
echo "2) 서버에 직접 접속해서 빌드 및 실행"
echo "3) Docker Compose로 배포 (서버에서 빌드)"
echo ""
read -p "선택 (1, 2 또는 3): " DEPLOY_METHOD

if [ "$DEPLOY_METHOD" = "1" ]; then
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📦 방법 1: 로컬 빌드 후 서버 배포"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # ── 로컬에서 Gradle 빌드 ──────────────────────────────────
    echo ""
    echo "🧱 로컬에서 Gradle 빌드 시작..."
    chmod +x ./gradlew
    ./gradlew clean build -x test --no-daemon

    JAR_FILE="build/libs/momen-0.0.1-SNAPSHOT.jar"
    if [ ! -f "$JAR_FILE" ]; then
        echo "❌ JAR 파일 없음: $JAR_FILE"
        exit 1
    fi
    echo "✅ JAR 파일 빌드 완료"

    # ── Docker 이미지 빌드 ──────────────────────────────────
    echo ""
    echo "🐳 Docker 이미지 빌드 중 (Linux 플랫폼)..."
    docker build --pull \
      --platform linux/amd64 \
      -t "$IMAGE_NAME:latest" \
      .

    echo "✅ 이미지 빌드 완료"

    # ── 이미지를 tar 파일로 저장 ────────────────────────────
    echo ""
    echo "💾 이미지를 tar 파일로 저장 중..."
    TAR_FILE="momen-image.tar"
    docker save "$IMAGE_NAME:latest" -o "$TAR_FILE"
    echo "✅ 이미지 저장 완료: $TAR_FILE"

    # ── 서버로 전송 ────────────────────────────────────────
    echo ""
    echo "📤 서버로 이미지 전송 중..."
    echo "서버 정보: $DEV_SERVER_USER@$DEV_SERVER_HOST:$DEV_SERVER_PATH"
    read -r -p "계속하시겠습니까? (y/n): " CONFIRM
    CONFIRM=$(echo "$CONFIRM" | tr '[:upper:]' '[:lower:]' | xargs)

    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "yes" ]; then
        echo "❌ 배포 취소됨"
        exit 1
    fi

    # 서버에 디렉토리 생성
    ssh "$DEV_SERVER_USER@$DEV_SERVER_HOST" "mkdir -p $DEV_SERVER_PATH"

    # 이미지 파일 전송
    scp "$TAR_FILE" "$DEV_SERVER_USER@$DEV_SERVER_HOST:$DEV_SERVER_PATH/"

    # 배포 스크립트, docker-compose.yml, 환경변수 파일 전송
    scp deploy-remote.sh docker-compose.yml "$DEV_SERVER_USER@$DEV_SERVER_HOST:$DEV_SERVER_PATH/"
    if [ -f ".env" ]; then
        scp .env "$DEV_SERVER_USER@$DEV_SERVER_HOST:$DEV_SERVER_PATH/momen.env"
        echo "✅ .env 파일 전송 완료 (momen.env)"
    else
        echo "⚠️  .env 파일이 없습니다. .env.example을 참고하세요."
        scp .env.example "$DEV_SERVER_USER@$DEV_SERVER_HOST:$DEV_SERVER_PATH/"
    fi

    echo "✅ 파일 전송 완료"

    # ── 서버에서 이미지 로드 및 실행 ────────────────────────
    echo ""
    echo "🚀 서버에서 이미지 로드 및 실행 중..."
    ssh "$DEV_SERVER_USER@$DEV_SERVER_HOST" "cd $DEV_SERVER_PATH && chmod +x deploy-remote.sh && ./deploy-remote.sh"

    # 로컬 tar 파일 정리
    rm -f "$TAR_FILE"

elif [ "$DEPLOY_METHOD" = "2" ]; then
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📦 방법 2: 서버에서 직접 빌드 및 실행"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    echo ""
    echo "서버 정보: $DEV_SERVER_USER@$DEV_SERVER_HOST:$DEV_SERVER_PATH"
    read -r -p "계속하시겠습니까? (y/n): " CONFIRM
    CONFIRM=$(echo "$CONFIRM" | tr '[:upper:]' '[:lower:]' | xargs)

    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "yes" ]; then
        echo "❌ 배포 취소됨"
        exit 1
    fi

    # 서버에 디렉토리 생성
    ssh "$DEV_SERVER_USER@$DEV_SERVER_HOST" "mkdir -p $DEV_SERVER_PATH"

    # 프로젝트 파일 전송 (gitignore 제외)
    echo ""
    echo "📤 프로젝트 파일 전송 중..."
    rsync -avz --exclude-from=.gitignore \
      --exclude='.git' \
      --exclude='build/' \
      --exclude='logs/' \
      --exclude='*.tar' \
      ./ "$DEV_SERVER_USER@$DEV_SERVER_HOST:$DEV_SERVER_PATH/"

    echo "✅ 파일 전송 완료"

    # ── 서버에서 빌드 및 실행 ────────────────────────────────
    echo ""
    echo "🚀 서버에서 빌드 및 실행 중..."
    ssh "$DEV_SERVER_USER@$DEV_SERVER_HOST" "cd $DEV_SERVER_PATH && chmod +x deploy-remote.sh && ./deploy-remote.sh"

elif [ "$DEPLOY_METHOD" = "3" ]; then
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📦 방법 3: Docker Compose 배포"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    echo ""
    echo "서버 정보: $DEV_SERVER_USER@$DEV_SERVER_HOST:$DEV_SERVER_PATH"
    read -r -p "계속하시겠습니까? (y/n): " CONFIRM
    CONFIRM=$(echo "$CONFIRM" | tr '[:upper:]' '[:lower:]' | xargs)

    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "yes" ]; then
        echo "❌ 배포 취소됨"
        exit 1
    fi

    # 서버에 디렉토리 생성
    ssh "$DEV_SERVER_USER@$DEV_SERVER_HOST" "mkdir -p $DEV_SERVER_PATH"

    # 프로젝트 파일 전송
    echo ""
    echo "📤 프로젝트 파일 전송 중..."
    rsync -avz --exclude-from=.gitignore \
      --exclude='.git' \
      --exclude='build/' \
      --exclude='logs/' \
      --exclude='*.tar' \
      ./ "$DEV_SERVER_USER@$DEV_SERVER_HOST:$DEV_SERVER_PATH/"

    echo "✅ 파일 전송 완료"

    # ── 서버에서 Docker Compose 실행 ────────────────────────
    echo ""
    echo "🚀 서버에서 Docker Compose 실행 중..."
    ssh "$DEV_SERVER_USER@$DEV_SERVER_HOST" "cd $DEV_SERVER_PATH && docker-compose up -d --build"

else
    echo "❌ 잘못된 선택입니다."
    exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎉 배포 완료!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 애플리케이션: http://$DEV_SERVER_HOST:$HOST_PORT/api"
echo "📊 Health Check: http://$DEV_SERVER_HOST:$HOST_PORT/api/actuator/health"
echo "📋 서버에서 컨테이너 확인: ssh $DEV_SERVER_USER@$DEV_SERVER_HOST 'docker ps | grep $CONTAINER_NAME'"
echo "📋 로그 확인: ssh $DEV_SERVER_USER@$DEV_SERVER_HOST 'docker logs -f $CONTAINER_NAME'"
echo "🛑 중지 명령: ssh $DEV_SERVER_USER@$DEV_SERVER_HOST 'docker stop $CONTAINER_NAME'"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
