package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.RecipeAdapter;
import hbv601g.recipeapp.databinding.FragmentRecipesBinding;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding mBinding;
    private RecipeService mRecipeService;
    private List<Recipe> mRecipeList;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentRecipesBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        long uid = mainActivity.getUserId();
        mRecipeService = new RecipeService(new NetworkingService(), uid);

        mRecipeService.getAllRecipes(new CustomCallback<>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                Log.d("Callback", "get all recipes success");
                requireActivity().runOnUiThread(() -> {
                    mRecipeList = recipes;
                    makeRecipesView(mainActivity, navController);
                });
            }

            @Override
            public void onFailure(List<Recipe> recipes) {
                requireActivity().runOnUiThread(() -> {
                    mRecipeList = recipes;
                    mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
                });
            }
        });


        if(mainActivity.getUserId() != 0) {
            mBinding.addRecipe.setOnClickListener(view ->
                navController.navigate(R.id.nav_new_recipe));
        }
        else{
            mBinding.addRecipe.hide();
        }

        return  root;
    }


    /**
     * Makes the ui components which use a list of recipes
     * @param mainActivity - the MainActivity
     * @param navController - the NavController
     */
    private void makeRecipesView(MainActivity mainActivity, NavController navController){
        ListView recipesListView = mBinding.recipesListView;

        RecipeAdapter recipeAdapter = new RecipeAdapter(mainActivity.getApplicationContext(), mRecipeList);
        recipesListView.setAdapter(recipeAdapter);

        recipesListView.setOnItemClickListener((parent, view, position, id) -> {
            Recipe recipe = (Recipe) parent.getItemAtPosition(position);
            Log.d("Selected", recipe.toString());
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe), recipe);
            navController.navigate(R.id.nav_recipe, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
