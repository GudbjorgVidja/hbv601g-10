package hbv601g.recipeapp.ui.recipes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
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
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

/**
 * A fragment to create new recipes
 */
public class NewRecipeFragment extends Fragment {
    private RecipeService mRecipeService;
    private FragmentNewRecipeBinding mBinding;
    private List<IngredientMeasurement> mIngredientList = new ArrayList<>();
    private int mTotalHeight = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentNewRecipeBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController =
                Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mRecipeService = new RecipeService(new NetworkingService(), mainActivity.getUserId());

        IngredientMeasurementAdapter adapter = new IngredientMeasurementAdapter(
                mainActivity.getApplicationContext(), mIngredientList);
        mBinding.ingredients.setAdapter(adapter);

        setListeners(mainActivity,navController);

        ListView ingredientsList = mBinding.ingredients;

        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_ingredient_measurement),
                this, (requestKey, result) -> {
            IngredientMeasurement ingredientMeasurement =
                    result.getParcelable(getString(R.string.selected_ingredient_measurement));

            mIngredientList.add(ingredientMeasurement);
            View listItem = adapter.getView(adapter.getCount() - 1, null, mBinding.ingredients);
            listItem.measure(0, 0);
            mTotalHeight += listItem.getMeasuredHeight() + ingredientsList.getDividerHeight();

            ViewGroup.LayoutParams params = ingredientsList.getLayoutParams();
            params.height = mTotalHeight ;
            ingredientsList.setLayoutParams(params);
            ingredientsList.requestLayout();
        });

        setHeight();
        return root;
    }

    /**
     * Sets listeners on various buttons and other UI components.
     *
     * @param mainActivity the current activity
     * @param navController the NavController used to navigate between fragments
     */
    private void setListeners(MainActivity mainActivity, NavController navController){
        mBinding.addIngredient.setOnClickListener(view ->
                navController.navigate(R.id.nav_add_ingredient_measurement_to_recipe));

        mBinding.createRecipe.setOnClickListener(view -> createRecipe(navController));

        mBinding.cancelRecipe.setOnClickListener(view -> navController.popBackStack());

        ListView ingredientsList = mBinding.ingredients;

        IngredientMeasurementAdapter adapter =
                (IngredientMeasurementAdapter) ingredientsList.getAdapter();
        ingredientsList.setOnItemClickListener((parent, view, position, id) -> {
            removeIngredientAlert(mainActivity, adapter,
                    (IngredientMeasurement) parent.getItemAtPosition(position), position);
        });

        //This make a listener to revel the tool tip on a long tip
        mBinding.removeIngredientsToolTip.performLongClick();

    }

    /**
     * Sets the height of the listview to fit the contents
     */
    private void setHeight(){
        ListView ingredientsList = mBinding.ingredients;
        IngredientMeasurementAdapter adapter =
                (IngredientMeasurementAdapter) ingredientsList.getAdapter();
        mTotalHeight = 0;

        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, ingredientsList);
            listItem.measure(0, 0);
            mTotalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = ingredientsList.getLayoutParams();
        params.height = mTotalHeight + (ingredientsList.getDividerHeight() * (adapter.getCount() - 1));
        ingredientsList.setLayoutParams(params);
        ingredientsList.requestLayout();
    }

    /**
     * Uses information from the UI to create a new recipe, and updates the UI based on the result.
     *
     * @param navController - The NavController
     */
    private void createRecipe(NavController navController){
        EditText temp = mBinding.recipeName;
        String title =  temp.getText().toString();

	    if(title.isEmpty()){
            temp.setError(getString(R.string.field_required_error));
            return;
        }

        String instructions = mBinding.instructions.getText().toString();
        boolean isPrivate = mBinding.isPrivate.isChecked();

        mRecipeService.createRecipe(title, instructions, mIngredientList, isPrivate, new CustomCallback<>() {
            @Override
            public void onSuccess(Recipe recipe) {
                if(getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), R.string.create_recipe_success_toast, Toast.LENGTH_LONG).show();
                    navController.popBackStack();
                });
            }

            @Override
            public void onFailure(Recipe recipe) {
                if(getActivity() == null) return;
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

    /**
     * Make a Dialog, that asks the user if they want to remove the ingredient
     * @param activity The MainActivity of the app
     * @param adapter Is the adapter for IngredientMeasurement list,
     * @param ingerd Is the IngredientMeasurement that is being removed
     * @param position Is the position of the IngredientMeasurement in the adapter that is being
     *                 remove
     */
    private void removeIngredientAlert(MainActivity activity, IngredientMeasurementAdapter adapter,
                                       IngredientMeasurement ingerd, int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(ingerd.getIngredient().getTitle());

        alert.setMessage(R.string.remove_ingredient_measurement_alert_message);

        alert.setPositiveButton(R.string.remove_button, (dialog, which) -> {

            ListView ingredientsList = mBinding.ingredients;
            View listItem = adapter.getView(position, null, ingredientsList);
            listItem.measure(0, 0);

            mTotalHeight -= (listItem.getMeasuredHeight()+ingredientsList.getDividerHeight());

            ViewGroup.LayoutParams params = ingredientsList.getLayoutParams();
            params.height = mTotalHeight;
            ingredientsList.setLayoutParams(params);
            ingredientsList.requestLayout();

            mIngredientList.remove(ingerd);

            adapter.setList(mIngredientList);
            adapter.notifyDataSetChanged();
        });

        alert.setNegativeButton(R.string.cancel_button_text, null);
        alert.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
