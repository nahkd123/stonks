/*
 * Copyright (c) 2023-2024 nahkd
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package stonks.fabric.command;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

import java.net.URI;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.ChatFormatting;
import stonks.fabric.StonksFabric;

public class StonksCommand {
	public static final LiteralArgumentBuilder<CommandSourceStack> ROOT = literal("stonks")
		.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
		.then(subcommand$about())
		.then(subcommand$give())
		.then(subcommand$inspect())
		.then(subcommand$category());

	public static LiteralArgumentBuilder<CommandSourceStack> subcommand$about() {
		var meta = FabricLoader.getInstance().getModContainer(StonksFabric.MODID).get().getMetadata();
		var bulletPoint = Component.literal(" - ").withStyle(s -> s.withColor(ChatFormatting.GRAY));

		return literal("about").executes(ctx -> {
			var src = ctx.getSource();
			src.sendSystemMessage(Component.empty());
			src.sendSystemMessage(Component.literal(" Stonks2").withStyle(s -> s.withColor(ChatFormatting.AQUA))
				.append(Component.literal(" for ").withStyle(s -> s.withColor(ChatFormatting.GRAY)))
				.append(Component.literal("Fabric").withStyle(s -> s.withColor(ChatFormatting.YELLOW))));
			src.sendSystemMessage(Component.literal(" Version ").withStyle(s -> s.withColor(ChatFormatting.GRAY))
				.append(Component.literal(meta.getVersion().getFriendlyString()).withStyle(s -> s.withColor(ChatFormatting.AQUA))));
			src.sendSystemMessage(Component.literal(" ")
				.append(makeLinkBtn("GitHub", ChatFormatting.WHITE, URI.create("https://github.com/nahkd123/stonks")))
				.append(makeLinkBtn("Issues", ChatFormatting.AQUA, URI.create("https://github.com/nahkd123/stonks/issues")))
				.append(makeLinkBtn("Wiki", ChatFormatting.YELLOW, URI.create("https://github.com/nahkd123/stonks/wiki"))));
			src.sendSystemMessage(Component.empty());
			src.sendSystemMessage(Component.literal(" Special thanks:"));
			src.sendSystemMessage(Component.empty()
				.withStyle(s -> s.withColor(ChatFormatting.WHITE))
				.append(bulletPoint).append("The Fabric Project ")
				.append(makeLinkBtn("Homepage", ChatFormatting.YELLOW, URI.create("https://fabricmc.net/")))
				.append(makeLinkBtn("GitHub", ChatFormatting.WHITE, URI.create("https://github.com/fabricMC"))));
			src.sendSystemMessage(Component.empty()
				.withStyle(s -> s.withColor(ChatFormatting.WHITE))
				.append(bulletPoint).append("Patbox ")
				.append(makeLinkBtn("Homepage", ChatFormatting.YELLOW, URI.create("https://pb4.eu/")))
				.append(makeLinkBtn("sgui", ChatFormatting.AQUA, URI.create("https://github.com/Patbox/sgui")))
				.append(makeLinkBtn("Common Economy API", ChatFormatting.AQUA,
					URI.create("https://github.com/Patbox/common-economy-api"))));
			src.sendSystemMessage(Component.empty()
				.withStyle(s -> s.withColor(ChatFormatting.WHITE))
				.append(bulletPoint).append("You! Thanks for using my mod!"));
			src.sendSystemMessage(Component.empty());
			return 1;
		});
	}

	private static Component makeLinkBtn(String name, ChatFormatting color, URI url) {
		return Component.literal("[")
			.withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY))
			.append(Component.literal(name).withStyle(s -> s
				.withColor(color)
				.withClickEvent(new ClickEvent.OpenUrl(url))
				.withHoverEvent(new HoverEvent.ShowText(Component.literal(url.toString())))))
			.append("] ");
	}

	public static LiteralArgumentBuilder<CommandSourceStack> subcommand$category() {
		return literal("category")
			.then(argument("id", StringArgumentType.string()).executes(ctx -> viewCategory(ctx)))
			.executes(ctx -> viewAllCategories(ctx));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> subcommand$give() {
		return literal("give")
			.then(argument("players", EntityArgument.players())
				.then(argument("id", StringArgumentType.string())
					.suggests((conComponent, builder) -> {
						return StonksFabric.getPlatform(conComponent.getSource().getServer())
							.getStonksCache()
							.getAllCategories()
							.thenApply(list -> {
								list
									.stream()
									.flatMap(category -> category.getProducts().stream())
									.forEach(product -> builder.suggest(product.getProductId()));
								return builder.build();
							});
					})
					.then(argument("amount", IntegerArgumentType.integer(0))
						.executes(ctx -> giveProducts(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))
					.executes(ctx -> giveProducts(ctx, 1))));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> subcommand$inspect() {
		return literal("inspect")
			.then(argument("players", EntityArgument.players())
				.executes(ctx -> {
					var players = EntityArgument.getPlayers(ctx, "players");
					var economy = StonksFabric.getPlatform(ctx.getSource().getServer()).getEconomySystem();

					for (var p : players) {
						ctx.getSource().sendSystemMessage(Component.literal("Inspecting ").append(p.getDisplayName()).append(":"));

						ctx.getSource().sendSystemMessage(Component.literal(" - ")
							.withStyle(s -> s.withColor(ChatFormatting.GRAY))
							.append(Component.literal("Account Balance: ").withStyle(s -> s.withColor(ChatFormatting.WHITE)))
							.append(economy.formatAsDisplay(economy.balanceOf(p))));
					}
					return 1;
				}));
	}

	private static int giveProducts(CommandContext<CommandSourceStack> ctx, int amount) throws CommandSyntaxException {
		var players = EntityArgument.getPlayers(ctx, "players");
		var id = StringArgumentType.getString(ctx, "id");
		var provider = StonksFabric.getPlatform(ctx.getSource().getServer());
		var cache = provider.getStonksCache();
		var adapter = provider.getStonksAdapter();
		cache
			.getAllCategories()
			.thenAccept(categories -> {
				var product = categories.stream()
					.flatMap(category -> category.getProducts().stream())
					.filter(v -> v.getProductId().equals(id))
					.findFirst();

				if (product.isPresent()) {
					ctx.getSource().getServer().execute(() -> {
						for (var p : players) {
							adapter.addUnitsTo(p, product.get(), amount);
							ctx.getSource().sendSuccess(() -> Component.literal("Gave ")
								.append(p.getDisplayName())
								.append(" " + amount + "x " + product.get().getProductName()), true);
						}
					});
				} else {
					ctx.getSource().sendFailure(Component.literal("Product not found: " + id)
						.withStyle(s -> s.withColor(ChatFormatting.RED)));
				}
			});
		return 1;
	}

	private static int viewCategory(CommandContext<CommandSourceStack> ctx) {
		var id = StringArgumentType.getString(ctx, "id");
		var cache = StonksFabric.getPlatform(ctx.getSource().getServer()).getStonksCache();
		cache
			.getCategoryById(id)
			.thenAccept(category -> {
				if (category == null) {
					ctx.getSource().sendFailure(Component.literal("Unknown category with ID " + id));
					return;
				}

				ctx.getSource().sendSystemMessage(Component.literal("Category: ")
					.append(Component.literal(category.getCategoryName())
						.withStyle(s -> s.withColor(ChatFormatting.AQUA)))
					.append(Component.literal(" (" + category.getCategoryId() + ")")
						.withStyle(s -> s.withColor(ChatFormatting.GRAY))));

				for (var product : category.getProducts()) {
					ctx.getSource().sendSystemMessage(Component.literal(" - ")
						.withStyle(s -> s.withColor(ChatFormatting.GRAY))
						.withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component
							.literal("Click to fill in your chatbox")
							.withStyle(s1 -> s1.withColor(ChatFormatting.AQUA)))))
						.withStyle(
							s -> s.withClickEvent(
								new ClickEvent.SuggestCommand("/stonks product " + product.getProductId())))
						.append(Component.literal(product.getProductName())
							.withStyle(s -> s.withColor(ChatFormatting.WHITE)))
						.append(Component.literal(" (" + product.getProductId() + ")")
							.withStyle(s -> s.withColor(ChatFormatting.GRAY))));
				}
			});
		return 1;
	}

	private static int viewAllCategories(CommandContext<CommandSourceStack> ctx) {
		var cache = StonksFabric.getPlatform(ctx.getSource().getServer()).getStonksCache();
		cache
			.getAllCategories()
			.thenAccept(categories -> {
				ctx.getSource().sendSystemMessage(Component.literal(categories.size() + " "
					+ (categories.size() == 1 ? "category" : "categories") + (categories.size() > 0 ? ":" : "")));
				for (var cat : categories) {
					ctx.getSource().sendSystemMessage(Component.literal(" - ")
						.withStyle(s -> s.withColor(ChatFormatting.GRAY))
						.withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component
							.literal("Click to fill in your chatbox")
							.withStyle(s1 -> s1.withColor(ChatFormatting.AQUA)))))
						.withStyle(
							s -> s.withClickEvent(
								new ClickEvent.SuggestCommand("/stonks category " + cat.getCategoryId())))
						.append(Component.literal(cat.getCategoryName())
							.withStyle(s -> s.withColor(ChatFormatting.WHITE)))
						.append(Component.literal(" (" + cat.getCategoryId() + ")")
							.withStyle(s -> s.withColor(ChatFormatting.GRAY))));
				}
			});
		return 1;
	};
}
