package me.yisang.limbusego.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.yisang.limbusego.gift.ModGifts;
import me.yisang.limbusego.gui.GiftGui;
import me.yisang.limbusego.gui.WeaponAdminGui;
import me.yisang.limbusego.gui.WeaponCatalogGui;
import me.yisang.limbusego.item.ModItems;
import me.yisang.limbusego.status.StatusEffect;
import me.yisang.limbusego.status.StatusManager;
import me.yisang.limbusego.status.StatusState;
import me.yisang.limbusego.tooltip.TooltipFormat;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 指令樹 /limbusego（別名 lego）。
 *   weapon give <玩家> <id> [數量]   —— 權限 2
 *   weapon catalog                   —— 所有人
 *   weapon admin                     —— 權限 2
 *   weapon <id>                      —— 權限 2，直接給自己
 *   gift …                           —— 同上
 *   status <效果> <威力> [次數]       —— 權限 2，給自己施加屬性（測試用）
 *   status apply <玩家> <效果> <威力> [次數]
 *   status show|clear [玩家]
 */
public class LimbusCommand {

    /** 武器/彈藥 id → 物品。順序即補完與 GUI 顯示順序。 */
    private static final Map<String, Item> WEAPONS = new LinkedHashMap<>();
    static {
        WEAPONS.put("solemn_lament_black", ModItems.SOLEMN_LAMENT_BLACK);
        WEAPONS.put("solemn_lament_white", ModItems.SOLEMN_LAMENT_WHITE);
        WEAPONS.put("solemn_shield", ModItems.SOLEMN_SHIELD);
        WEAPONS.put("mimicry", ModItems.MIMICRY);
        WEAPONS.put("dacapo", ModItems.DACAPO);
        WEAPONS.put("ring_brush", ModItems.RING_BRUSH);
        WEAPONS.put("w_corp_knife", ModItems.W_CORP_KNIFE);
        WEAPONS.put("tiantui_star", ModItems.TIANTUI_STAR);
        WEAPONS.put("twilight", ModItems.TWILIGHT);
        WEAPONS.put("tibia", ModItems.TIBIA);
        WEAPONS.put("bladesinger", ModItems.BLADESINGER);
        WEAPONS.put("butterfly_quartz", ModItems.BUTTERFLY_QUARTZ);
        WEAPONS.put("tiger_mark", ModItems.TIGER_MARK);
        WEAPONS.put("savage_tiger_mark", ModItems.SAVAGE_TIGER_MARK);
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> root = build();
            dispatcher.register(root);
            // 別名 lego → 重新導向 limbusego
            dispatcher.register(CommandManager.literal("lego").redirect(dispatcher.getRoot().getChild("limbusego")));
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> build() {
        var weapon = CommandManager.literal("weapon");

        weapon.then(CommandManager.literal("catalog")
                .executes(ctx -> {
                    ctx.getSource().getPlayerOrThrow().openHandledScreen(WeaponCatalogGui.create());
                    return 1;
                }));

        weapon.then(CommandManager.literal("admin")
                .requires(src -> src.hasPermissionLevel(2))
                .executes(ctx -> {
                    ctx.getSource().getPlayerOrThrow().openHandledScreen(WeaponAdminGui.create());
                    return 1;
                }));

        weapon.then(CommandManager.literal("give")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .then(CommandManager.argument("id", StringArgumentType.word())
                                .suggests((ctx, b) -> { WEAPONS.keySet().forEach(b::suggest); return b.buildFuture(); })
                                .executes(ctx -> giveTo(ctx, 1))
                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> giveTo(ctx, IntegerArgumentType.getInteger(ctx, "count")))))));

        // weapon <id> —— 直接給自己
        for (String id : WEAPONS.keySet()) {
            weapon.then(CommandManager.literal(id)
                    .requires(src -> src.hasPermissionLevel(2))
                    .executes(ctx -> {
                        ServerPlayerEntity self = ctx.getSource().getPlayerOrThrow();
                        give(self, id, 1);
                        return 1;
                    }));
        }

        return CommandManager.literal("limbusego").then(weapon).then(buildGift()).then(buildStatus());
    }

    // ── status ──────────────────────────────────────────────────────

    private static final int STATUS_DEFAULT_COUNT = 3;

    private static LiteralArgumentBuilder<ServerCommandSource> buildStatus() {
        var status = CommandManager.literal("status").requires(src -> src.hasPermissionLevel(2));

        status.then(CommandManager.literal("apply")
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .then(statusEffectArg(ctx -> EntityArgumentType.getPlayer(ctx, "target")))));

        status.then(CommandManager.literal("show")
                .executes(ctx -> showStatus(ctx, ctx.getSource().getPlayerOrThrow()))
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> showStatus(ctx, EntityArgumentType.getPlayer(ctx, "target")))));

        status.then(CommandManager.literal("clear")
                .executes(ctx -> clearStatus(ctx, ctx.getSource().getPlayerOrThrow()))
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .executes(ctx -> clearStatus(ctx, EntityArgumentType.getPlayer(ctx, "target")))));

        // status <effect> <potency> [count] —— 直接給自己
        status.then(statusEffectArg(ctx -> ctx.getSource().getPlayerOrThrow()));
        return status;
    }

    /** {@code <effect> <potency> [count]} 三段參數，target 由呼叫端決定。 */
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String> statusEffectArg(
            TargetResolver target) {
        return CommandManager.argument("effect", StringArgumentType.word())
                .suggests((ctx, b) -> {
                    for (StatusEffect e : StatusEffect.values()) b.suggest(e.name().toLowerCase(Locale.ROOT));
                    return b.buildFuture();
                })
                .then(CommandManager.argument("potency", IntegerArgumentType.integer(1, 99))
                        .executes(ctx -> applyStatus(ctx, target.resolve(ctx), STATUS_DEFAULT_COUNT))
                        .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 999))
                                .executes(ctx -> applyStatus(ctx, target.resolve(ctx), IntegerArgumentType.getInteger(ctx, "count")))));
    }

    @FunctionalInterface
    private interface TargetResolver {
        ServerPlayerEntity resolve(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException;
    }

    private static int applyStatus(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target, int count) {
        String name = StringArgumentType.getString(ctx, "effect");
        StatusEffect effect;
        try {
            effect = StatusEffect.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            ctx.getSource().sendError(Text.translatable("limbusego.cmd.unknown_status", name));
            return 0;
        }
        int potency = IntegerArgumentType.getInteger(ctx, "potency");
        StatusManager.get().apply(target, effect, potency, count);
        ctx.getSource().sendFeedback(() -> Text.translatable("limbusego.cmd.status_applied",
                target.getName(), TooltipFormat.status(effect), potency, count), true);
        return 1;
    }

    private static int showStatus(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) {
        StatusState s = StatusManager.get().get(target);
        var snapshot = s == null ? Map.<StatusEffect, int[]>of() : s.snapshot();
        if (snapshot.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.translatable("limbusego.cmd.status_none", target.getName()), false);
            return 0;
        }
        var list = Text.empty();
        boolean first = true;
        for (var en : snapshot.entrySet()) {
            if (!first) list.append(Text.literal("§7, "));
            first = false;
            list.append(TooltipFormat.status(en.getKey()))
                    .append(Text.literal(" §f" + en.getValue()[0] + "§7/§f" + en.getValue()[1]));
        }
        ctx.getSource().sendFeedback(() -> Text.translatable("limbusego.cmd.status_list", target.getName(), list), false);
        return snapshot.size();
    }

    private static int clearStatus(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) {
        StatusManager.get().clear(target);
        ctx.getSource().sendFeedback(() -> Text.translatable("limbusego.cmd.status_cleared", target.getName()), true);
        return 1;
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> buildGift() {
        var gift = CommandManager.literal("gift");

        gift.then(CommandManager.literal("catalog")
                .executes(ctx -> {
                    ctx.getSource().getPlayerOrThrow().openHandledScreen(GiftGui.catalog());
                    return 1;
                }));

        gift.then(CommandManager.literal("admin")
                .requires(src -> src.hasPermissionLevel(2))
                .executes(ctx -> {
                    ctx.getSource().getPlayerOrThrow().openHandledScreen(GiftGui.admin());
                    return 1;
                }));

        gift.then(CommandManager.literal("give")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .then(CommandManager.argument("id", StringArgumentType.word())
                                .suggests((ctx, b) -> { ModGifts.byId().keySet().forEach(b::suggest); return b.buildFuture(); })
                                .executes(ctx -> giveGift(ctx, 1))
                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> giveGift(ctx, IntegerArgumentType.getInteger(ctx, "count")))))));

        // gift <id> —— 直接給自己
        for (String id : ModGifts.byId().keySet()) {
            gift.then(CommandManager.literal(id)
                    .requires(src -> src.hasPermissionLevel(2))
                    .executes(ctx -> {
                        ServerPlayerEntity self = ctx.getSource().getPlayerOrThrow();
                        self.getInventory().offerOrDrop(new ItemStack(ModGifts.byId().get(id), 1));
                        return 1;
                    }));
        }

        return gift;
    }

    private static int giveGift(CommandContext<ServerCommandSource> ctx, int count) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        String id = StringArgumentType.getString(ctx, "id").toLowerCase();
        Item item = ModGifts.byId().get(id);
        if (item == null) {
            ctx.getSource().sendError(Text.translatable("limbusego.cmd.unknown_gift", id));
            return 0;
        }
        target.getInventory().offerOrDrop(new ItemStack(item, count));
        ctx.getSource().sendFeedback(() -> Text.translatable("limbusego.cmd.given", target.getName(), count, id), true);
        return 1;
    }

    private static int giveTo(CommandContext<ServerCommandSource> ctx, int count) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        String id = StringArgumentType.getString(ctx, "id").toLowerCase();
        if (!WEAPONS.containsKey(id)) {
            ctx.getSource().sendError(Text.translatable("limbusego.cmd.unknown_weapon", id));
            return 0;
        }
        give(target, id, count);
        ctx.getSource().sendFeedback(() -> Text.translatable("limbusego.cmd.given", target.getName(), count, id), true);
        return 1;
    }

    private static void give(ServerPlayerEntity player, String id, int count) {
        player.getInventory().offerOrDrop(new ItemStack(WEAPONS.get(id), count));
    }
}
