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
package io.github.nahkd123.stonks.minecraft.economy;

import java.util.UUID;

public interface Transaction {
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_FAIL_UNKNOWN = -1;
	public static final int CODE_FAIL_INSUFFICIENT_BALANCE = 1;

	public UUID getPlayerUuid();

	/**
	 * <p>
	 * Get the amount of money involved in this transaction.
	 * </p>
	 * 
	 * @return The amount of money.
	 */
	public long getAmount();

	public long getNewBalance();

	public TransactionType getType();

	public boolean isSuccess();

	/**
	 * <p>
	 * Get the error code for this transaction. Negative value means the error is
	 * not known to Stonks and should use the message from
	 * {@link #getErrorMessage()} instead. Positive value means the error is known
	 * to Stonks and will use the localized message. See static fields of
	 * {@link Transaction} for codes.
	 * </p>
	 * 
	 * @return The error code.
	 * @see #CODE_SUCCESS
	 * @see #CODE_FAIL_UNKNOWN
	 * @see #CODE_FAIL_INSUFFICIENT_BALANCE
	 */
	public int getCode();

	/**
	 * <p>
	 * The optional error message if {@link #isSuccess()} is false. This will be
	 * used by Stonks if {@link #getCode()} returns negative value or Stonks can't
	 * find localized message.
	 * </p>
	 * 
	 * @return The error message.
	 */
	public String getErrorMessage();
}
