package com.momen.application.planner;

/**
 * 주차별 피드백 AI 요약용 프롬프트 템플릿 과목별??
 */
public final class FeedbackSummaryPromptTemplates {

    private static final String BASE = "다음은 멘토가 %d주차에 작성한 %s 피드백입니다. 핵심을 2~3문장으로 요약해 주세요. 한국어로 작성.\n\n[원문]\n%s";

    public static String korean(int weekNumber, String content) {
        return String.format(BASE, weekNumber, "독서와 문법", content);
    }

    public static String math(int weekNumber, String content) {
        return String.format(BASE, weekNumber, "수학", content);
    }

    public static String english(int weekNumber, String content) {
        return String.format(BASE, weekNumber, "영어", content);
    }

    public static String science(int weekNumber, String content) {
        return String.format(BASE, weekNumber, "과학", content);
    }

    public static String total(int weekNumber, String content) {
        return String.format("다음은 멘토가 %d주차에 작성한 총평 피드백입니다. 핵심을 2~3문장으로 요약해 주세요. 한국어로 작성.\n\n[원문]\n%s", weekNumber, content);
    }

    // ---- 월별 요약용 (해당 월 전체 원문을 한 번에 요약)
    private static final String MONTHLY_BASE = "다음은 멘토가 %s에 작성한 %s 피드백입니다. 한 달치 내용을 3~5문장으로 요약해 주세요. 한국어로 작성.\n\n[원문]\n%s";

    public static String monthlyKorean(String yearMonthLabel, String content) {
        return String.format(MONTHLY_BASE, yearMonthLabel, "독서와 문법", content);
    }

    public static String monthlyMath(String yearMonthLabel, String content) {
        return String.format(MONTHLY_BASE, yearMonthLabel, "수학", content);
    }

    public static String monthlyEnglish(String yearMonthLabel, String content) {
        return String.format(MONTHLY_BASE, yearMonthLabel, "영어", content);
    }

    public static String monthlyScience(String yearMonthLabel, String content) {
        return String.format(MONTHLY_BASE, yearMonthLabel, "과학", content);
    }

    public static String monthlyTotal(String yearMonthLabel, String content) {
        return String.format("다음은 멘토가 %s에 작성한 총평 피드백입니다. 한 달치 내용을 3~5문장으로 요약해 주세요. 한국어로 작성.\n\n[원문]\n%s", yearMonthLabel, content);
    }

    private FeedbackSummaryPromptTemplates() {}
}
