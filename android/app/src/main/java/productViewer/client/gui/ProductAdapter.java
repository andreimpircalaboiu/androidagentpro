package productViewer.client.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import productViewer.client.model.Product;

/**
 * Created by Radu2T on 12/5/2016.
 */

public class ProductAdapter extends ArrayAdapter<Product> {

    public ProductAdapter(Context context, ArrayList<Product> products) {
        super(context, 0, products);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Product product = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.product_layout, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvPrice = (TextView) convertView.findViewById(R.id.tvPrice);
        // Populate the data into the template view using the data object
        tvName.setText(product.getName());
        tvPrice.setText(Double.toString(product.getPrice()));
        // Return the completed view to render on screen
        return convertView;
    }
}
