package hbv601g.recipeapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hbv601g.recipeapp.R;
import hbv601g.recipeapp.entities.Recipe;

public class RecipeAdapter extends BaseAdapter {
    private List<Recipe> mRecipeList;
    private LayoutInflater thisInflater;

    public RecipeAdapter(Context context, List<Recipe> recipes){
        this.mRecipeList = recipes;
        thisInflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return mRecipeList.size();
    }

    @Override
    public Object getItem(int position) {
        return mRecipeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;//mRecipeList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = thisInflater.inflate(R.layout.recipe_list_item, parent, false);

            TextView title = convertView.findViewById(R.id.recipe_overview_name);
            TextView createdBy = convertView.findViewById(R.id.recipe_overview_created_by);
            TextView tpc = convertView.findViewById(R.id.recipe_overview_tpc);
            TextView tic = convertView.findViewById(R.id.recipe_overview_tic);

            Recipe currentRecipe = (Recipe) getItem(position);

            title.setText(currentRecipe.getTitle());
            createdBy.setText(currentRecipe.getRecipeCreator());
            tpc.setText(String.format("%s",currentRecipe.getTotalPurchaseCost()));
            tic.setText(String.format("%s",(int) Math.round(currentRecipe.getTotalIngredientCost())));
        }
        return convertView;
    }
}
