package productViewer.client.model;

import java.util.List;

public class ProductResponse extends Response
{
	private List<Product> items;
	
	public List<Product> getItems()
	{
		return items;
	}

	public void setItems(List<Product> items)
	{
		this.items = items;
	}
}
