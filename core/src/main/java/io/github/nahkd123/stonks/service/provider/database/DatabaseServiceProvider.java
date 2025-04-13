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

import com.google.auto.service.AutoService;

import io.github.nahkd123.stonks.service.MarketService;
import io.github.nahkd123.stonks.service.ServiceConfig;
import io.github.nahkd123.stonks.service.provider.MarketServiceHost;
import io.github.nahkd123.stonks.service.provider.MarketServiceProvider;
import io.github.nahkd123.stonks.service.provider.database.DatabaseServiceProvider.Config;
import io.github.nahkd123.stonks.utils.OneOf;
import io.github.nahkd123.stonks.utils.dynamic.DynamicCodec;
import io.github.nahkd123.stonks.utils.dynamic.DynamicCodec.ObjectField;
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
	public DynamicCodec<Config> getConfigCodec() {
		DynamicCodec<Config> baseCompound = DynamicCodec.object(Config::new, Map.of(
			"url", new ObjectField<>(DynamicCodec.STRING, c -> c.url, (c, v) -> c.url = v),
			"username", new ObjectField<>(DynamicCodec.STRING, c -> c.username, (c, v) -> c.username = v),
			"password", new ObjectField<>(DynamicCodec.STRING, c -> c.password, (c, v) -> c.password = v),
			"backupOnMigrate",
			new ObjectField<>(DynamicCodec.BOOLEAN, c -> c.backupOnMigrate, (c, v) -> c.backupOnMigrate = v)));
		return baseCompound.or(DynamicCodec.STRING.map(url->{Config c=new Config();c.url=url;return c;},null)).map(oneOf->switch(oneOf){case OneOf.First(Config c)->c;case OneOf.Second(Config c)->c;default->throw new IllegalArgumentException("Unexpected value: "+oneOf);},c->new OneOf.First<>(c));
	}

	@Override
	public MarketServiceHost createHost(Config config) {
		if (config == null) throw new IllegalArgumentException("Configuration must be provided");
		if (config.url == null) throw new IllegalArgumentException("Missing 'url' property");
		return new Host(config);
	}

	class Config {
		String url, username, password;
		boolean backupOnMigrate = false;
	}

	class Host implements MarketServiceHost {
		private DatabaseMarketService service;

		public Host(Config config) {
			this.service = new DatabaseMarketService(() -> {
				Connection sql = config.username != null
					? DriverManager.getConnection(config.url, config.username, config.password)
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
