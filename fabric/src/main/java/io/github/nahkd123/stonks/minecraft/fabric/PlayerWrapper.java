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
package io.github.nahkd123.stonks.minecraft.fabric;

import java.util.UUID;

import io.github.nahkd123.stonks.minecraft.MinecraftServer;
import io.github.nahkd123.stonks.minecraft.Player;
import io.github.nahkd123.stonks.minecraft.fabric.gui.ContainerGuiFrontend;
import io.github.nahkd123.stonks.minecraft.fabric.gui.template.GuiTemplate;
import io.github.nahkd123.stonks.minecraft.fabric.text.TextProxy;
import io.github.nahkd123.stonks.minecraft.gui.ContainerGui;
import io.github.nahkd123.stonks.minecraft.text.TextComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import stonks.fabric.StonksFabricPlatform;

public class PlayerWrapper implements Player {
	private net.minecraft.server.MinecraftServer server;
	private UUID uuid;

	public PlayerWrapper(net.minecraft.server.MinecraftServer server, UUID uuid) {
		this.server = server;
		this.uuid = uuid;
	}

	@Override
	public MinecraftServer getServer() { return ((StonksFabricPlatform) server).getModernWrapper(); }

	@Override
	public UUID getUuid() { return uuid; }

	public ServerPlayerEntity getEntity() { return server.getPlayerManager().getPlayer(uuid); }

	@Override
	public String getUsername() { return getEntity().getName().getString(); }

	@Override
	public TextComponent getDisplayName() {
		ServerPlayerEntity player = getEntity();
		return new TextProxy(player.getDisplayName() instanceof MutableText m ? m : Text.literal(getUsername()));
	}

	@Override
	public void sendSystemMessage(TextComponent message) {
		if (message instanceof TextProxy proxy) getEntity().sendMessage(proxy.getUnderlying(), false);
	}

	@Override
	public void sendActionBarMessage(TextComponent message) {
		if (message instanceof TextProxy proxy) getEntity().sendMessage(proxy.getUnderlying(), true);
	}

	@Override
	public void openGui(ContainerGui gui) {
		if (gui == null) {
			getEntity().closeHandledScreen();
			return;
		}

		GuiTemplate template = FabricServer.of(server).getGuiTemplate(gui);
		ContainerGuiFrontend frontend = new ContainerGuiFrontend(getEntity(), false, gui, template);
		frontend.open();
	}
}
