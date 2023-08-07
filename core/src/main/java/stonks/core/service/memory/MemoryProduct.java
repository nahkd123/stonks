package stonks.core.service.memory;

import stonks.core.product.Product;

public class MemoryProduct implements Product {
	private MemoryCategory category;
	private String id;
	private String name;
	private String constructData;

	public MemoryProduct(MemoryCategory category, String id, String name, String constructData) {
		this.category = category;
		this.id = id;
		this.name = name;
		this.constructData = constructData;
	}

	@Override
	public MemoryCategory getCategory() { return category; }

	@Override
	public String getProductId() { return id; }

	@Override
	public String getProductName() { return name; }

	@Override
	public String getProductConstructionData() { return constructData; }
}
