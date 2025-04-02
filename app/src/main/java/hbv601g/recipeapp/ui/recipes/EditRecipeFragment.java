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
import hbv601g.recipeapp.databinding.FragmentEditRecipeBinding;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

/**
 * A fragment to edit recipes
 */
public class EditRecipeFragment extends Fragment {
    private RecipeService mRecipeService;
    private FragmentEditRecipeBinding mBinding;
    private IngredientMeasurementAdapter mAdapter;
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

        if(getArguments() == null ||
                getArguments().getParcelable(getString(R.string.selected_recipe)) == null
        ){
            Log.e("EditRecipeFragment", "No recipe to edit");
            navController.popBackStack();
        }

        try {
            mRecipe = getArguments().getParcelable(getString(R.string.selected_recipe));
            if (mRecipe.getCreatedBy() == null) {
                Log.e("EditRecipeFragment", "No ones owns this");
                navController.popBackStack();
            }
        }
        catch (NullPointerException e){
            mainActivity.makeToast(R.string.recipe_missing_toast, Toast.LENGTH_LONG);
            navController.popBackStack();
        }

        setEdit(mainActivity);
        List<IngredientMeasurement> newIngredients = new ArrayList<>();
        List<IngredientMeasurement> removedIngredients = new ArrayList<>();

        mBinding.addIngredient.setOnClickListener(view -> {
            navController.navigate(R.id.nav_add_ingredient_measurement_to_recipe);
        });

        mBinding.ingredients.setOnItemClickListener((parent, view, position, id) -> {
            removeIngredientAlert(
                    mainActivity,
                    removedIngredients,
                    (IngredientMeasurement) parent.getItemAtPosition(position)
            );
        });

        mBinding.cancelEditRecipe.setOnClickListener(view -> {
            if(!removedIngredients.isEmpty()){
                mList.addAll(removedIngredients);
            }

            if(!newIngredients.isEmpty()){
                mList.removeAll(newIngredients);
            }
            navController.popBackStack();
        });

        mBinding.editRecipe.setOnClickListener(view -> {
            Recipe recipe = editRecipe(mainActivity);
            if (recipe != null) {
                Bundle res = new Bundle();
                res.putParcelable(getString(R.string.selected_recipe), recipe);

                getParentFragmentManager().setFragmentResult
                        (
                                getString(R.string.request_edit_recipe), res
                        );

                navController.popBackStack();
            }
            else{
                Toast.makeText(
                        getActivity(), R.string.recipe_edit_unknown_error, Toast.LENGTH_LONG
                ).show();
            }
        });

        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_ingredient_measurement),
        this, (requestKey, result) -> {
            IngredientMeasurement ingredientMeasurement
                    = result.getParcelable(getString(R.string.selected_ingredient_measurement));
            mList.add(ingredientMeasurement);
            newIngredients.add(ingredientMeasurement);
        });

        return root;
    }

    /**
     * Displays the information about the recipe that should be edited in the UI
     *
     * @param activity the current activity.
     */
    private void setEdit(MainActivity activity){
        mBinding.recipeName.setText(mRecipe.getTitle());
        mBinding.instructions.setText(mRecipe.getInstructions());

        mList = mRecipe.getIngredientMeasurements();
        if(mList == null){mList = new ArrayList<>();}

        ListView ingredientsList = mBinding.ingredients;
        mAdapter = new IngredientMeasurementAdapter
                (
                        activity.getApplicationContext(),
                        mList
                );
        ingredientsList.setAdapter(mAdapter);

        mBinding.isPrivate.setChecked(mRecipe.isPrivate());

        // setting the listview height to fit the contents

        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View listItem = mAdapter.getView(i, null, ingredientsList);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = ingredientsList.getLayoutParams();
        params.height = totalHeight + (ingredientsList.getDividerHeight() * (mAdapter.getCount() - 1));
        ingredientsList.setLayoutParams(params);
        ingredientsList.requestLayout();

    }

    /**
     * Gets information from the UI and uses it to update the recipe
     *
     * @param activity the current activity
     * @return the updated recipe if possible else return null
     */
    private Recipe editRecipe(MainActivity activity){
        if(mRecipe.getCreatedBy().getId() != activity.getUserId()){
            return null;
        }

        EditText temp = mBinding.recipeName;
        String title =  temp.getText().toString();

        if(title.isEmpty()){
            temp.setError(getString(R.string.recipe_name_is_empty_error));
            return null;
        }
        else{
            temp.setError(null);
        }
        
        Recipe upRes = new Recipe();
        upRes.setTitle(title);
        upRes.setInstructions(mBinding.instructions.getText().toString());
        upRes.setPrivate(mBinding.isPrivate.isChecked());

        return mRecipeService.updateRecipe(upRes, mRecipe.getId(), mList);
    }

    /**
     * Make a Dialog, that asks the user if they want to remove the ingredient
     * @param activity The MainActivity of the app
     * @param removeList Is a list that contains all IngredientMeasurement that have been removed
     *                   in the edit so far.
     * @param ingerd Is the IngredientMeasurement that is being removed
     */
    private void removeIngredientAlert
    (
            MainActivity activity ,
            List<IngredientMeasurement> removeList,
            IngredientMeasurement ingerd
    ) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(
                String.format(
                        getString(R.string.remove_ingredient_measurement_alert_title),
                        ingerd.getIngredient().getTitle()
                )
        );

        alert.setMessage(R.string.remove_ingredient_measurement_alert_message);

        alert.setPositiveButton(R.string.remove_button, (dialog, which) -> {
            mList.remove(ingerd);
            removeList.add(ingerd);

            mAdapter.setList(mList);
            mAdapter.notifyDataSetChanged();
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
