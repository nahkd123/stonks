/*
 * Copyright (c) 2023 nahkd
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

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import stonks.fabric.StonksFabric;
import stonks.fabric.StonksFabricUtils;

public class StonksCommand {
	public static final LiteralArgumentBuilder<ServerCommandSource> ROOT = literal("stonks")
		.requires(s -> s.hasPermissionLevel(CommandManager.field_31840))
		.then(subcommand$about())
		.then(subcommand$give())
		.then(subcommand$inspect())
		.then(subcommand$category());

	public static LiteralArgumentBuilder<ServerCommandSource> subcommand$about() {
		var meta = FabricLoader.getInstance().getModContainer(StonksFabric.MODID).get().getMetadata();
		var bulletPoint = Text.literal(" - ").styled(s -> s.withColor(Formatting.GRAY));

		return literal("about").executes(ctx -> {
			var src = ctx.getSource();
			src.sendMessage(Text.empty());
			src.sendMessage(Text.literal(" Stonks2").styled(s -> s.withColor(Formatting.AQUA))
				.append(Text.literal(" for ").styled(s -> s.withColor(Formatting.GRAY)))
				.append(Text.literal("Fabric").styled(s -> s.withColor(Formatting.YELLOW))));
			src.sendMessage(Text.literal(" Version ").styled(s -> s.withColor(Formatting.GRAY))
				.append(Text.literal(meta.getVersion().getFriendlyString()).styled(s -> s.withColor(Formatting.AQUA))));
			src.sendMessage(Text.literal(" ")
				.append(makeLinkBtn("GitHub", Formatting.WHITE, "https://github.com/nahkd123/stonks"))
				.append(makeLinkBtn("Issues", Formatting.AQUA, "https://github.com/nahkd123/stonks/issues"))
				.append(makeLinkBtn("Wiki", Formatting.YELLOW, "https://github.com/nahkd123/stonks/wiki")));
			src.sendMessage(Text.empty());
			src.sendMessage(Text.literal(" Special thanks:"));
			src.sendMessage(Text.empty()
				.styled(s -> s.withColor(Formatting.WHITE))
				.append(bulletPoint).append("The Fabric Project ")
				.append(makeLinkBtn("Homepage", Formatting.YELLOW, "https://fabricmc.net/"))
				.append(makeLinkBtn("GitHub", Formatting.WHITE, "https://github.com/fabricMC")));
			src.sendMessage(Text.empty()
				.styled(s -> s.withColor(Formatting.WHITE))
				.append(bulletPoint).append("Patbox ")
				.append(makeLinkBtn("Homepage", Formatting.YELLOW, "https://pb4.eu/"))
				.append(makeLinkBtn("sgui", Formatting.AQUA, "https://github.com/Patbox/sgui"))
				.append(makeLinkBtn("Common Economy API", Formatting.AQUA,
					"https://github.com/Patbox/common-economy-api")));
			src.sendMessage(Text.empty()
				.styled(s -> s.withColor(Formatting.WHITE))
				.append(bulletPoint).append("You! Thanks for using my mod!"));
			src.sendMessage(Text.empty());
			return 1;
		});
	}

	private static Text makeLinkBtn(String name, Formatting color, String url) {
		return Text.literal("[")
			.styled(s -> s.withColor(Formatting.DARK_GRAY))
			.append(Text.literal(name).styled(s -> s
				.withColor(color)
				.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(url)))))
			.append("] ");
	}

	public static LiteralArgumentBuilder<ServerCommandSource> subcommand$category() {
		return literal("category")
			.then(argument("id", StringArgumentType.string()).executes(ctx -> viewCategory(ctx)))
			.executes(ctx -> viewAllCategories(ctx));
	}

	private static LiteralArgumentBuilder<ServerCommandSource> subcommand$give() {
		return literal("give")
			.then(argument("players", EntityArgumentType.players())
				.then(argument("id", StringArgumentType.string())
					.suggests((context, builder) -> {
						var cache = StonksFabric.getServiceProvider(context.getSource().getServer()).getStonksCache();
						return CompletableFuture.supplyAsync(() -> {
							try {
								cache.getAllCategories()
									.await()
									.stream()
									.flatMap(v -> v.getProducts().stream())
									.forEach(v -> builder.suggest(v.getProductId()));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							return builder.build();
						});
					})
					.then(argument("amount", IntegerArgumentType.integer(0))
						.executes(ctx -> giveProducts(ctx, IntegerArgumentType.getInteger(ctx, "amount"))))
					.executes(ctx -> giveProducts(ctx, 1))));
	}

	private static LiteralArgumentBuilder<ServerCommandSource> subcommand$inspect() {
		return literal("inspect")
			.then(argument("players", EntityArgumentType.players())
				.executes(ctx -> {
					var players = EntityArgumentType.getPlayers(ctx, "players");
					var adapter = StonksFabric.getServiceProvider(ctx.getSource().getServer()).getStonksAdapter();

					for (var p : players) {
						ctx.getSource().sendMessage(Text.literal("Inspecting ").append(p.getDisplayName()).append(":"));

						var balance = adapter.accountBalance(p);
						ctx.getSource().sendMessage(Text.literal(" - ")
							.styled(s -> s.withColor(Formatting.GRAY))
							.append(Text.literal("Account Balance: ").styled(s -> s.withColor(Formatting.WHITE)))
							.append(StonksFabricUtils.currencyText(Optional.of(balance), true)));
					}
					return 1;
				}));
	}

	private static int giveProducts(CommandContext<ServerCommandSource> ctx, int amount) throws CommandSyntaxException {
		var players = EntityArgumentType.getPlayers(ctx, "players");
		var id = StringArgumentType.getString(ctx, "id");
		var provider = StonksFabric.getServiceProvider(ctx.getSource().getServer());
		var cache = provider.getStonksCache();
		var adapter = provider.getStonksAdapter();
		cache
			.getAllCategories()
			.afterThatDo(categories -> {
				var product = categories.stream()
					.flatMap(category -> category.getProducts().stream())
					.filter(v -> v.getProductId().equals(id))
					.findFirst();

				if (product.isPresent()) {
					for (var p : players) {
						adapter.addUnitsTo(p, product.get(), amount);
						ctx.getSource().sendFeedback(() -> Text.literal("Gave ")
							.append(p.getDisplayName())
							.append(" " + amount + "x " + product.get().getProductName()), true);
					}
				} else {
					ctx.getSource().sendError(Text.literal("Product not found: " + id)
						.styled(s -> s.withColor(Formatting.RED)));
				}
			});
		return 1;
	}

	private static int viewCategory(CommandContext<ServerCommandSource> ctx) {
		var id = StringArgumentType.getString(ctx, "id");
		var cache = StonksFabric.getServiceProvider(ctx.getSource().getServer()).getStonksCache();
		cache
			.getCategoryById(id)
			.afterThatDo(category -> {
				if (category == null) {
					ctx.getSource().sendError(Text.literal("Unknown category with ID " + id));
					return;
				}

				ctx.getSource().sendMessage(Text.literal("Category: ")
					.append(Text.literal(category.getCategoryName())
						.styled(s -> s.withColor(Formatting.AQUA)))
					.append(Text.literal(" (" + category.getCategoryId() + ")")
						.styled(s -> s.withColor(Formatting.GRAY))));

				for (var product : category.getProducts()) {
					ctx.getSource().sendMessage(Text.literal(" - ")
						.styled(s -> s.withColor(Formatting.GRAY))
						.styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text
							.literal("Click to fill in your chatbox")
							.styled(s1 -> s1.withColor(Formatting.AQUA)))))
						.styled(
							s -> s.withClickEvent(
								new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/stonks product "
									+ product.getProductId())))
						.append(Text.literal(product.getProductName())
							.styled(s -> s.withColor(Formatting.WHITE)))
						.append(Text.literal(" (" + product.getProductId() + ")")
							.styled(s -> s.withColor(Formatting.GRAY))));
				}
			});
		return 1;
	}

	private static int viewAllCategories(CommandContext<ServerCommandSource> ctx) {
		var cache = StonksFabric.getServiceProvider(ctx.getSource().getServer()).getStonksCache();
		cache
			.getAllCategories()
			.afterThatDo(categories -> {
				ctx.getSource().sendMessage(Text.literal(categories.size() + " "
					+ (categories.size() == 1 ? "category" : "categories") + (categories.size() > 0 ? ":" : "")));
				for (var cat : categories) {
					ctx.getSource().sendMessage(Text.literal(" - ")
						.styled(s -> s.withColor(Formatting.GRAY))
						.styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text
							.literal("Click to fill in your chatbox")
							.styled(s1 -> s1.withColor(Formatting.AQUA)))))
						.styled(
							s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/stonks category "
								+ cat.getCategoryId())))
						.append(Text.literal(cat.getCategoryName())
							.styled(s -> s.withColor(Formatting.WHITE)))
						.append(Text.literal(" (" + cat.getCategoryId() + ")")
							.styled(s -> s.withColor(Formatting.GRAY))));
				}
			});
		return 1;
	};
}
