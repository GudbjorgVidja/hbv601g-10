package hbv601g.recipeapp.ui.recipes;

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
import java.util.List;
import java.util.Objects;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientMeasurementAdapter;
import hbv601g.recipeapp.adapters.RecipeListAdapter;
import hbv601g.recipeapp.databinding.FragmentRecipeBinding;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;
import hbv601g.recipeapp.service.RecipeService;

/**
 * A fragment for single recipes
 */
public class RecipeFragment extends Fragment {
    private FragmentRecipeBinding mBinding;
    private Recipe mRecipe;
    private RecipeService mRecipeService;
    private RecipeListService mRecipeListService;

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
        mRecipeListService = new RecipeListService(new NetworkingService(), mainActivity.getUserId());

        if (getArguments() == null || getArguments().getParcelable(getString(R.string.selected_recipe)) == null){
            Log.e("RecipeFragment", "No recipe to view");
            return root;
        }

        mBinding.addToListButton.setOnClickListener(v -> makeAddToListAlert(mainActivity) );

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
        else{
            mBinding.deleteRecipeButton.setVisibility(GONE);
            mBinding.editRecipeButton.setVisibility(GONE);
        }

        getParentFragmentManager().setFragmentResultListener(getString(R.string.request_edit_recipe),
                this, (requestKey, result) -> {
                    Recipe temp = result.getParcelable(getString(R.string.selected_recipe));
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

            mRecipeService.deleteRecipe(mRecipe.getId(), new CustomCallback<>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        navController.popBackStack();
                        mainActivity.makeToast(R.string.delete_recipe_success, Toast.LENGTH_LONG);
                    });
                }

                @Override
                public void onFailure(Boolean aBoolean) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() ->
                            mainActivity.makeToast(R.string.delete_recipe_failed, Toast.LENGTH_LONG));
                }
            });


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

        // TODO: harðkóðaðir strengir
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
            mRecipeService.getPersonalizedPurchaseCost(mRecipe.getId(), new CustomCallback<>() {
                @Override
                public void onSuccess(Double ppc) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        String tmp = getString(R.string.recipe_ppc, ppc+"");
                        mBinding.recipePpc.setText(tmp);
                    });
                }

                @Override
                public void onFailure(Double ppc) {
                    // TODO: á að gera visibility á viðmótshlutnum gone, nú er eyða?
                    Log.d("Callback", "Failed to get personalized purchase cost");
                }
            });
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


    /**
     * Makes and shows an alert dialog for adding the currently open recipe to a recipe list.
     * If any lists are found for the current user, the lists are displayed in a dialog.
     * After an item is selected, an attempt is made to add the recipe to the list.
     *
     * @param mainActivity - the MainActivity of the app
     */
    private void makeAddToListAlert(MainActivity mainActivity) {

        long rid = mRecipe.getId();

        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());

        mRecipeListService.getUserRecipeLists(mainActivity.getUserId(), new CustomCallback<>() {
            @Override
            public void onSuccess(List<RecipeList> recipeLists) {
                if(getActivity() == null) return;
                if(recipeLists.isEmpty()){
                    Log.d("Callback", "No lists found");
                    // TODO: harðkóðaður texti
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(mainActivity, "No lists found", Toast.LENGTH_LONG).show());

                    return;
                }
                alert.setTitle(getString(R.string.add_recipe_to_list_dialog_title));

                RecipeListAdapter adapter = new RecipeListAdapter(getActivity(), recipeLists);

                alert.setAdapter(adapter, (dialog, which) -> {
                    long lid = ((RecipeList) adapter.getItem(which)).getId();
                    mRecipeListService.addRecipeToList(rid, lid, new CustomCallback<>() {
                        @Override
                        public void onSuccess(Integer listSize) {
                            if(getActivity() == null) return;
                            requireActivity().runOnUiThread(() -> {
                                if(listSize == ((RecipeList) adapter.getItem(which)).getRecipes().size())
                                    mainActivity.makeToast(R.string.add_recipe_to_list_existing_toast, Toast.LENGTH_LONG);
                                else
                                    mainActivity.makeToast(R.string.add_recipe_to_list_success_toast, Toast.LENGTH_LONG);
                            });
                        }

                        @Override
                        public void onFailure(Integer listSize) {
                            if(getActivity() == null) return;
                            requireActivity().runOnUiThread(() -> mainActivity.makeToast(R.string.add_recipe_to_list_failed_toast, Toast.LENGTH_LONG));
                        }
                    });

                });
                alert.setNegativeButton(getString(R.string.cancel_button_text), null);

                if(getActivity() == null) return;
                requireActivity().runOnUiThread(() -> alert.create().show());

            }

            @Override
            public void onFailure(List<RecipeList> recipeLists) {
                if(getActivity() == null) return;
                // TODO: harðkóðaður texti
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(mainActivity, "Something went wrong, No lists found", Toast.LENGTH_LONG).show());
            }
        });

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
