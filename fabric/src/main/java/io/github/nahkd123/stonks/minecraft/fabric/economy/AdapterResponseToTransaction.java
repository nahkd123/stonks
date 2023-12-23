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
package io.github.nahkd123.stonks.minecraft.fabric.economy;

import java.util.UUID;

import io.github.nahkd123.stonks.minecraft.economy.Transaction;
import io.github.nahkd123.stonks.minecraft.economy.TransactionType;
import stonks.fabric.adapter.AdapterResponse;

public class AdapterResponseToTransaction<T> implements Transaction {
	private UUID uuid;
	private AdapterResponse<T> response;
	private long amount;
	private long newBalance;
	private TransactionType type;

	public AdapterResponseToTransaction(UUID uuid, AdapterResponse<T> response, TransactionType type, long amount, long newBalance) {
		this.uuid = uuid;
		this.response = response;
		this.type = type;
		this.amount = amount;
		this.newBalance = newBalance;
	}

	@Override
	public UUID getPlayerUuid() { return uuid; }

	@Override
	public long getAmount() { return amount; }

	@Override
	public long getNewBalance() { return newBalance; }

	@Override
	public TransactionType getType() { return type; }

	@Override
	public boolean isSuccess() { return response.isSuccess(); }

	@Override
	public int getCode() { return response.isFailed() ? CODE_FAIL_UNKNOWN : CODE_SUCCESS; }

	@Override
	public String getErrorMessage() { return response.message().getString(); }
}
