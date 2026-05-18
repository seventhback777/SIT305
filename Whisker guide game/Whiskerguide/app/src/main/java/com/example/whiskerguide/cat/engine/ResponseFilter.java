package com.example.whiskerguide.cat.engine;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseFilter {

    private static final int MAX_LENGTH = 200;
    private static final String FALLBACK = "Meow? That question is outside our adventure~";

    private static final List<String> OFF_TOPIC = Arrays.asList(
            "weather", "stock", "bitcoin", "news", "election", "president",
            "as an ai", "i am an ai", "i'm an ai", "as a language model"
    );

    // Strip common few-shot leakage prefixes (covers "A:", "Answer:", "You:", and markdown list markers).
    private static final Pattern PREFIX_PATTERN = Pattern.compile(
            "^\\s*(?:[Aa]\\s*[::]\\s*|Answer\\s*[::]\\s*|You\\s*[::]\\s*|答\\s*[::]\\s*|[-•*]\\s+)",
            Pattern.MULTILINE);

    // 检测三个或以上相同字符连续出现
    private static final Pattern REPEAT_PATTERN = Pattern.compile("(.)\\1{3,}");

    public String filter(String raw) {
        if (raw == null) return FALLBACK;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return FALLBACK;

        // 剥掉 "A: " 前缀
        Matcher m = PREFIX_PATTERN.matcher(trimmed);
        if (m.find() && m.start() == 0) {
            trimmed = trimmed.substring(m.end()).trim();
        }

        // 截断到第二个换行(只取第一段,避免连续 Q&A 幻觉)
        int firstQ = trimmed.indexOf("\nQ");
        if (firstQ > 0) trimmed = trimmed.substring(0, firstQ).trim();
        int doubleNewline = trimmed.indexOf("\n\n");
        if (doubleNewline > 0) trimmed = trimmed.substring(0, doubleNewline).trim();

        // 检测严重重复 → 截断到重复点
        Matcher r = REPEAT_PATTERN.matcher(trimmed);
        if (r.find()) {
            trimmed = trimmed.substring(0, r.start()).trim();
            if (!trimmed.isEmpty() && !endsWithPunct(trimmed)) trimmed += "...";
        }

        if (trimmed.isEmpty()) return FALLBACK;

        String lower = trimmed.toLowerCase(Locale.ROOT);
        for (String tag : OFF_TOPIC) {
            if (lower.contains(tag)) return FALLBACK;
        }

        if (trimmed.length() > MAX_LENGTH) {
            int cut = MAX_LENGTH;
            int lastPunct = Math.max(
                    trimmed.lastIndexOf("。", MAX_LENGTH),
                    trimmed.lastIndexOf(".", MAX_LENGTH));
            if (lastPunct > MAX_LENGTH / 2) cut = lastPunct + 1;
            trimmed = trimmed.substring(0, cut).trim() + "...";
        }
        return trimmed;
    }

    private boolean endsWithPunct(String s) {
        if (s.isEmpty()) return false;
        char c = s.charAt(s.length() - 1);
        return c == '。' || c == '.' || c == '!' || c == '!' || c == '?' || c == '?' || c == '~';
    }
}
