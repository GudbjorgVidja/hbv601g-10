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
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.RecipeList;

public class RecipeListAdapter extends BaseAdapter {

    private List<RecipeList> mRecipeLists;
    private LayoutInflater mInflater;

    public RecipeListAdapter(Context context, List<RecipeList> recipeLists){
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
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.recipe_list_list_item, parent, false);
        }

        TextView title = convertView.findViewById(R.id.list_title);

        RecipeList list = (RecipeList) getItem(position);

        title.setText((list.getTitle()));
        return convertView;
    }


}
