package hbv601g.recipeapp.adapters;

import android.content.Context;
import android.icu.text.DecimalFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hbv601g.recipeapp.R;
import hbv601g.recipeapp.entities.IngredientMeasurement;

/**
 * Adapter fyrir ListView til að gera lista af ingredients
 * sem eru í pantry hjá notanda
 */
public class PantryAdapter extends BaseAdapter {
    private List<IngredientMeasurement> mPantryList;
    private LayoutInflater mInflater;

    public PantryAdapter(Context context, List<IngredientMeasurement> ingredients){
        this.mPantryList = ingredients;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return mPantryList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPantryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pantry_list_item, parent, false);
        }

        DecimalFormat df = new DecimalFormat("###,##0.###");
        TextView title = convertView.findViewById(R.id.pantry_list_title);
        TextView quantity = convertView.findViewById(R.id.pantry_list_quantity);
        TextView unit = convertView.findViewById(R.id.pantry_list_unit);

        IngredientMeasurement currentPantryItem = (IngredientMeasurement) getItem(position);

        title.setText(currentPantryItem.getIngredient().getTitle());
        quantity.setText(String.format("%s", df.format(currentPantryItem.getQuantity())));
        unit.setText(currentPantryItem.getUnit().toString());


        return convertView;
    }

}
