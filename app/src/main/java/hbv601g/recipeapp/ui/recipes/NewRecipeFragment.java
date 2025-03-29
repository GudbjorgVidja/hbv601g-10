package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListAdapter;
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
import hbv601g.recipeapp.databinding.FragmentNewRecipeBinding;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

public class NewRecipeFragment extends Fragment {
    private RecipeService mRecipeService;
    private FragmentNewRecipeBinding mBinding;

    private List<IngredientMeasurement> mIngredientList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentNewRecipeBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        IngredientMeasurementAdapter adapter = new IngredientMeasurementAdapter
                        (
                            mainActivity.getApplicationContext(), mIngredientList
                        );
        mBinding.ingredients.setAdapter(adapter);

        NavController navController = Navigation.findNavController(
                mainActivity, R.id.nav_host_fragment_activity_main
        );

        mRecipeService = new RecipeService(new NetworkingService(), mainActivity.getUserId());

        mBinding.addIngredient.setOnClickListener(view ->
            navController.navigate(R.id.nav_add_ingredient_measurement_to_recipe));


        mBinding.createRecipe.setOnClickListener(view -> createRecipe(navController));

        mBinding.cancelRecipe.setOnClickListener(view -> navController.popBackStack());

        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_ingredient_measurement),
                this, (requestKey, result) -> {
            IngredientMeasurement ingredientMeasurement
                    = result.getParcelable(getString(R.string.selected_ingredient_measurement));
            mIngredientList.add(ingredientMeasurement);
        });

        return root;
    }

    /**
     * Creates a recipe and changes the ui based on the result
     * @param navController - The NavController
     */
    private void createRecipe( NavController navController){
        String title =  mBinding.recipeName.getText().toString();
        String instructions = mBinding.instructions.getText().toString();
        Boolean isPrivate = mBinding.isPrivate.isChecked();
        List<IngredientMeasurement> ingredientMeasurementList = new ArrayList<>();

        ListAdapter ingredients= mBinding.ingredients.getAdapter();
        int size = ingredients.getCount();
        for(int i = 0; i < size; i++){
            ingredientMeasurementList.add((IngredientMeasurement) ingredients.getItem(i));
        }

        mRecipeService.createRecipe(title, instructions, ingredientMeasurementList, isPrivate, new CustomCallback<>() {
            @Override
            public void onSuccess(Recipe recipe) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), R.string.create_recipe_success_toast, Toast.LENGTH_LONG).show();
                    navController.popBackStack();
                });
            }

            @Override
            public void onFailure(Recipe recipe) {
                requireActivity().runOnUiThread(() -> {
                    if(recipe != null){
                        Log.d("Callback", "Recipe created but failed to add ingredient measurements");
                        Toast.makeText(getActivity(), R.string.create_recipe_ingredients_failed_toast, Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(getActivity(), R.string.create_recipe_failed_toast, Toast.LENGTH_LONG).show();
                });
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
