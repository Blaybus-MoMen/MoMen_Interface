package com.momen.domain.user;

/**
 * OAuth 제공자 열거형
 */
public enum OAuthProvider {
    KAKAO("kakao"),
    GOOGLE("google"),
    NAVER("naver");

    private final String providerName;

    OAuthProvider(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }

    public static OAuthProvider fromString(String provider) {
        for (OAuthProvider op : OAuthProvider.values()) {
            if (op.providerName.equalsIgnoreCase(provider)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown OAuth provider: " + provider);
    }
}
