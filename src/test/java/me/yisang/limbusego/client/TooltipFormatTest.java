package me.yisang.limbusego.client;

import me.yisang.limbusego.LangKeys;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TooltipFormatTest {

    @Test
    void sectionIsGoldAndPrefixed() {
        var text = TooltipFormat.section("tooltip.limbusego.hint");
        assertEquals(Formatting.GOLD.getColorValue().intValue(), text.getStyle().getColor().getRgb());
        assertTrue(text.getString().startsWith("▸ "), "段落標題需以 ▸ 開頭，實得：" + text.getString());
    }

    @Test
    void bodyIsGrayAndIndented() {
        var text = TooltipFormat.body("tooltip.limbusego.hint");
        assertEquals(Formatting.GRAY.getColorValue().intValue(), text.getStyle().getColor().getRgb());
        assertTrue(text.getString().startsWith("  "), "內文需縮排兩格，實得：" + text.getString());
    }

    @Test
    void bodyIsNeverItalic() {
        // Minecraft 對 LORE 預設套斜體；我們的行必須明確關掉，否則版面不一致
        assertEquals(Boolean.FALSE, TooltipFormat.body("tooltip.limbusego.hint").getStyle().isItalic());
        assertEquals(Boolean.FALSE, TooltipFormat.section("tooltip.limbusego.hint").getStyle().isItalic());
    }

    @Test
    void statusUsesItsOwnTranslationKeyAndColor() {
        var burn = TooltipFormat.status(StatusEffect.BURN);
        var content = (TranslatableTextContent) burn.getContent();
        assertEquals("status.limbusego.burn", content.getKey());
        assertNotNull(burn.getStyle().getColor(), "屬性名稱必須有顏色");
    }

    @Test
    void giftTagRendersRomanTier() {
        // 測試環境沒載入語言檔，getString() 只會回傳鍵名並丟掉參數，因此直接驗參數
        assertEquals("I", tierArg(1));
        assertEquals("II", tierArg(2));
        assertEquals("IV", tierArg(4));
    }

    private static Object tierArg(int tier) {
        var content = (TranslatableTextContent) TooltipFormat.giftTag(tier).getContent();
        assertEquals("tooltip.limbusego.tag.gift", content.getKey());
        assertEquals(1, content.getArgs().length);
        return content.getArgs()[0];
    }

    @Test
    void sharedKeysExistInBothLanguages() {
        LangKeys.assertKeyExists("tooltip.limbusego.hint");
        LangKeys.assertKeyExists("tooltip.limbusego.tag.ego");
        LangKeys.assertKeyExists("tooltip.limbusego.tag.gift");
        LangKeys.assertKeyExists("tooltip.limbusego.potency");
    }
}
