package me.yisang.limbusego;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** 測試共用：讀取 lang JSON 的鍵集。不載入 Minecraft。 */
public final class LangKeys {

    private static final Path DIR = Path.of("src/main/resources/assets/limbusego/lang");
    private static final Pattern KEY = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:");

    private static final Pattern ENTRY = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    /** 三個 lang 檔名，鍵集必須完全一致。 */
    public static final java.util.List<String> FILES = java.util.List.of("zh_tw.json", "zh_cn.json", "en_us.json");

    private static Set<String> zh;
    private static Set<String> cn;
    private static Set<String> en;
    private static Map<String, String> zhValues;
    private static Map<String, String> cnValues;
    private static Map<String, String> enValues;

    private LangKeys() {}

    public static synchronized Set<String> zhTw() {
        if (zh == null) zh = read("zh_tw.json");
        return zh;
    }

    public static synchronized Set<String> zhCn() {
        if (cn == null) cn = read("zh_cn.json");
        return cn;
    }

    public static synchronized Set<String> enUs() {
        if (en == null) en = read("en_us.json");
        return en;
    }

    public static synchronized Map<String, String> zhTwValues() {
        if (zhValues == null) zhValues = readValues("zh_tw.json");
        return zhValues;
    }

    public static synchronized Map<String, String> zhCnValues() {
        if (cnValues == null) cnValues = readValues("zh_cn.json");
        return cnValues;
    }

    public static synchronized Map<String, String> enUsValues() {
        if (enValues == null) enValues = readValues("en_us.json");
        return enValues;
    }

    /** 斷言某個翻譯鍵在繁中、簡中、英文 lang 檔都存在。 */
    public static void assertKeyExists(String key) {
        assertTrue(zhTw().contains(key), "zh_tw.json 缺少翻譯鍵：" + key);
        assertTrue(zhCn().contains(key), "zh_cn.json 缺少翻譯鍵：" + key);
        assertTrue(enUs().contains(key), "en_us.json 缺少翻譯鍵：" + key);
    }

    /** 依檔名取值表。 */
    public static Map<String, String> values(String file) {
        return switch (file) {
            case "zh_tw.json" -> zhTwValues();
            case "zh_cn.json" -> zhCnValues();
            case "en_us.json" -> enUsValues();
            default -> throw new IllegalArgumentException(file);
        };
    }

    private static Set<String> read(String file) {
        try {
            Set<String> keys = new LinkedHashSet<>();
            for (String line : Files.readAllLines(DIR.resolve(file), StandardCharsets.UTF_8)) {
                Matcher m = KEY.matcher(line);
                if (m.find()) keys.add(m.group(1));
            }
            if (keys.isEmpty()) throw new IllegalStateException("讀不到任何翻譯鍵：" + file);
            return keys;
        } catch (IOException e) {
            throw new IllegalStateException("讀取 lang 檔失敗：" + file, e);
        }
    }

    private static Map<String, String> readValues(String file) {
        try {
            Map<String, String> out = new LinkedHashMap<>();
            for (String line : Files.readAllLines(DIR.resolve(file), StandardCharsets.UTF_8)) {
                Matcher m = ENTRY.matcher(line);
                if (m.find()) out.put(m.group(1), m.group(2));
            }
            if (out.isEmpty()) throw new IllegalStateException("讀不到任何翻譯值：" + file);
            return out;
        } catch (IOException e) {
            throw new IllegalStateException("讀取 lang 檔失敗：" + file, e);
        }
    }
}
