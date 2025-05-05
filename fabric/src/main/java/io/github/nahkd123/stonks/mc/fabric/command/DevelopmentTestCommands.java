package io.github.nahkd123.stonks.mc.fabric.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import io.github.nahkd123.stonks.mc.fabric.StonksMcInstance;
import io.github.nahkd123.stonks.mc.fabric.StonksMod;
import io.github.nahkd123.stonks.mc.fabric.econ.EconomyService;
import io.github.nahkd123.stonks.mc.fabric.gui.tasked.TaskedSignGui;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DevelopmentTestCommands {
	public static final LiteralArgumentBuilder<ServerCommandSource> ROOT = literal("stonksdev")
		.then(literal("test")
			.then(literal("signinput")
				.executes(DevelopmentTestCommands::testSignInput))
			.then(literal("parsecurrency")
				.then(argument("input", StringArgumentType.greedyString())
					.executes(DevelopmentTestCommands::parseCurrency))));

	public static int testSignInput(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		TaskedSignGui
			.prompt(player,
				Text.empty(),
				Text.literal("^^^^^^^^^^^^"),
				Text.literal("Enter your input"))
			.thenApply(sign -> sign.getLine(0).getLiteralString())
			.thenAccept(input -> ctx.getSource().sendMessage(Text.literal("You just entered %s".formatted(input))));
		return 0;
	}

	private static final SimpleCommandExceptionType NO_ECONOMY_SERVICE = new SimpleCommandExceptionType(Text
		.literal("Economy service is not available"));
	private static final DynamicCommandExceptionType CURRENCY_PARSE_ERROR = new DynamicCommandExceptionType(t -> Text
		.literal("Error while parsing currency: %s".formatted(t)));

	public static int parseCurrency(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		String input = StringArgumentType.getString(ctx, "input");
		StonksMcInstance instance = StonksMod.getInstance(ctx.getSource().getServer());
		EconomyService economy = instance.getEconomyService();
		if (economy == null) throw NO_ECONOMY_SERVICE.create();

		try {
			long value = economy.parseCurrency(input);
			ctx.getSource().sendFeedback(() -> Text.literal("Value is %d".formatted(value)), false);
			return (int) value;
		} catch (IllegalArgumentException e) {
			throw CURRENCY_PARSE_ERROR.create(e.getLocalizedMessage());
		}
	}
}
