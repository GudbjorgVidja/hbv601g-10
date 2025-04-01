package hbv601g.recipeapp.ui.recipes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.RecipeAdapter;
import hbv601g.recipeapp.databinding.FragmentRecipesBinding;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding mBinding;
    private RecipeService mRecipeService;
    private List<Recipe> mRecipeList;
    private ListView mRecipeListView;

    private RecipeAdapter mRecipeAdapter;
    private MainActivity mMainActivity;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentRecipesBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        mMainActivity = (MainActivity) getActivity();
        assert mMainActivity != null;

        Button filterTpcButton = mBinding.filterTpcButton;
        Button filterTicButton = mBinding.filterTicButton;
        Button clearFilterButton = mBinding.clearFilterButton;
        Button sortByPriceButton = mBinding.sortByPriceButton;
        Button sortByTitleButton = mBinding.sortByTitleButton;

        NavController navController = Navigation.findNavController(mMainActivity, R.id.nav_host_fragment_activity_main);

        long uid = mMainActivity.getUserId();
        mRecipeService = new RecipeService(new NetworkingService(), uid);

        mRecipeList = new ArrayList<>();
        getAllRecipes();
        mRecipeAdapter = new RecipeAdapter(mMainActivity.getApplicationContext(), mRecipeList);

        mRecipeListView = mBinding.recipesListView;
        mRecipeListView.setAdapter(mRecipeAdapter);

        // Þarf updateListView() hér?

        mRecipeListView.setOnItemClickListener((parent, view, position, id) -> {
            Recipe recipe = (Recipe) parent.getItemAtPosition(position);
            Log.d("Selected", recipe.toString());
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe), recipe);
            navController.navigate(R.id.nav_recipe, bundle);
        });


        mBinding.recipeSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForRec();
                //mRecipeList = searchForRec();
                //recipeAdapter.setList(mRecipeList);
                //recipeAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    searchForRec();
                    //mRecipeList = searchForRec();
                    //recipeAdapter.setList(mRecipeList);
                    //recipeAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });


        filterTpcButton.setOnClickListener(v -> makeFilterTPCAlert(mMainActivity));
        filterTicButton.setOnClickListener(v -> makeFilterTICAlert(mMainActivity));
        filterTpcButton.setOnClickListener(v -> makeFilterTPCAlert(mainActivity));
        filterTicButton.setOnClickListener(v -> makeFilterTICAlert(mainActivity));
        sortByPriceButton.setOnClickListener(v -> updateListView(mRecipeService.getAllOrderedRecipes()));
        sortByTitleButton.setOnClickListener(v -> updateListView(mRecipeService.getAllOrderedRecipesByTitle()));

        clearFilterButton.setOnClickListener(v -> {
            // uppfærist í aðferðinni
            getAllRecipes();
            /*
            try {
                mRecipeList = mRecipeService.getAllRecipes();
                updateListView(mRecipeList);
            } catch (NullPointerException e) {
                mRecipeList = new ArrayList<>();
                mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
            }
             */
        });


        if(mMainActivity.getUserId() != 0) {
            mBinding.addRecipe.setOnClickListener(view ->
                navController.navigate(R.id.nav_new_recipe));
        }
        else{
            mBinding.addRecipe.hide();
        }

        return  root;
    }


    private void getAllRecipes(){
        mRecipeService.getAllRecipes(new CustomCallback<>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if(getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    mRecipeList = recipes;
                    updateListView();
                    //mRecipeAdapter.notifyDataSetChanged();
                    //makeRecipesView(mainActivity, navController);
                    // eða updateListView????
                });
            }

            @Override
            public void onFailure(List<Recipe> recipes) {
                if(getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    mRecipeList = recipes; // Getum líka sleppt uppfærslu
                    updateListView(); // Viljum við þetta?
                    mMainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
                });
            }
        });
    }

    /**
     * Alert dialog that allows the user to input a maximum TPC to filter the recipe list by.
     * The filtered list is then sent to the UI. The user can only input numbers.
     * @param mainActivity - The MainActivity of the application.
     */
    private void makeFilterTPCAlert(MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.title_filter_tpc));

        final EditText input = new EditText(mainActivity);
        input.setHint("Enter max TPC"); // TODO: harðkóðaður strengur
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton(R.string.confirm_filter_button, (dialog, which) -> {
            // TODO: hafa check hér? input er number?
            int maxTPC = -2;
            try {
                maxTPC = Integer.parseInt(input.getText().toString());
            } catch (NumberFormatException e) {
                mainActivity.makeToast(R.string.invalid_price_input_toast, Toast.LENGTH_SHORT);
            }

            mRecipeService.getAllRecipesUnderTPC(maxTPC + 1, new CustomCallback<>() {
                @Override
                public void onSuccess(List<Recipe> recipes) {
                    requireActivity().runOnUiThread(() -> {
                        updateListView(recipes);
                    });
                }

                @Override
                public void onFailure(List<Recipe> recipes) {
                    requireActivity().runOnUiThread(() ->
                            mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_SHORT));
                }
            });

        });

        alert.setNegativeButton(R.string.cancel_button_text, (dialog, which) -> dialog.cancel());
        alert.show();
    }


    /**
     * Alerti dialog that allows the user to input a maximum TIC to tilfet the recipe list by.
     * The filtered list is then sent to the UI. The user can only input numbers.
     * @param mainActivity - The MainActivity of the application
     */
    private void makeFilterTICAlert(MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.title_filter_tic));

        final EditText input = new EditText(mainActivity);
        input.setHint("Enter max TIC");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton(R.string.confirm_filter_button, (dialog, which) -> {
            int maxTIC = -2;
            try {
                maxTIC = Integer.parseInt(input.getText().toString());
            } catch (NumberFormatException e) {
                mainActivity.makeToast(R.string.invalid_price_input_toast, Toast.LENGTH_SHORT);
            }

            mRecipeService.getAllRecipesUnderTIC(maxTIC + 1, new CustomCallback<>() {
                @Override
                public void onSuccess(List<Recipe> recipes) {
                    requireActivity().runOnUiThread(() -> {
                        updateListView(recipes);
                    });
                }

                @Override
                public void onFailure(List<Recipe> recipes) {
                    requireActivity().runOnUiThread(() ->
                            mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_SHORT));
                }
            });

        });

        alert.setNegativeButton(R.string.cancel_button_text, (dialog, which) -> dialog.cancel());
        alert.show();
    }


    /**
     * Method that sets the recipe list displayed in the Recipe ListView.
     * @param newList - New list to send to the ListView
     */
    private void updateListView(List<Recipe> newList) {
        RecipeAdapter adapter = new RecipeAdapter(requireContext(), newList);
        mRecipeListView.setAdapter(adapter);
    }

    private void updateListView(){
        mRecipeAdapter.setList(mRecipeList);
        mRecipeAdapter.notifyDataSetChanged();
    }


    /**
     * This function Search for the recipe with the title that the user input in the Search bar.
     *
     * @return a list of recipe that have the title of the recipe in the Search bar or if the
     *         Search bar is empty then it returns all of the recipes that the user can see.
     */
    private void searchForRec() {
        String input = mBinding.recipeSearchBar.getQuery().toString();
        if (!input.isEmpty()){
            mRecipeService.SearchRecipe(input, new CustomCallback<>() {
                @Override
                public void onSuccess(List<Recipe> recipes) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        mRecipeList = recipes;
                        updateListView();
                    });
                }

                @Override
                public void onFailure(List<Recipe> recipes) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        mRecipeList = recipes; // Getum líka sleppt uppfærslu
                        updateListView(); // Viljum við þetta?
                        // TODO: eða getAll hér?
                        mMainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
                    });
                }
            });
        }


        else getAllRecipes();


        // TODO: Viljum við byrja að setja alltaf all recipes, og svo leita?
        /*
        List<Recipe> searchResult = mRecipeService.getAllRecipes();

        if (!input.isEmpty()) searchResult = mRecipeService.SearchRecipe(input);

        if (searchResult == null) searchResult = new ArrayList<>();
        return searchResult;
         */
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
