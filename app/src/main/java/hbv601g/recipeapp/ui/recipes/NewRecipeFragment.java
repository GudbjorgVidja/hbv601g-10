package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListAdapter;
import android.widget.ListView;
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
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

/**
 * A fragment to create new recipes
 */
public class NewRecipeFragment extends Fragment {
    private RecipeService mRecipeService;
    private FragmentNewRecipeBinding mBinding;

    private List<IngredientMeasurement> mList = new ArrayList<>();
    private  int mTotalHeight = 0;

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
                            mainActivity.getApplicationContext(), mList
                        );
        mBinding.ingredients.setAdapter(adapter);

        NavController navController = Navigation.findNavController(
                mainActivity, R.id.nav_host_fragment_activity_main
        );

        mRecipeService = new RecipeService(new NetworkingService(), mainActivity.getUserId());

        mBinding.addIngredient.setOnClickListener(view -> {
            navController.navigate(R.id.nav_add_ingredient_measurement_to_recipe);
        });

        mBinding.createRecipe.setOnClickListener(view -> {
                Recipe recipe = createRecipe();
                if (recipe != null) {
                    navController.popBackStack();
                }
                else{
                    Toast.makeText(
                            getActivity(), R.string.recipe_unknown_error, Toast.LENGTH_LONG
                    ).show();
                }
        });

        mBinding.cancelRecipe.setOnClickListener(view -> {
            navController.popBackStack();
        });

        ListView ingredientsList = mBinding.ingredients;
        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_ingredient_measurement),
                this, (requestKey, result) -> {
            IngredientMeasurement ingredientMeasurement
                    = result.getParcelable(getString(R.string.selected_ingredient_measurement));
            mList.add(ingredientMeasurement);
            View listItem = adapter.getView(adapter.getCount()-1, null, mBinding.ingredients);
            listItem.measure(0, 0);
            mTotalHeight += listItem.getMeasuredHeight();

            ViewGroup.LayoutParams params = ingredientsList.getLayoutParams();
            params.height = mTotalHeight + (ingredientsList.getDividerHeight() * (adapter.getCount() - 1));
            ingredientsList.setLayoutParams(params);
            ingredientsList.requestLayout();

        });

        return root;
    }

    /**
     * Uses information from the UI to create a new recipe
     *
     * @return the new recipe
     */
    private Recipe createRecipe(){
        String title =  mBinding.recipeName.getText().toString();
        String instructions = mBinding.instructions.getText().toString();
        Boolean isPrivate = mBinding.isPrivate.isChecked();
        List<IngredientMeasurement> ingredientMeasurementList = new ArrayList<>();

        ListAdapter ingredients= mBinding.ingredients.getAdapter();
        int size = ingredients.getCount();
        for(int i = 0; i < size; i++){
            ingredientMeasurementList.add((IngredientMeasurement) ingredients.getItem(i));
        }

        return  mRecipeService.createRecipe(
                title,instructions, ingredientMeasurementList, isPrivate
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
