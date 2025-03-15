package hbv601g.recipeapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hbv601g.recipeapp.R;
import hbv601g.recipeapp.entities.Ingredient;

/**
 * Adapter sem er settur á ListView hlut til að gera lista
 * af ingredients.
 */
public class IngredientAdapter extends BaseAdapter {
    private List<Ingredient> mIngredientList;
    private LayoutInflater mInflater;

    public IngredientAdapter(Context context, List<Ingredient> ingredients){
        this.mIngredientList = ingredients;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mIngredientList.size();
    }

    @Override
    public Object getItem(int position) {
        return mIngredientList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.ingredient_list_item, parent, false);
        }


        TextView title = convertView.findViewById(R.id.ingredient_list_title);
        TextView quantity = convertView.findViewById(R.id.ingredient_list_quantity);
        TextView price = convertView.findViewById(R.id.ingredient_list_price);
        TextView unit = convertView.findViewById(R.id.ingredient_list_unit);

        // Ingredient object fyrir tiltekinn list item
        Ingredient currentIngredient = (Ingredient) getItem(position);

        title.setText(currentIngredient.getTitle());
        quantity.setText(String.format("%s", currentIngredient.getQuantity()));
        price.setText(String.format("%s", currentIngredient.getPrice()));
        unit.setText(currentIngredient.getUnit().toString());
        return convertView;
    }



}
