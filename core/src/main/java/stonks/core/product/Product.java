/*
 * Copyright (c) 2023-2024 nahkd
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
package stonks.core.product;

import stonks.core.dynamic.Dynamic;
import stonks.core.dynamic.DynamicPrimitive;

public interface Product {
	public Category getCategory();

	public String getProductId();

	/**
	 * <p>
	 * Get the display name of the product. This will be displayed in GUIs.
	 * </p>
	 * 
	 * @return The display name of the product.
	 */
	public String getProductName();

	/**
	 * <p>
	 * Stonks use this to construct the equivalent of this {@link Product} on
	 * different platforms. For example: Fabric constructs {@code ItemStack} from
	 * this value.
	 * </p>
	 * 
	 * @return Product construction data as string. Can be {@code null}.
	 * @deprecated use {@link #getProductMetadata()}, which have supports for
	 *             complex data structure.
	 */
	@Deprecated(forRemoval = true)
	default String getProductConstructionData() {
		var meta = getProductMetadata();
		if (meta instanceof DynamicPrimitive prim) return prim.asString();
		return "";
	}

	/**
	 * <p>
	 * Obtain the structured metadata of this product. The metadata contains stuffs
	 * like "how to reconstruct this product", "the product's floor price" and so
	 * on. At this moment, only {@link DynamicPrimitive} with string type will be
	 * returned from this method when using Stonks for Fabric, but in the future,
	 * user will be able to configure with complex structure.
	 * </p>
	 * <p>
	 * My plan is to move {@link #getProductName()} to metadata as well.
	 * </p>
	 * 
	 * @return The product metadata.
	 */
	public Dynamic getProductMetadata();
}
