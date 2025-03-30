package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientMeasurementAdapter;
import hbv601g.recipeapp.databinding.FragmentEditRecipeBinding;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

/**
 * Fragment which opens when the user chooses to edit a recipe, which includes changing the title
 * and instructions, and adding ingredient measurements
 */
public class EditRecipeFragment extends Fragment {
    private RecipeService mRecipeService;
    private FragmentEditRecipeBinding mBinding;
    private List<IngredientMeasurement> mList;
    private Recipe mRecipe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentEditRecipeBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        NavController navController = Navigation.findNavController(
                mainActivity, R.id.nav_host_fragment_activity_main
        );

        mRecipeService = new RecipeService(new NetworkingService(), mainActivity.getUserId());

        if (getArguments() == null || getArguments().getParcelable(getString(R.string.selected_recipe)) == null) {
            Log.e("EditRecipeFragment", "No recipe to edit");
            navController.popBackStack();
        }

        try {
            mRecipe = getArguments().getParcelable(getString(R.string.selected_recipe));
            if (mRecipe.getCreatedBy() == null) {
                Log.e("EditRecipeFragment", "No ones owns this");
                navController.popBackStack();
            }
        } catch (NullPointerException e) {
            mainActivity.makeToast(R.string.recipe_missing_toast, Toast.LENGTH_LONG);
            navController.popBackStack();
        }


        setEditable(mainActivity);

        mBinding.addIngredient.setOnClickListener(view ->
            navController.navigate(R.id.nav_add_ingredient_measurement_to_recipe));

        mBinding.cancelEditRecipe.setOnClickListener(view -> navController.popBackStack());

        mBinding.editRecipe.setOnClickListener(view -> editRecipe(mainActivity, navController));

        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_ingredient_measurement),
                this, (requestKey, result) -> {
                    IngredientMeasurement ingredientMeasurement
                            = result.getParcelable(getString(R.string.selected_ingredient_measurement));
                    mList.add(ingredientMeasurement);
                });

        return root;
    }

    /**
     * Sets all of the information so that the user/owner can edit their recipe
     *
     * @param activity: MainActivity value, is the activity that is now it progress.
     */
    private void setEditable(MainActivity activity) {
        mBinding.recipeName.setText(mRecipe.getTitle());
        mBinding.instructions.setText(mRecipe.getInstructions());

        mList = mRecipe.getIngredientMeasurements();
        if (mList == null) {
            mList = new ArrayList<>();
        }

        IngredientMeasurementAdapter adapter = new IngredientMeasurementAdapter
                (
                        activity.getApplicationContext(),
                        mList
                );
        mBinding.ingredients.setAdapter(adapter);

        mBinding.isPrivate.setChecked(mRecipe.isPrivate());
    }

    /**
     * Updates the recipe when changes get confirmed, and reacts in the ui
     *
     * @param mainActivity  : MainActivity value, is the activity of the fragment
     * @param navController : the NavController
     */
    private void editRecipe(MainActivity mainActivity, NavController navController) {
        if (mRecipe.getCreatedBy().getId() != mainActivity.getUserId()) {
            return;
        }

        Recipe upRes = new Recipe();
        upRes.setTitle(mBinding.recipeName.getText().toString());
        upRes.setInstructions(mBinding.instructions.getText().toString());
        upRes.setPrivate(mBinding.isPrivate.isChecked());

        mRecipeService.updateRecipe(upRes, mRecipe.getId(), mList, new CustomCallback<>() {
            @Override
            public void onSuccess(Recipe recipe) {
                requireActivity().runOnUiThread(() -> {
                    Bundle res = new Bundle();
                    res.putParcelable(getString(R.string.selected_recipe), recipe);

                    getParentFragmentManager().setFragmentResult(getString(R.string.request_edit_recipe), res);
                    navController.popBackStack();
                });
            }

            @Override
            public void onFailure(Recipe recipe) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), R.string.recipe_edit_unknown_error, Toast.LENGTH_LONG).show());
            }
        });

    }
}
