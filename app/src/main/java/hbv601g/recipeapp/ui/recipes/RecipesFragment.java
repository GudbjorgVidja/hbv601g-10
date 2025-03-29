package hbv601g.recipeapp.ui.recipes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.RecipeAdapter;
import hbv601g.recipeapp.databinding.FragmentRecipesBinding;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding mBinding;
    private RecipeService mRecipeService;
    private List<Recipe> mRecipeList;
    private ListView mRecipeListView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentRecipesBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        Button mFilterTPCButton = mBinding.filterTpcButton;
        Button mFilterTICButton = mBinding.filterTicButton;
        Button mClearFilterButton = mBinding.clearFilterButton;

        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        long uid = mainActivity.getUserId();
        mRecipeService = new RecipeService(new NetworkingService(), uid);


        try{
            mRecipeList = mRecipeService.getAllRecipes();
        } catch (NullPointerException e){
            mRecipeList = new ArrayList<>();
            mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
        }

        mRecipeListView = mBinding.recipesListView;

        RecipeAdapter recipeAdapter = new RecipeAdapter(mainActivity.getApplicationContext(), mRecipeList);
        mRecipeListView.setAdapter(recipeAdapter);

        mRecipeListView.setOnItemClickListener((parent, view, position, id) -> {
            Recipe recipe = (Recipe) parent.getItemAtPosition(position);
            Log.d("Selected", recipe.toString());

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe), recipe);
            navController.navigate(R.id.nav_recipe, bundle);
        });

        mFilterTPCButton.setOnClickListener(v -> makeFilterTPCAlert(mainActivity));
        mFilterTICButton.setOnClickListener(v -> makeFilterTICAlert(mainActivity));

        mClearFilterButton.setOnClickListener(v -> {
            try {
                mRecipeList = mRecipeService.getAllRecipes();
                updateListView(mRecipeList);
            } catch (NullPointerException e) {
                mRecipeList = new ArrayList<>();
                mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
            }
        });



        if(mainActivity.getUserId() != 0) {
            mBinding.addRecipe.setOnClickListener(view -> {
                navController.navigate(R.id.nav_new_recipe);
            });
        }
        else{
            mBinding.addRecipe.hide();
        }

        return  root;
    }

    /**
     * Alert dialog that allows the user to input a maximum TPC to filter the recipe list by.
     * The filtered list is then sent to the UI. The user can only input numbers.
     * @param mainActivity - The MainActivity of the application.
     */
    private void makeFilterTPCAlert(MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.title_filter_tpc));

        final EditText input = new EditText(mainActivity);
        input.setHint("Enter max TPC");
        alert.setView(input);

        alert.setPositiveButton(R.string.confirm_filter, (dialog, which) -> {
            try {
                int maxTPC = Integer.parseInt(input.getText().toString());
                List<Recipe> filteredRecipes = mRecipeService.getAllRecipesUnderTPC(maxTPC);
                if (filteredRecipes != null) {
                    updateListView(filteredRecipes);
                } else {
                    mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_SHORT);
                }
            } catch (NumberFormatException e) {
                mainActivity.makeToast(R.string.invalid_price_input, Toast.LENGTH_SHORT);
            }
        });

        alert.setNegativeButton(R.string.cancel_button_text, (dialog, which) -> dialog.cancel());
        alert.show();
    }


    /**
     * Alerti dialog that allows the user to input a maximum TIC to tilfet the recipe list by.
     * The filtered list is then sent to the UI. The user can only input numbers.
     * @param mainActivity - The MainActivity of the application
     */
    private void makeFilterTICAlert(MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.title_filter_tic));

        final EditText input = new EditText(mainActivity);
        input.setHint("Enter max TIC");
        alert.setView(input);

        alert.setPositiveButton(R.string.confirm_filter, (dialog, which) -> {
            try {
                int maxTIC = Integer.parseInt(input.getText().toString());
                List<Recipe> filteredRecipes = mRecipeService.getAllRecipesUnderTIC(maxTIC);
                if (filteredRecipes != null) {
                    updateListView(filteredRecipes);
                } else {
                    mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_SHORT);
                }
            } catch (NumberFormatException e) {
                mainActivity.makeToast(R.string.invalid_price_input, Toast.LENGTH_SHORT);
            }
        });

        alert.setNegativeButton(R.string.cancel_button_text, (dialog, which) -> dialog.cancel());
        alert.show();
    }


    /**
     * Method that sets the recipe list displayed in the Recipe ListView.
     * @param newList - New list to send to the ListView
     */
    private void updateListView(List<Recipe> newList) {
        RecipeAdapter adapter = new RecipeAdapter(requireContext(), newList);
        mRecipeListView.setAdapter(adapter);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
