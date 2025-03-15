package hbv601g.recipeapp.ui.recipeLists;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
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
 * https://developer.android.com/develop/ui/views/components/dialogs
 * https://stackoverflow.com/questions/10932832/multiple-choice-alertdialog-with-custom-adapter
 */
public class AddRecipeToListDialogFragment extends DialogFragment {

    private RecipeListService mRecipeListService;

    private List<RecipeList> mRecipeLists;

    private long mRid;

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

        // Prófaði að staðfesta að mainActivity sé rétt
        //mainActivity.makeToast(R.string.add_to_list_button, Toast.LENGTH_LONG);

        mRecipeLists = mRecipeListService.getUserRecipeLists();


        // Setja upp adapterinn
        // Ath contextið
        //RecipeListAdapter adapter = new RecipeListAdapter(mainActivity.getApplicationContext(), mRecipeLists);
        //RecipeListAdapter adapter = new RecipeListAdapter(getContext(), mRecipeLists);
        RecipeListAdapter adapter = new RecipeListAdapter(getActivity(), mRecipeLists);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a list to add the recipe to");

        // Set adapter
        builder.setAdapter(adapter, (dialog, which) -> {
            Log.d("Dialog", "selected " + which);
            try {
                RecipeList recipeList = mRecipeListService.addRecipeToList(mRid,((RecipeList) adapter.getItem(which)).getId());
                mainActivity.makeToast(R.string.add_recipe_to_list_success_toast, Toast.LENGTH_LONG);
            } catch (NullPointerException e){
                mainActivity.makeToast(R.string.add_recipe_to_list_failed_toast, Toast.LENGTH_LONG);
            }
        } );


        builder.setNegativeButton("cancel", (dialog, id) ->{
            Log.d("Dialog", "Cancelled without action");
        } );
        return builder.create();


    }


}
