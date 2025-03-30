package hbv601g.recipeapp.ui.recipes;

import static android.view.View.GONE;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Objects;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientMeasurementAdapter;
import hbv601g.recipeapp.databinding.FragmentRecipeBinding;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.exceptions.DeleteFailedException;
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

        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
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

        if (mRecipe != null && mRecipe.getCreatedBy() != null && mainActivity.getUserId() != 0 && mRecipe.getCreatedBy().getId() == mainActivity.getUserId()){
            mBinding.deleteRecipeButton.setOnClickListener(
                    v -> makeDeleteRecipeAlert(navController, mainActivity));

            mBinding.editRecipeButton.setOnClickListener(view ->{
                Bundle bundle = new Bundle();
                bundle.putParcelable(getString(R.string.selected_recipe), mRecipe);
                navController.navigate(R.id.nav_edit_recipe, bundle);
            });
        }
        else mBinding.deleteRecipeButton.setVisibility(GONE);

        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_edit_recipe),
                this, (requestKey, result) -> {
                    Recipe temp
                            = result.getParcelable(getString(R.string.selected_recipe));
                    if (temp != null){
                        mRecipe = temp;
                        setRecipe();
                    }
                });

        mBinding.recipeCreator.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong(getString(R.string.selected_user_id), mRecipe.getCreatedBy().getId());
            bundle.putString(getString(R.string.selected_user_name), mRecipe.getRecipeCreator());
            navController.navigate(R.id.nav_user,bundle);
        });

        return root;
    }

    /**
     * Makes and shows an alert dialog for deleting recipes. After the user confirms their action
     * an attempt is made to delete the recipe. If the user cancels the action, nothing happens
     * @param navController - the NavController being used for navigation
     * @param mainActivity - the MainActivity of the app
     */
    private void makeDeleteRecipeAlert(NavController navController, MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(getString(R.string.delete_recipe_alert_title));
        alert.setMessage(getString(R.string.delete_recipe_alert_message));
        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            try{
                mRecipeService.deleteRecipe(mRecipe.getId());
                navController.popBackStack();
                mainActivity.makeToast(R.string.delete_recipe_success, Toast.LENGTH_LONG);
            } catch (DeleteFailedException e) {
                mainActivity.makeToast(R.string.delete_recipe_failed, Toast.LENGTH_LONG);
            }
        });
        alert.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        alert.show();
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
            mBinding.addToListButton.setVisibility(GONE);
        }

        assert mainActivity != null;
        ListView ingredientMeasurementListView = mBinding.recipeIngredients;
        IngredientMeasurementAdapter adapter = new IngredientMeasurementAdapter(
                mainActivity.getApplicationContext(),
                Objects.requireNonNullElseGet(mRecipe.getIngredientMeasurements(), ArrayList::new));

        ingredientMeasurementListView.setAdapter(adapter);


        // setting the listview height to fit the contents

        int totalHeight = 0;

        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, ingredientMeasurementListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = ingredientMeasurementListView.getLayoutParams();
        params.height = totalHeight + (ingredientMeasurementListView.getDividerHeight() * (adapter.getCount() - 1));
        ingredientMeasurementListView.setLayoutParams(params);
        ingredientMeasurementListView.requestLayout();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
