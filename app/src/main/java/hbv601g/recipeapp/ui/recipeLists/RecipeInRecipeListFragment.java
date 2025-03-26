package hbv601g.recipeapp.ui.recipeLists;

import static android.view.View.GONE;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import hbv601g.recipeapp.databinding.FragmentRecipeInRecipeListBinding;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;
import hbv601g.recipeapp.service.RecipeService;

public class RecipeInRecipeListFragment extends Fragment {
    private FragmentRecipeInRecipeListBinding mBinding;
    private RecipeListService mRecipeListService;
    private RecipeList mList;
    private Recipe mRecipe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentRecipeInRecipeListBinding.inflate(inflater,container,false);
        View root = mBinding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity == null) {
            Log.e("RecipeFragment", "MainActivity is null. Navigation failed.");
            return root;
        }

        NavController navController = Navigation.findNavController
                (
                        mainActivity, R.id.nav_host_fragment_activity_main
                );
        mRecipeListService = new RecipeListService
                (
                        new NetworkingService(),
                        mainActivity.getUserId()
                );

        if (getArguments() == null ||
                getArguments().getParcelable(getString(R.string.selected_recipe)) == null){
            Log.e("RecipeFragment", "No recipe to view");
            return root;
        }

        mList = getArguments().getParcelable(getString(R.string.selected_recipe_list));
        mRecipe = getArguments().getParcelable(getString(R.string.selected_recipe));
        setRecipe();

        if (mRecipe != null && mList != null && mList.getCreatedBy() != null
                && mainActivity.getUserId() != 0
                && mList.getCreatedBy().getId() == mainActivity.getUserId()){
            mBinding.removeRecipeFormListButton.setOnClickListener(v -> {
                removeRecipeAlert(navController, mainActivity);
            });
        }
        else mBinding.removeRecipeFormListButton.setVisibility(GONE);

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
            RecipeService tempServ = new RecipeService
                    (
                            new NetworkingService(),
                            mainActivity.getUserId()
                    );

            double ppc = tempServ.getPersonalizedPurchaseCost(mRecipe.getId());
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

    /**
     * Make a dialog to conform if user wants to remove the recipe form the recipe list.
     *
     * @param navController : the NavController being used for navigation.
     * @param mainActivity  : the MainActivity of the app.
     */
    public void removeRecipeAlert(NavController navController, MainActivity mainActivity){
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(R.string.remove_recipe_form_recipe_list_alert_title);
        alert.setMessage(R.string.remove_recipe_form_recipe_list_alert_message);

        alert.setNegativeButton(android.R.string.no, null);
        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            if(mRecipeListService.removeRecipeFormList(mList, mRecipe)){
                mainActivity.makeToast
                        (
                                R.string.recipe_removed_form_list_success_toast,
                                Toast.LENGTH_LONG
                        );

                navController.popBackStack();
            }
            else {
                mainActivity.makeToast
                        (
                                R.string.recipe_removed_form_list_failed_toast,
                                Toast.LENGTH_LONG
                        );
            }
        });

        alert.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding =null;
    }
}
