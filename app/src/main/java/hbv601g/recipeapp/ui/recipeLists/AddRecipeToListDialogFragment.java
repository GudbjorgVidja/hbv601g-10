package hbv601g.recipeapp.ui.recipeLists;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.RecipeListAdapter;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;

/**
 * Dialog fragment to show a dialog of possible lists to add a recipe to. When a list is selected
 * the given recipe is added to that list and the dialog closed.
 */
public class AddRecipeToListDialogFragment extends DialogFragment {

    private RecipeListService mRecipeListService;
    private List<RecipeList> mRecipeLists;
    private long mRid;


    /**
     * Method to create an instance of the fragment with arguments
     * @param rid - id of recipe to add to list
     * @return an instance of the fragment
     */
    public static AddRecipeToListDialogFragment newInstance(long rid) {
        AddRecipeToListDialogFragment f = new AddRecipeToListDialogFragment();
        Bundle args = new Bundle();
        args.putLong("rid", rid);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null)
            mRid = getArguments().getLong("rid");

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        mRecipeListService = new RecipeListService(new NetworkingService(), mainActivity.getUserId());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.add_recipe_to_list_dialog_title));

        try {
            mRecipeLists = mRecipeListService.getUserRecipeLists();
        } catch (NullPointerException e){
            mainActivity.makeToast(R.string.get_recipe_lists_failed_toast, Toast.LENGTH_LONG);
        }

        if(mRecipeLists != null) {
            RecipeListAdapter adapter = new RecipeListAdapter(getActivity(), mRecipeLists);
            builder.setAdapter(adapter, (dialog, which) -> {
                try {
                    mRecipeListService.addRecipeToList(mRid, ((RecipeList) adapter.getItem(which)).getId());
                    mainActivity.makeToast(R.string.add_recipe_to_list_success_toast, Toast.LENGTH_LONG);
                } catch (NullPointerException e) {
                    mainActivity.makeToast(R.string.add_recipe_to_list_failed_toast, Toast.LENGTH_LONG);
                }
            });
        }

        builder.setNegativeButton("cancel", null);
        return builder.create();
    }


}
