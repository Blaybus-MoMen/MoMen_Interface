#!/bin/bash

# StoryG Redis 데이터 확인 스크립트

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔍 StoryG Redis 데이터 확인"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Redis 호스트 설정
REDIS_HOST="${REDIS_HOST:-localhost}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_PASSWORD="${REDIS_PASSWORD:-}"

# Redis CLI 명령어 설정
if [ -n "$REDIS_PASSWORD" ]; then
    REDIS_CMD="redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD"
else
    REDIS_CMD="redis-cli -h $REDIS_HOST -p $REDIS_PORT"
fi

echo "📡 Redis 연결 정보: $REDIS_HOST:$REDIS_PORT"
echo ""

echo "1️⃣ 이메일 인증번호 관련 키:"
$REDIS_CMD --scan --pattern "email:verification:*" 2>/dev/null | while read key; do
    if [ ! -z "$key" ]; then
        email=$(echo $key | cut -d: -f3)
        code=$($REDIS_CMD GET "$key" 2>/dev/null)
        ttl=$($REDIS_CMD TTL "$key" 2>/dev/null)
        echo "   - $email: $code (TTL: ${ttl}s)"
    fi
done

echo ""
echo "2️⃣ 이메일 인증 완료 플래그:"
$REDIS_CMD --scan --pattern "email:verified:*" 2>/dev/null | while read key; do
    if [ ! -z "$key" ]; then
        email=$(echo $key | cut -d: -f3)
        verified=$($REDIS_CMD GET "$key" 2>/dev/null)
        ttl=$($REDIS_CMD TTL "$key" 2>/dev/null)
        echo "   - $email: $verified (TTL: ${ttl}s)"
    fi
done

echo ""
echo "3️⃣ 인증 시도 횟수:"
$REDIS_CMD --scan --pattern "email:attempt:*" 2>/dev/null | while read key; do
    if [ ! -z "$key" ]; then
        email=$(echo $key | cut -d: -f3)
        count=$($REDIS_CMD GET "$key" 2>/dev/null)
        ttl=$($REDIS_CMD TTL "$key" 2>/dev/null)
        echo "   - $email: $count 회 (TTL: ${ttl}s)"
    fi
done

echo ""
echo "4️⃣ Refresh Token (사용자 ID별):"
$REDIS_CMD --scan --pattern "token:refresh:*" 2>/dev/null | while read key; do
    if [ ! -z "$key" ]; then
        userId=$(echo $key | cut -d: -f3)
        token=$($REDIS_CMD GET "$key" 2>/dev/null)
        ttl=$($REDIS_CMD TTL "$key" 2>/dev/null)
        if [ ! -z "$token" ]; then
            tokenPreview="${token:0:30}..."
            echo "   - UserID: $userId, Token: $tokenPreview (TTL: ${ttl}s)"
        fi
    fi
done

echo ""
echo "5️⃣ Access Token 블랙리스트:"
blacklistCount=$($REDIS_CMD --scan --pattern "token:blacklist:*" 2>/dev/null | wc -l | tr -d ' ')
echo "   - 블랙리스트된 토큰 수: $blacklistCount"

echo ""
echo "6️⃣ 세션 관련 키:"
sessionCount=$($REDIS_CMD --scan --pattern "session:*" 2>/dev/null | wc -l | tr -d ' ')
echo "   - 세션 키 수: $sessionCount"

echo ""
echo "7️⃣ 캐시 관련 키:"
cacheCount=$($REDIS_CMD --scan --pattern "cache:*" 2>/dev/null | wc -l | tr -d ' ')
echo "   - 캐시 키 수: $cacheCount"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 전체 통계"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
totalKeys=$($REDIS_CMD DBSIZE 2>/dev/null)
echo "   - 총 키 개수: $totalKeys"

echo ""
echo "📋 Redis 정보:"
$REDIS_CMD INFO server 2>/dev/null | grep -E "redis_version|uptime_in_days|connected_clients" || echo "   Redis 정보를 가져올 수 없습니다."
echo ""
