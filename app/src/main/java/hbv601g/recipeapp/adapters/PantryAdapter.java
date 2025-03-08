package hbv601g.recipeapp.adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hbv601g.recipeapp.R;
import hbv601g.recipeapp.entities.IngredientMeasurement;

public class PantryAdapter extends BaseAdapter {
    private List<IngredientMeasurement> pantryList;
    private LayoutInflater inflater;

    public PantryAdapter(Context context, List<IngredientMeasurement> ingredients){
        this.pantryList = ingredients;
        inflater = (LayoutInflater.from(context));
    }


    @Override
    public int getCount() {
        return pantryList.size();
    }

    @Override
    public Object getItem(int position) {
        return pantryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.pantry_list_item, parent, false);
        }

        TextView title = convertView.findViewById(R.id.pantry_list_title);
        TextView quantity = convertView.findViewById(R.id.pantry_list_quantity);
        TextView unit = convertView.findViewById(R.id.pantry_list_unit);

        IngredientMeasurement currentPantryItem = (IngredientMeasurement) getItem(position);

        if (currentPantryItem != null && currentPantryItem.getIngredient() != null) {
            title.setText(currentPantryItem.getIngredient().getTitle());
            quantity.setText(String.format("%s", currentPantryItem.getQuantity()));
            unit.setText(currentPantryItem.getUnit().toString());
        } else {
            title.setText("Unknown");
            quantity.setText("-");
            unit.setText("-");
        }

        return convertView;
    }

}
