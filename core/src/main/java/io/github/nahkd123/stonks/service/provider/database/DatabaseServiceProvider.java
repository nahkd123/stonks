/*
 * Copyright (c) 2023-2025 nahkd
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
package io.github.nahkd123.stonks.service.provider.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Optional;

import com.google.auto.service.AutoService;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.ServiceConfig;
import io.github.nahkd123.stonks.service.provider.MarketServiceHost;
import io.github.nahkd123.stonks.service.provider.MarketServiceProvider;
import io.github.nahkd123.stonks.service.provider.database.DatabaseServiceProvider.Config;
import io.github.nahkd123.tableschema.jdbc.JdbcDatabase;

/**
 * <p>
 * Database service provider that provides {@link DatabaseMarketService} upon
 * creating service. Following is the example configuration in JSON (objects are
 * {@link Map}).
 * </p>
 * {@snippet :
 * {
 * 	"url": "jdbc:&lt;subprotocol&gt;:&lt;subname&gt;",
 * 	"username": "Optional username for authentication",
 * 	"password": "Password for authentication",
 * 	"backupOnMigrate": false
 * }
 * }
 * '
 * <p>
 * If there is no need for authentication or any other options, you can use just
 * string alone for database URL.
 * </p>
 * {@snippet :
 * "jdbc:&lt;subprotocol&gt;:&lt;subname&gt;"
 * }
 */
@AutoService(MarketServiceProvider.class)
public class DatabaseServiceProvider implements MarketServiceProvider<Config> {
	@Override
	public String getProviderName() { return "database"; }

	@Override
	public Codec<Config> getConfigCodec() { return Config.CODEC; }

	@Override
	public MarketServiceHost createHost(Config config) {
		if (config == null) throw new IllegalArgumentException("Configuration must be provided");
		if (config.url == null) throw new IllegalArgumentException("Missing 'url' property");
		return new Host(config);
	}

	record Config(String url, Optional<String> username, Optional<String> password, boolean backupOnMigrate) {
		static final Codec<Config> STRING_CODEC = Codec.STRING.xmap(
			s -> new Config(s, Optional.empty(), Optional.empty(), false),
			Config::url);
		static final MapCodec<Config> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("url").forGetter(Config::url),
			Codec.STRING.optionalFieldOf("username").forGetter(Config::username),
			Codec.STRING.optionalFieldOf("password").forGetter(Config::password),
			Codec.BOOL.optionalFieldOf("backupOnMigrate", false).forGetter(Config::backupOnMigrate))
			.apply(i, Config::new));
		static final Codec<Config> CODEC = Codec.either(STRING_CODEC, MAP_CODEC.codec()).xmap(
			e -> e.left().or(e::right).get(),
			c -> c.username.isEmpty() && c.password.isEmpty() && !c.backupOnMigrate
				? Either.left(c)
				: Either.right(c));
	}

	class Host implements MarketServiceHost {
		private DatabaseMarketService service;

		public Host(Config config) {
			this.service = new DatabaseMarketService(() -> {
				Connection sql = config.username.isPresent()
					? DriverManager.getConnection(config.url, config.username.get(), config.password.get())
					: DriverManager.getConnection(config.url);
				return new JdbcDatabase(sql);
			}, new ServiceConfig(false, 5), config.backupOnMigrate
				? new DatabaseServiceOption[] { DatabaseServiceOption.Standard.BACKUP_ON_MIGRATE }
				: new DatabaseServiceOption[0]);
		}

		@Override
		public MarketService getService() { return service; }

		@Override
		public void startService() {
			service.startServiceThread().join();
		}

		@Override
		public void stopService() {
			service.interrupt();
			service.getStopTask().join();
		}
	}
}
