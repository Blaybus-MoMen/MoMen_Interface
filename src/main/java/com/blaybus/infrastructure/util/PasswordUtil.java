package com.blaybus.infrastructure.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 비밀번호 관련 유틸리티 클래스
 */
public class PasswordUtil {
    private static final String U = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String L = "abcdefghijkmnpqrstuvwxyz";
    private static final String D = "123456789";
    private static final String S = "!@#$%^&*()-_=+[]{};:,.?";
    private static final SecureRandom R = new SecureRandom();

    // 보안이 강화된 임시 비밀번호 생성
    public static String generateSecureTempPassword(int length) {
        if (length < 8) length = 8;

        List<Character> list = new ArrayList<>();
        list.add(pick(U)); list.add(pick(L)); list.add(pick(D)); list.add(pick(S));

        String all = U + L + D + S;
        while (list.size() < length) list.add(pick(all));

        Collections.shuffle(list, R);

        // 간단한 연속 3회 동일 문자 방지
        for (int i = 2; i < list.size(); i++) {
            if (list.get(i).equals(list.get(i - 1)) && list.get(i).equals(list.get(i - 2))) {
                list.set(i, pick(all));
            }
        }

        StringBuilder result = new StringBuilder();
        list.forEach(result::append);

        return result.toString();
    }

    private static char pick(String pool) { return pool.charAt(R.nextInt(pool.length())); }

    private PasswordUtil() {}
}
