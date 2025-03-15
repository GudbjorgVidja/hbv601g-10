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
import hbv601g.recipeapp.ui.recipeLists.AddRecipeToListDialogFragment;

/**
 * A fragment for single recipes
 */
public class RecipeFragment extends Fragment {
    private FragmentRecipeBinding mBinding;
    private Recipe mRecipe;
    private RecipeService mRecipeService;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentRecipeBinding.inflate(inflater,container,false);
        View root = mBinding.getRoot();
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

        mBinding.addToListButton.setOnClickListener(v -> {
            AddRecipeToListDialogFragment dialog = AddRecipeToListDialogFragment.newInstance(mRecipe.getId());
            dialog.show(mainActivity.getSupportFragmentManager(), "AddRecipeToListDialogFragment");
        });

        mRecipe = getArguments().getParcelable(getString(R.string.selected_recipe));
        setRecipe();

        return root;
    }

    /**
     * Puts information from a selected recipe into the user interface
     */
    private void setRecipe(){
        mBinding.recipeTitle.setText(mRecipe.getTitle());

        String tmp = mRecipe.getCreatedBy()==null ? "Unknown" : mRecipe.getCreatedBy().getUsername();
        mBinding.recipeCreator.setText(tmp);

        tmp = mRecipe.getInstructions() == null ? "No instructions" : mRecipe.getInstructions();
        mBinding.recipeInstructions.setText(tmp);

        tmp = getString(R.string.recipe_tpc, mRecipe.getTotalPurchaseCost()+"");
        mBinding.recipeTpc.setText(tmp);

        tmp=getString(R.string.recipe_tic,mRecipe.getTotalIngredientCost()+"");
        mBinding.recipeTic.setText(tmp);

        tmp = mRecipe.isPrivate() ? "private" : "public";
        mBinding.recipePrivate.setText(tmp);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null && mainActivity.getUserId() != 0){
            double ppc = mRecipeService.getPersonalizedPurchaseCost(mRecipe.getId());
            tmp = getString(R.string.recipe_ppc, ppc+"");
            mBinding.recipePpc.setText(tmp);
        }
        else {
            mBinding.recipePpc.setVisibility(GONE);
        }

        assert mainActivity != null;
        ListView ingredientMeasurementListView = mBinding.recipeIngredients;
        IngredientMeasurementAdapter adapter = new IngredientMeasurementAdapter(
                mainActivity.getApplicationContext(),
                Objects.requireNonNullElseGet(mRecipe.getIngredientMeasurements(), ArrayList::new));

        ingredientMeasurementListView.setAdapter(adapter);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding =null;
    }
}
