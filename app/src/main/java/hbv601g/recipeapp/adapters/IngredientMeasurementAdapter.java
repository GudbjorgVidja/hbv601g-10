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


/**
 * Adapter to display a list of IngredientMeasurement in a ListView
 */
public class IngredientMeasurementAdapter extends BaseAdapter {
    private final List<IngredientMeasurement> mIngredientMeasurements;
    private final LayoutInflater mLayoutInflater;

    public IngredientMeasurementAdapter(Context context, List<IngredientMeasurement> ingredientMeasurements){
        this.mIngredientMeasurements = ingredientMeasurements;
        mLayoutInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return mIngredientMeasurements.size();
    }

    @Override
    public Object getItem(int position) {
        return mIngredientMeasurements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.ingredient_measurement_item, parent, false);
        }

        TextView title = convertView.findViewById(R.id.ingredient_measurement_list_title);
        TextView quantity = convertView.findViewById(R.id.ingredient_measurement_list_quantity);
        TextView unit = convertView.findViewById(R.id.ingredient_measurement_list_unit);

        IngredientMeasurement currIngMeas = (IngredientMeasurement) getItem(position);

        if (currIngMeas.getIngredient()!=null && currIngMeas.getUnit() != null
                && currIngMeas.getQuantity() != 0){
            title.setText(currIngMeas.getIngredient().getTitle());
            quantity.setText(String.format("%s", currIngMeas.getQuantity()));
            unit.setText(currIngMeas.getUnit().toString());
        }


        return convertView;
    }

}
