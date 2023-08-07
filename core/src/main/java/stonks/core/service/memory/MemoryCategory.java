package stonks.core.service.memory;

import java.util.ArrayList;
import java.util.List;

import stonks.core.product.Category;
import stonks.core.product.Product;

public class MemoryCategory implements Category {
	private String id;
	private String name;
	private List<MemoryProduct> products = new ArrayList<>();

	public MemoryCategory(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String getCategoryId() { return id; }

	@Override
	public String getCategoryName() { return name; }

	@Override
	public List<Product> getProducts() { return products.stream().map(v -> (Product) v).toList(); }

	public List<MemoryProduct> getModifiableMockProducts() { return products; }
}
