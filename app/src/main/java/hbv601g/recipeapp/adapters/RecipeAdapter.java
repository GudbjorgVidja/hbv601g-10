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

/**
 * An adapter used to show Recipe items in a ListView in the UI
 */
public class RecipeAdapter extends BaseAdapter {
    private List<Recipe> mRecipeList;
    private LayoutInflater mInflater;

    public RecipeAdapter(Context context, List<Recipe> recipes){
        this.mRecipeList = recipes;
        mInflater = LayoutInflater.from(context);
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

    /**
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return the view that was created
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.recipe_list_item, parent, false);
        }

        TextView title = convertView.findViewById(R.id.recipe_overview_name);
        TextView createdBy = convertView.findViewById(R.id.recipe_overview_created_by);
        TextView tpc = convertView.findViewById(R.id.recipe_overview_tpc);
        TextView tic = convertView.findViewById(R.id.recipe_overview_tic);

        Recipe currentRecipe = (Recipe) getItem(position);

        title.setText(currentRecipe.getTitle());
        createdBy.setText(currentRecipe.getRecipeCreator());
        tpc.setText(String.format("%s",currentRecipe.getTotalPurchaseCost()));
        tic.setText(String.format("%s",(int) Math.round(currentRecipe.getTotalIngredientCost())));
        return convertView;
    }
}
