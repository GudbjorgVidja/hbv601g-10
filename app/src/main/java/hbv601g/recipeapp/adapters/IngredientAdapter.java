package hbv601g.recipeapp.adapters;

import android.content.Context;
import android.icu.text.DecimalFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import hbv601g.recipeapp.R;
import hbv601g.recipeapp.entities.Ingredient;

/**
 * Adapter sem er settur á ListView hlut til að gera lista
 * af ingredients.
 */
public class IngredientAdapter extends ArrayAdapter<Ingredient> {
    private List<Ingredient> mIngredientList;
    private LayoutInflater mInflater;

    public IngredientAdapter(Context context, List<Ingredient> ingredients){
        super(context, R.layout.ingredient_list_item, ingredients);
        this.mIngredientList = ingredients;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mIngredientList.size();
    }

    @Override
    public Ingredient getItem(int position) {
        return mIngredientList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Allows the ingredient list to be set to update the data set.
     *
     * @param list the new ingredient list
     */
    public void setIngredientList(List<Ingredient> list){
        mIngredientList = list;
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

        // Ingredient object for a given list item
        Ingredient currentIngredient = getItem(position);

        DecimalFormat df = new DecimalFormat("###,##0.###");

        title.setText(currentIngredient.getTitle());
        quantity.setText(String.format("%s", df.format(currentIngredient.getQuantity())));
        price.setText(String.format(Locale.getDefault(), "%,.0f", currentIngredient.getPrice()));
        unit.setText(currentIngredient.getUnit().toString());
        return convertView;
    }



}
