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
package io.github.nahkd123.stonks.market.helper;

import java.util.UUID;

import io.github.nahkd123.stonks.market.result.InstantBuyResult;
import io.github.nahkd123.stonks.market.result.InstantSellResult;
import io.github.nahkd123.stonks.market.result.MutableInstantBuyResult;
import io.github.nahkd123.stonks.market.result.MutableInstantSellResult;

public class InstantOrdersProcessor {
	public static InstantBuyResult instantBuy(OrdersIterator iter, UUID user, long requested, long initialBalance) {
		MutableInstantBuyResult result = new MutableInstantBuyResult(user, iter
			.getProduct(), requested, initialBalance);

		while (iter.hasNext() && result.getPendingAmount() > 0L) {
			MutableOrder order = iter.next().asMutable(); // Sell offers - lowest
			if (order.getFilledUnits() == order.getTotalUnits()) continue;

			long available = order.getTotalUnits() - order.getFilledUnits();
			long canBuy = result.getNewBalance() / order.getPricePerUnit();
			long unitsBought = Math.min(available, canBuy);
			long moneySpent = unitsBought * order.getPricePerUnit();
			if (unitsBought == 0L) return result;

			order.setFilledUnits(order.getFilledUnits() + unitsBought);
			result.setBoughtAmount(result.getBoughtAmount() + unitsBought);
			result.setNewBalance(result.getNewBalance() - moneySpent);
			iter.update(order);
		}

		return result;
	}

	public static InstantSellResult instantSell(OrdersIterator iter, UUID user, long requested) {
		MutableInstantSellResult result = new MutableInstantSellResult(user, iter.getProduct(), requested);

		while (iter.hasNext() && result.getLeftoverAmount() > 0L) {
			MutableOrder order = iter.next().asMutable(); // Buy offers - highest
			if (order.getFilledUnits() == order.getTotalUnits()) continue;

			long available = order.getTotalUnits() - order.getFilledUnits();
			long unitsSold = Math.min(available, result.getLeftoverAmount());
			long moneyEarn = unitsSold * order.getPricePerUnit();
			if (unitsSold == 0L) return result;

			order.setFilledUnits(order.getFilledUnits() + unitsSold);
			result.setLeftoverAmount(result.getLeftoverAmount() - unitsSold);
			result.setCollectedBalance(result.getCollectedBalance() + moneyEarn);
			iter.update(order);
		}

		return result;
	}
}
