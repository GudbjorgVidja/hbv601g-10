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
 * Adapter fyrir ListView til að gera lista af IngredientMeasurement
 */
public class IngredientMeasurementAdapter extends BaseAdapter {
    private List<IngredientMeasurement> mIngredientMeasurementList;
    private LayoutInflater thisInflater;

    public IngredientMeasurementAdapter(Context context, List<IngredientMeasurement> list){
        this.mIngredientMeasurementList = list;
        thisInflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return mIngredientMeasurementList.size();
    }

    @Override
    public Object getItem(int position) {
        return mIngredientMeasurementList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = thisInflater.inflate(R.layout.ingredient_measurement_list_item, parent, false);

            TextView title = convertView.findViewById(R.id.ingredient_measurement_title);
            TextView quantity = convertView.findViewById(R.id.ingredient_measurement_quantity);

            IngredientMeasurement ingreMeas = (IngredientMeasurement) getItem(position);

            title.setText(ingreMeas.getIngredient().getTitle());
            quantity.setText(String.format("%.2f " + ingreMeas.getUnit().toString(),
                    ingreMeas.getQuantity()));
        }
        return convertView;
    }
}
