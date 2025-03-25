package hbv601g.recipeapp.ui.recipeLists;

import static android.view.View.GONE;

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
import android.widget.ListView;
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

        /*
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


        if(mRecipeList != null && mRecipeList.getCreatedBy() != null && mainActivity.getUserId() != 0 &&
                mRecipeList.getCreatedBy().getId() == mainActivity.getUserId() ){
            mBinding.deleteListButton.setOnClickListener(
                    v -> makeDeleteListAlert(navController, mainActivity));
        }
        else {
            mBinding.deleteListButton.setVisibility(GONE);
        }


        return root;
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

    /**
     * Makes an alert to delete this recipe list. If confirmed, the list gets deleted.
     * @param navController - the navController instance
     * @param mainActivity - the current mainActivity
     */
    private void makeDeleteListAlert(NavController navController, MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(getString(R.string.delete_list_alert_title));
        alert.setMessage(getString(R.string.delete_list_alert_message));
        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            boolean result = mRecipeListService.deleteRecipeList(mRecipeList.getId());
            if (result){
                navController.popBackStack();
                mainActivity.makeToast(R.string.delete_list_success, Toast.LENGTH_LONG);
            }
            else mainActivity.makeToast(R.string.delete_list_failed, Toast.LENGTH_LONG);
        });
        alert.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        alert.show();
    }

}