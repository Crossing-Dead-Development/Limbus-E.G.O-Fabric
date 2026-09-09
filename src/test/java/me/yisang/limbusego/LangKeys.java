package me.yisang.limbusego;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** 測試共用：讀取 lang JSON 的鍵集。不載入 Minecraft。 */
public final class LangKeys {

    private static final Path DIR = Path.of("src/main/resources/assets/limbusego/lang");
    private static final Pattern KEY = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:");

    private static Set<String> zh;
    private static Set<String> en;

    private LangKeys() {}

    public static synchronized Set<String> zhTw() {
        if (zh == null) zh = read("zh_tw.json");
        return zh;
    }

    public static synchronized Set<String> enUs() {
        if (en == null) en = read("en_us.json");
        return en;
    }

    /** 斷言某個翻譯鍵在中英文 lang 檔都存在。 */
    public static void assertKeyExists(String key) {
        assertTrue(zhTw().contains(key), "zh_tw.json 缺少翻譯鍵：" + key);
        assertTrue(enUs().contains(key), "en_us.json 缺少翻譯鍵：" + key);
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
}
