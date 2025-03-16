package hbv601g.recipeapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import hbv601g.recipeapp.R;
import hbv601g.recipeapp.entities.RecipeList;

/**
 * Adapter to display RecipeList objects in a ListView
 */
public class RecipeListAdapter extends BaseAdapter {
    private List<RecipeList> mRecipeLists;
    private LayoutInflater mInflater;

    public RecipeListAdapter(Context context, List<RecipeList> recipeLists) {
        this.mRecipeLists = recipeLists;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mRecipeLists.size();
    }

    @Override
    public Object getItem(int position) {
        return mRecipeLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.recipe_list_list_item, parent, false);
        }
            TextView title = convertView.findViewById(R.id.recipe_list_title_textview);
            TextView privacy = convertView.findViewById(R.id.recipe_list_privacy_textview);

            RecipeList currentRecipeList = (RecipeList) getItem(position);

            title.setText(currentRecipeList.getTitle());
            String tmp = currentRecipeList.isPrivate() ? "private" : "public";
            privacy.setText(tmp);


        return convertView;
    }


}
