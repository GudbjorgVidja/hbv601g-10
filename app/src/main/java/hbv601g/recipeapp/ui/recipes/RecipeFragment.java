package hbv601g.recipeapp.ui.recipes;

import static android.view.View.GONE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Objects;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientMeasurementAdapter;
import hbv601g.recipeapp.databinding.FragmentRecipeBinding;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

/**
 * A fragment for single recipes
 */
public class RecipeFragment extends Fragment {
    private FragmentRecipeBinding binding;
    private Recipe mRecipe;
    private RecipeService mRecipeService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRecipeBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) {
            Log.e("RecipeFragment", "MainActivity is null. Navigation failed.");
            return root;
        }

        mRecipeService = new RecipeService(new NetworkingService(), mainActivity.getUserId());

        if (getArguments() == null ||
                getArguments().getParcelable(getString(R.string.selected_recipe)) == null){
            Log.e("RecipeFragment", "No recipe to view");
            return root;
        }

        mRecipe = getArguments().getParcelable(getString(R.string.selected_recipe));
        setRecipe();

        return root;
    }

    /**
     * Puts information from a selected recipe into the user interface
     */
    private void setRecipe(){
        binding.recipeTitle.setText(mRecipe.getTitle());

        String tmp = mRecipe.getCreatedBy()==null ? "Unknown" : mRecipe.getCreatedBy().getUsername();
        binding.recipeCreator.setText(tmp);

        tmp = mRecipe.getInstructions() == null ? "No instructions" : mRecipe.getInstructions();
        binding.recipeInstructions.setText(tmp);

        tmp = getString(R.string.recipe_tpc, mRecipe.getTotalPurchaseCost()+"");
        binding.recipeTpc.setText(tmp);

        tmp=getString(R.string.recipe_tic,mRecipe.getTotalIngredientCost()+"");
        binding.recipeTic.setText(tmp);

        tmp = mRecipe.isPrivate() ? "private" : "public";
        binding.recipePrivate.setText(tmp);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null && mainActivity.getUserId() != 0){
            double ppc = mRecipeService.getPersonalizedPurchaseCost(mRecipe.getId());
            tmp = getString(R.string.recipe_ppc, ppc+"");
            binding.recipePpc.setText(tmp);
        }
        else {
            binding.recipePpc.setVisibility(GONE);
        }

        assert mainActivity != null;
        ListView ingredientMeasurementListView = binding.recipeIngredients;
        IngredientMeasurementAdapter adapter = new IngredientMeasurementAdapter(
                mainActivity.getApplicationContext(),
                Objects.requireNonNullElseGet(mRecipe.getIngredientMeasurements(), ArrayList::new));

        ingredientMeasurementListView.setAdapter(adapter);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding=null;
    }
}
