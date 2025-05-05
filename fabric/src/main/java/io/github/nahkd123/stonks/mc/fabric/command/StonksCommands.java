package io.github.nahkd123.stonks.mc.fabric.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.concurrent.CompletionException;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.nahkd123.stonks.mc.fabric.StonksMcInstance;
import io.github.nahkd123.stonks.mc.fabric.StonksMod;
import io.github.nahkd123.stonks.mc.fabric.econ.EconomyService;
import io.github.nahkd123.stonks.mc.fabric.gui.MarketMenu;
import io.github.nahkd123.stonks.mc.fabric.gui.tasked.PlayerAction;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class StonksCommands {
	public static final LiteralArgumentBuilder<ServerCommandSource> MARKET = literal("marketnext")
		.executes(StonksCommands::market);

	private static int market(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		StonksMcInstance instance = StonksMod.getInstance(ctx.getSource().getServer());
		MarketMenu menu = new MarketMenu(player, instance);
		menu.openTasked().exceptionally(t -> {
			if (t instanceof CompletionException e && e.getCause() != null) t = e.getCause();
			if (!(t instanceof PlayerAction)) t.printStackTrace();
			return null;
		});
		return 0;
	}

	public static final LiteralArgumentBuilder<ServerCommandSource> ADMIN_ROOT = literal("stonksnext")
		.requires(s -> s.hasPermissionLevel(CommandManager.field_31841))
		.then(literal("balanceof")
			.then(argument("player", EntityArgumentType.player())
				.executes(ctx -> balanceOf(ctx, EntityArgumentType.getPlayer(ctx, "player"))))
			.executes(ctx -> balanceOf(ctx, ctx.getSource().getPlayerOrThrow())));

	private static int balanceOf(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
		StonksMcInstance instance = StonksMod.getInstance(ctx.getSource().getServer());
		EconomyService service = instance.getEconomyService();

		if (service == null) {
			ctx.getSource().sendError(Text.literal("Economy service is not configured; all players have balance of 0"));
			return 0;
		}

		long balance = service.queryBalance(player.getGameProfile()).join();
		ctx.getSource().sendMessage(Text.empty()
			.append(player.getDisplayName())
			.append(" have balance of ")
			.append(service.formatDisplayCurrency(balance))
			.append(" (Raw is %d)".formatted(balance)));
		return (int) balance;
	}
}
