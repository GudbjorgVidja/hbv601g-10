package hbv601g.recipeapp.ui.recipeLists;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.RecipeAdapter;
import hbv601g.recipeapp.databinding.FragmentRecipeListBinding;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;

/**
 * Fragment for a single recipe list. Contains a title, the list creator, a description and a list of Recipes
 */
public class RecipeListFragment extends Fragment {
    private FragmentRecipeListBinding mBinding;
    private RecipeList mRecipeList;
    private RecipeList mClickedList;
    private RecipeListService mRecipeListService;
    private TextView mRecipeListTitle;
    private Button mRenameListButton;




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Clicked list is sent in a bundle to the fragment
        if(getArguments() != null){
            mClickedList = getArguments().getParcelable(getString(R.string.selected_recipe_list));
        }

        mBinding = FragmentRecipeListBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mRecipeListService = new RecipeListService(new NetworkingService(), mainActivity.getUserId());

        /**
         * We use the ID of mClickedList to fetch the list from the API
         * so that it will update when a recipe is added to the list while
         * the list is still open.
         */
        mRecipeList = mRecipeListService.getListById(mClickedList.getId());

        // UI set with list information
        if(mRecipeList != null) {
            setRecipeList();
        }

        ListView mRecipeListListView = mBinding.recipeListRecipes;

        // On click listener so the user can click and view recipes from the list
        mRecipeListListView.setOnItemClickListener((parent, view, position, id) -> {
            Recipe recipe = (Recipe) parent.getItemAtPosition(position);
            Log.d("Selected", recipe.toString());

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe), recipe);
            navController.navigate(R.id.nav_recipe, bundle);
        });

        mRecipeListTitle = mBinding.recipeListTitle;
        mRenameListButton = mBinding.recipeListRenameButton;

        if(mainActivity.getUserId() == mRecipeList.getCreatedBy().getId()){
            // On click listener for renaming the recipe list
            mRenameListButton.setOnClickListener(
                    v -> makeRenameAlert(mainActivity)
            );

        } else {
            mainActivity.makeToast(R.string.recipe_list_rename_not_authorized, Toast.LENGTH_LONG);
        }

        return root;
    }


    /**
     * Makes an alert dialog where the user can rename the recipe list. The dialog does not accept
     * an empty input when the user clicks save, if the user saves with a valid input the API is
     * called to rename the recipe list. If cancel is clicked, the dialog closes and nothing happens.
     * @param mainActivity - The MainActivity of the app
     */
    private void makeRenameAlert(MainActivity mainActivity){
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(getString(R.string.title_rename_recipe_list));

        final EditText input = new EditText(mainActivity);
        input.setText(mRecipeListTitle.getText().toString());
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.save), null);
        builder.setNegativeButton(getString(R.string.cancel_button_text), (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Listens for empty input when clicking save, makes a toast if input is empty.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String newTitle = input.getText().toString().trim();

            if (!newTitle.isEmpty()) {
                mRecipeListTitle.setText(newTitle);
                mRecipeListService.updateRecipeListTitle(mRecipeList.getId(), newTitle);
                dialog.dismiss();
            } else {
                mainActivity.makeToast(R.string.recipe_list_rename_blank, Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * Function to set recipe list information in the UI.
     */
    private void setRecipeList(){
        mBinding.recipeListTitle.setText(mRecipeList.getTitle());

        String tmp = mRecipeList.getCreatedBy() == null ? "Unknown" : mRecipeList.getCreatedBy().getUsername();
        mBinding.recipeListCreatedBy.setText(tmp);

        tmp = mRecipeList.getDescription().isEmpty() ? "No description available" : mRecipeList.getDescription();

        mBinding.recipeListDescription.setText(tmp);

        MainActivity mainActivity = (MainActivity) getActivity();

        assert mainActivity != null;

        ListView recipeListView = mBinding.recipeListRecipes;
        List<Recipe> mListRecipes = mRecipeListService.getRecipesFromList(mRecipeList.getId());

        RecipeAdapter adapter = new RecipeAdapter(mainActivity.getApplicationContext(), mListRecipes);
        Log.d("RecipeListFragment", "List recipes are: " + mRecipeList.getRecipes());
        recipeListView.setAdapter(adapter);
    }
}