#!/bin/bash

# StoryG Database Setup Script
# 이 스크립트는 MariaDB 데이터베이스와 사용자를 자동으로 설정합니다.

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🗄️ StoryG Database Setup"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 데이터베이스 설정 (필요시 수정)
DB_NAME="${DB_NAME:-storyg}"
DB_USER="${DB_USER:-storyg_user}"
DB_PASSWORD="${DB_PASSWORD:-storyg_password}"

echo "📋 설정할 데이터베이스 정보:"
echo "   - 데이터베이스: $DB_NAME"
echo "   - 사용자: $DB_USER"
echo ""

# MariaDB root 비밀번호 입력
echo "MariaDB root 비밀번호를 입력하세요:"
read -s MYSQL_ROOT_PASSWORD
echo ""

# 데이터베이스 생성 및 사용자 설정
echo "🔧 데이터베이스 생성 중..."
mysql -u root -p"$MYSQL_ROOT_PASSWORD" << EOF
-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 전용 사용자 생성 (이미 존재하면 무시)
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASSWORD';
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASSWORD';

-- 권한 부여
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%';
FLUSH PRIVILEGES;

-- 확인
SELECT 'Database created successfully!' AS Status;
SHOW DATABASES LIKE '$DB_NAME';
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "✅ 데이터베이스 설정 완료!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "📋 .env 파일에 다음 설정을 추가하세요:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "DB_HOST=localhost"
    echo "DB_PORT=3306"
    echo "DB_NAME=$DB_NAME"
    echo "DB_USER=$DB_USER"
    echo "DB_PASSWORD=$DB_PASSWORD"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "또는 Docker에서 호스트 DB 접속 시:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "DB_HOST=host.docker.internal"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""

    # 연결 테스트
    echo "🔍 데이터베이스 연결 테스트..."
    mysql -u "$DB_USER" -p"$DB_PASSWORD" -e "SELECT 'Connection successful!' AS Status;" "$DB_NAME" 2>/dev/null

    if [ $? -eq 0 ]; then
        echo "✅ 연결 테스트 성공!"
    else
        echo "⚠️  연결 테스트 실패. 설정을 확인하세요."
    fi

else
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "❌ 데이터베이스 설정 실패!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "다음을 확인하세요:"
    echo "1. MariaDB가 실행 중인지"
    echo "2. root 비밀번호가 올바른지"
    echo ""
    echo "수동 설정 방법:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "mysql -u root -p"
    echo ""
    echo "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo "CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASSWORD';"
    echo "GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'%';"
    echo "FLUSH PRIVILEGES;"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    exit 1
fi
