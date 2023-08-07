package stonks.core.product;

import java.util.List;

public interface Category {
	public String getCategoryId();
	public String getCategoryName();
	public List<Product> getProducts();
}
