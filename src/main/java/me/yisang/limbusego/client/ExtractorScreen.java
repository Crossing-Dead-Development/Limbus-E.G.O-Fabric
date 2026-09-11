package me.yisang.limbusego.client;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.extractor.ExtractorScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** 提取機 GUI：背景圖 + 依進度裁寬的箭頭。沒有按鈕。 */
public class ExtractorScreen extends HandledScreen<ExtractorScreenHandler> {

    private static final Identifier TEXTURE = LimbusEGOMod.id("textures/gui/extractor.png");
    private static final int ARROW_X = 79, ARROW_Y = 35, ARROW_W = 24, ARROW_H = 17;
    private static final int ARROW_FULL_U = 176, ARROW_FULL_V = 0;

    public ExtractorScreen(ExtractorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
        playerInventoryTitleY = backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        ctx.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        int max = handler.getMaxProgress();
        int progress = handler.getProgress();
        if (max > 0 && progress > 0) {
            int w = Math.min(ARROW_W, progress * ARROW_W / max);
            ctx.drawTexture(RenderLayer::getGuiTextured, TEXTURE,
                    x + ARROW_X, y + ARROW_Y, ARROW_FULL_U, ARROW_FULL_V, w, ARROW_H, 256, 256);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
    }
}
