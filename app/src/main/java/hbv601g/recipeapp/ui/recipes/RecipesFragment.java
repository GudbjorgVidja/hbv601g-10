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
    private Recipe viewRep;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentRecipesBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

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
            viewRep = recipe;
            Log.d("Selected", recipe.toString());

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe), recipe);
            navController.navigate(R.id.nav_recipe, bundle);
        });

        if(mainActivity.getUserId() != 0) {
            mBinding.addRecipe.setOnClickListener(view -> {
                navController.navigate(R.id.nav_new_recipe);
            });
        }
        else{
            mBinding.addRecipe.hide();
        }

        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_new_recipe),
                this, (requestKey, result) -> {
                    Recipe recipe = result.getParcelable(getString(R.string.selected_recipe));
                    mRecipeList.add(recipe);
                });

        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_edit_recipe),
                this, ((requestKey, result) -> {
                    Recipe recipe = result.getParcelable(getString(R.string.selected_recipe));
                    mRecipeList.remove(viewRep);
                    mRecipeList.add(recipe);

                    recipeAdapter.notifyDataSetChanged();
                }));

        return  root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
