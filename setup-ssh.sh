#!/bin/bash

# SSH 키 생성 및 서버 등록 스크립트

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔐 StoryG SSH 키 설정 가이드"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 서버 정보
DEV_SERVER_HOST="${DEV_SERVER_HOST:-100.50.98.194}"
DEV_SERVER_USER="${DEV_SERVER_USER:-root}"

# ── 1. SSH 키 확인 ────────────────────────────────────────
echo ""
echo "1️⃣ SSH 키 확인 중..."
if [ -f ~/.ssh/id_rsa ] || [ -f ~/.ssh/id_ed25519 ]; then
    echo "✅ 기존 SSH 키가 있습니다:"
    ls -la ~/.ssh/id_* 2>/dev/null | grep -v ".pub"
    echo ""
    read -p "기존 키를 사용하시겠습니까? (y/n): " USE_EXISTING
    if [ "$USE_EXISTING" != "y" ]; then
        CREATE_NEW=true
    else
        CREATE_NEW=false
    fi
else
    echo "⚠️  SSH 키가 없습니다."
    CREATE_NEW=true
fi

# ── 2. SSH 키 생성 ────────────────────────────────────────
if [ "$CREATE_NEW" = true ]; then
    echo ""
    echo "2️⃣ SSH 키 생성 중..."
    echo ""
    read -p "이메일 주소를 입력하세요 (키 식별용): " EMAIL

    # Ed25519 키 생성 (더 안전하고 빠름)
    ssh-keygen -t ed25519 -C "$EMAIL" -f ~/.ssh/id_ed25519 -N ""

    if [ $? -eq 0 ]; then
        echo "✅ SSH 키 생성 완료!"
        echo "   공개키: ~/.ssh/id_ed25519.pub"
        echo "   개인키: ~/.ssh/id_ed25519"
    else
        echo "❌ SSH 키 생성 실패"
        exit 1
    fi
fi

# ── 3. 공개키 확인 ────────────────────────────────────────
echo ""
echo "3️⃣ 공개키 확인:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 사용할 공개키 선택
if [ -f ~/.ssh/id_ed25519.pub ]; then
    PUB_KEY_FILE=~/.ssh/id_ed25519.pub
elif [ -f ~/.ssh/id_rsa.pub ]; then
    PUB_KEY_FILE=~/.ssh/id_rsa.pub
else
    echo "❌ 공개키를 찾을 수 없습니다."
    exit 1
fi

PUB_KEY=$(cat "$PUB_KEY_FILE")
echo "$PUB_KEY"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ── 4. 서버에 공개키 등록 ──────────────────────────────────
echo ""
echo "4️⃣ 서버에 공개키 등록"
echo ""
echo "서버: $DEV_SERVER_USER@$DEV_SERVER_HOST"
echo ""
echo "다음 중 하나의 방법을 선택하세요:"
echo "1) ssh-copy-id 사용 (자동 등록, 비밀번호 필요)"
echo "2) 수동으로 공개키 복사 (서버 관리자에게 전달)"
echo ""
read -p "선택 (1 또는 2): " COPY_METHOD

if [ "$COPY_METHOD" = "1" ]; then
    echo ""
    echo "📤 ssh-copy-id로 공개키 전송 중..."
    echo "⚠️  서버 비밀번호를 입력하세요:"
    ssh-copy-id -i "$PUB_KEY_FILE" "$DEV_SERVER_USER@$DEV_SERVER_HOST"

    if [ $? -eq 0 ]; then
        echo "✅ 공개키 등록 완료!"
    else
        echo "❌ 공개키 등록 실패"
        echo ""
        echo "수동 등록 방법:"
        echo "1. 위에 표시된 공개키를 복사하세요"
        echo "2. 서버에 접속: ssh $DEV_SERVER_USER@$DEV_SERVER_HOST"
        echo "3. 다음 명령 실행:"
        echo "   mkdir -p ~/.ssh"
        echo "   echo '$PUB_KEY' >> ~/.ssh/authorized_keys"
        echo "   chmod 700 ~/.ssh"
        echo "   chmod 600 ~/.ssh/authorized_keys"
        exit 1
    fi
elif [ "$COPY_METHOD" = "2" ]; then
    echo ""
    echo "📋 공개키를 클립보드에 복사합니다."
    echo "$PUB_KEY" | pbcopy 2>/dev/null || echo "$PUB_KEY"
    echo ""
    echo "서버 관리자에게 다음 정보를 전달하세요:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "공개키:"
    echo "$PUB_KEY"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "서버에서 실행할 명령:"
    echo "  mkdir -p ~/.ssh"
    echo "  echo '$PUB_KEY' >> ~/.ssh/authorized_keys"
    echo "  chmod 700 ~/.ssh"
    echo "  chmod 600 ~/.ssh/authorized_keys"
    echo ""
    read -p "서버에 등록이 완료되었나요? (y/n): " REGISTERED
    if [ "$REGISTERED" != "y" ]; then
        echo "⚠️  서버에 공개키를 등록한 후 다시 시도하세요."
        exit 1
    fi
else
    echo "❌ 잘못된 선택입니다."
    exit 1
fi

# ── 5. SSH 접속 테스트 ────────────────────────────────────
echo ""
echo "5️⃣ SSH 접속 테스트 중..."
ssh -o ConnectTimeout=5 -o BatchMode=yes "$DEV_SERVER_USER@$DEV_SERVER_HOST" "echo 'SSH connection successful'" 2>&1

if [ $? -eq 0 ]; then
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "✅ SSH 접속 성공!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "이제 배포 스크립트를 실행할 수 있습니다:"
    echo "  ./deploy-dev.sh"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
else
    echo ""
    echo "❌ SSH 접속 실패"
    echo ""
    echo "다음을 확인하세요:"
    echo "1. 서버에 공개키가 올바르게 등록되었는지"
    echo "2. 서버의 SSH 설정이 올바른지"
    echo "3. 방화벽이 SSH 포트(22)를 허용하는지"
    echo ""
    echo "수동 테스트:"
    echo "  ssh $DEV_SERVER_USER@$DEV_SERVER_HOST"
fi
