package stonks.fabric.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.server.command.ServerCommandSource;
import stonks.fabric.menu.MarketMainMenu;

public class MarketCommand {
	public static final LiteralArgumentBuilder<ServerCommandSource> ROOT = literal("market")
		.executes(ctx -> {
			var player = ctx.getSource().getPlayerOrThrow();
			var menu = new MarketMainMenu(null, player);
			menu.open();
			return 1;
		});
}
