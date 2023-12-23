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
package io.github.nahkd123.stonks.minecraft.fabric.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.nahkd123.stonks.minecraft.MinecraftPlatform;
import io.github.nahkd123.stonks.minecraft.fabric.FabricServer;
import io.github.nahkd123.stonks.minecraft.fabric.text.TextProxy;
import io.github.nahkd123.stonks.minecraft.gui.provided.gui.MainContainerGui;
import io.github.nahkd123.stonks.minecraft.text.FriendlyParser;
import io.github.nahkd123.stonks.minecraft.text.TextFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class DevelopmentCommand {
	public static final LiteralArgumentBuilder<ServerCommandSource> ROOT = literal("stonksdev")
		.requires(s -> s.hasPermissionLevel(CommandManager.field_31841))
		.then(literal("test")
			.then(literal("friendlyparser").executes(context -> {
				FabricServer server = FabricServer.of(context.getSource().getServer());
				MinecraftPlatform platform = server.getPlatform();
				TextFactory text = platform.getTextFactory();

				for (String line : new String[] {
					"Plain text no styles",
					"[b]Plain text bold[/]",
					"[color:red]Mixed[/] styles[b]![/]",
					"Mixed [i]styles[/]!"
				}) {
					context.getSource()
						.sendMessage(((TextProxy) FriendlyParser.parse(text, line, null)).getUnderlying());
				}
				return 1;
			})))
		.then(literal("openmoderngui").executes(context -> {
			ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
			FabricServer server = FabricServer.of(context.getSource().getServer());
			server.getPlayerByUUID(player.getUuid()).openGui(new MainContainerGui(null, server, 0));
			return 1;
		}));
}
