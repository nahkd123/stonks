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
public class DatabaseServiceProvider implements MarketServiceProvider {
	@Override
	public String getProviderName() { return "database"; }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public MarketServiceHost createHost(Object config) {
		String url = null;
		String username = null;
		String password = null;
		boolean backupOnMigrate = false;

		switch (config) {
		case String url0:
			url = url0;
			break;
		case Map complex:
			url = (String) complex.get("url");
			username = (String) complex.get("username");
			password = (String) complex.get("password");
			backupOnMigrate = (Boolean) complex.getOrDefault("backupOnMigrate", false);
			break;
		default:
			throw new IllegalArgumentException("Unable to resolve config object: %s".formatted(config));
		}

		if (url == null) throw new IllegalArgumentException("Missing 'url'");
		return new Host(url, username, password, backupOnMigrate);
	}

	class Host implements MarketServiceHost {
		private DatabaseMarketService service;

		public Host(String url, String username, String password, boolean backupOnMigrate) {
			this.service = new DatabaseMarketService(() -> {
				Connection sql = username != null
					? DriverManager.getConnection(url, username, password)
					: DriverManager.getConnection(url);
				return new JdbcDatabase(sql);
			}, new ServiceConfig(false, 0), backupOnMigrate
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
		}
	}
}
