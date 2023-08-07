package stonks.core.product;

public interface Product {
	public Category getCategory();

	public String getProductId();

	public String getProductName();

	/**
	 * <p>
	 * Stonks use this to construct the equivalent of this {@link Product} on
	 * different platforms. For example: Fabric constructs {@code ItemStack} from
	 * this value.
	 * </p>
	 * 
	 * @return Product construction data as string. Can be {@code null}.
	 */
	public String getProductConstructionData();
}
