package client.helper;

import java.util.ArrayList;
import java.util.List;

import client.model.Product;
import client.model.ProductResponse;

public final class ProductHelper
{
	public static ProductResponse CreateDefaultProducts()
	{
		List<Product> products = new ArrayList<Product>();
		products.add(CreateProduct("Ceapa", 2));
		products.add(CreateProduct("Varza", 1));
		products.add(CreateProduct("Rosie", 5));
		products.add(CreateProduct("Castravete", 1.5));
		products.add(CreateProduct("Praz", 2.3));
		ProductResponse response = new ProductResponse();
		response.setItems(products);
		return response;
	}

	public static Product CreateProduct(String name, double price)
	{
		Product product = new Product();
		product.setName(name);
		product.setPrice(price);
		return product;
	}

}
