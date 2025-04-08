package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
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
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

public class NewRecipeFragment extends Fragment {
    private RecipeService mRecipeService;
    private FragmentNewRecipeBinding mBinding;

    private List<IngredientMeasurement> mList = new ArrayList<>();

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
                Recipe recipe = createRecipe(mainActivity);
                if (recipe != null) {
                    navController.popBackStack();
                }
        });

        mBinding.cancelRecipe.setOnClickListener(view -> {
            navController.popBackStack();
        });

        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_ingredient_measurement),
                this, (requestKey, result) -> {
            IngredientMeasurement ingredientMeasurement
                    = result.getParcelable(getString(R.string.selected_ingredient_measurement));
            mList.add(ingredientMeasurement);
        });

        return root;
    }

    private Recipe createRecipe(MainActivity activity){
        EditText temp = mBinding.recipeName;
        String title =  temp.getText().toString();

        if(title.isEmpty()){
            temp.setError(getString(R.string.recipe_name_is_empty_error));
            return null;
        }
        else{
            temp.setError(null);
        }

        String instructions = mBinding.instructions.getText().toString();
        Boolean isPrivate = mBinding.isPrivate.isChecked();
        List<IngredientMeasurement> ingredientMeasurementList = new ArrayList<>();

        ListAdapter ingredients= mBinding.ingredients.getAdapter();
        int size = ingredients.getCount();
        for(int i = 0; i < size; i++){
            ingredientMeasurementList.add((IngredientMeasurement) ingredients.getItem(i));
        }


        Recipe res = mRecipeService.createRecipe(
                title,instructions, ingredientMeasurementList, isPrivate
        );

        if (res == null){
            activity.makeToast(R.string.recipe_unknown_error, Toast.LENGTH_LONG);
            return null;
        }

        return res;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
