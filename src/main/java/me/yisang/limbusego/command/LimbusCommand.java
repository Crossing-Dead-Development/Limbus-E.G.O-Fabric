package me.yisang.limbusego.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.yisang.limbusego.gui.WeaponAdminGui;
import me.yisang.limbusego.gui.WeaponCatalogGui;
import me.yisang.limbusego.item.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 指令樹 /limbusego（別名 lego）。
 *   weapon give <玩家> <id> [數量]   —— 權限 2
 *   weapon catalog                   —— 所有人
 *   weapon admin                     —— 權限 2
 *   weapon <id>                      —— 權限 2，直接給自己
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

        return CommandManager.literal("limbusego").then(weapon);
    }

    private static int giveTo(CommandContext<ServerCommandSource> ctx, int count) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        String id = StringArgumentType.getString(ctx, "id").toLowerCase();
        if (!WEAPONS.containsKey(id)) {
            ctx.getSource().sendError(Text.literal("未知武器：" + id));
            return 0;
        }
        give(target, id, count);
        ctx.getSource().sendFeedback(() -> Text.literal("§a已給予 " + target.getName().getString() + " × " + count + " " + id), true);
        return 1;
    }

    private static void give(ServerPlayerEntity player, String id, int count) {
        player.getInventory().offerOrDrop(new ItemStack(WEAPONS.get(id), count));
    }
}
