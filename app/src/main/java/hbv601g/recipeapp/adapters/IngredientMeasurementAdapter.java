package hbv601g.recipeapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hbv601g.recipeapp.R;
import hbv601g.recipeapp.entities.IngredientMeasurement;

public class IngredientMeasurementAdapter extends BaseAdapter {
    private List<IngredientMeasurement> list;
    private LayoutInflater thisInflater;

    public IngredientMeasurementAdapter(Context context, List<IngredientMeasurement> list){
        this.list = list;
        thisInflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = thisInflater.inflate(R.layout.ingredient_measurement_list_item, parent, false);

            //todo fix this.

            TextView title = convertView.findViewById(R.id.ingredient_measurement_title);
            TextView quantity = convertView.findViewById(R.id.ingredient_measurement_quantity);
            TextView unit = convertView.findViewById(R.id.ingredient_list_unit);

            IngredientMeasurement ingreMeas = (IngredientMeasurement) getItem(position);

            title.setText(ingreMeas.getIngredient().getTitle());
            quantity.setText(String.format("%.2f", ingreMeas.getQuantity()));
            unit.setText(ingreMeas.getUnit().toString());
        }
        return convertView;
    }
}
