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
package io.github.nahkd123.stonks.market.impl.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import io.github.nahkd123.stonks.market.OrderInfo;
import io.github.nahkd123.stonks.market.helper.MutableOrder;
import io.github.nahkd123.stonks.market.result.InstantSellResult;
import io.github.nahkd123.stonks.market.summary.ProductSummary;
import io.github.nahkd123.stonks.test.SqlTestUtils;
import io.github.nahkd123.stonks.test.TestCatalogue;

class SqlMarketServiceTest {
	Connection newDatabase() throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
		conn.setAutoCommit(false);
		return conn;
	}

	@Test
	void testInsert() throws SQLException {
		Connection db = newDatabase();
		TestCatalogue catalogue = new TestCatalogue();
		SqlMarketService service = new SqlMarketService(catalogue, db);

		UUID user = UUID.randomUUID();
		service.updateOrder(new MutableOrder(id -> catalogue.getProductById(id).orElse(null), UUID
			.randomUUID(), user, catalogue.apple.getId(), true, 100, 50, 0, 0));
		service.updateOrder(new MutableOrder(id -> catalogue.getProductById(id).orElse(null), UUID
			.randomUUID(), user, catalogue.rice.getId(), true, 200, 50, 0, 0));

		assertTrue(service.createIterator(catalogue.apple, true).hasNext());
		assertTrue(service.createIterator(catalogue.rice, true).hasNext());
		assertFalse(service.createIterator(catalogue.apple, false).hasNext());
		assertFalse(service.createIterator(catalogue.rice, false).hasNext());

		Statement s = db.createStatement();
		SqlTestUtils.printResultSet(s.executeQuery("SELECT * FROM orders"));
	}

	@Test
	void testInstantSell() throws SQLException, InterruptedException, ExecutionException, SQLException {
		Connection db = newDatabase();
		TestCatalogue catalogue = new TestCatalogue();
		SqlMarketService service = new SqlMarketService(catalogue, db);

		UUID userA = UUID.randomUUID();
		UUID userB = UUID.randomUUID();

		// Place buy orders
		OrderInfo buyOrder = service.makeBuyOrder(userA, catalogue.apple, 100, 20).get().data();
		assertEquals(100, buyOrder.getTotalUnits());
		assertEquals(20, buyOrder.getPricePerUnit());

		buyOrder = service.makeBuyOrder(userB, catalogue.apple, 150, 10).get().data();
		assertEquals(150, buyOrder.getTotalUnits());
		assertEquals(10, buyOrder.getPricePerUnit());

		buyOrder = service.makeBuyOrder(userB, catalogue.apple, 250, 20).get().data();
		assertEquals(250, buyOrder.getTotalUnits());
		assertEquals(20, buyOrder.getPricePerUnit());

		// Get summary
		ProductSummary summary = service.querySummary(catalogue.apple).get().data();
		assertEquals(2, summary.getBuySummary().size());
		assertEquals(0, summary.getSellSummary().size());
		assertEquals(350, summary.getBuySummary().get(0).totalUnits());
		assertEquals(20, summary.getBuySummary().get(0).pricePerUnit());
		assertEquals(150, summary.getBuySummary().get(1).totalUnits());
		assertEquals(10, summary.getBuySummary().get(1).pricePerUnit());

		// Perform instant sell
		InstantSellResult sellResult = service.instantSell(userB, catalogue.rice, 100).get().data();
		assertEquals(100, sellResult.getLeftoverAmount());
		assertEquals(0, sellResult.getCollectedBalance());

		sellResult = service.instantSell(userB, catalogue.apple, 150).get().data();
		assertEquals(0, sellResult.getLeftoverAmount());
		assertEquals(150 * 20, sellResult.getCollectedBalance());

		sellResult = service.instantSell(userB, catalogue.apple, 500).get().data();
		assertEquals(500 - 350, sellResult.getLeftoverAmount());
		assertEquals(200 * 20 + 150 * 10, sellResult.getCollectedBalance());

		Statement s = db.createStatement();
		SqlTestUtils.printResultSet(s.executeQuery("SELECT * FROM orders"));
	}
}
