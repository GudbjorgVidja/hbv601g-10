package hbv601g.recipeapp.ui.recipes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
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

/**
 * A fragment displaying an overview of recipes
 */
public class RecipesFragment extends Fragment {
    private FragmentRecipesBinding mBinding;
    private RecipeService mRecipeService;
    private List<Recipe> mRecipeList;
    private ListView mRecipeListView;
    private String mSelected;
    private RecipeAdapter mRecipeAdapter;
    private MainActivity mMainActivity;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentRecipesBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        mMainActivity = (MainActivity) getActivity();
        assert mMainActivity != null;
        NavController navController = Navigation.findNavController(mMainActivity, R.id.nav_host_fragment_activity_main);
        long uid = mMainActivity.getUserId();
        mRecipeService = new RecipeService(new NetworkingService(), uid);

        mRecipeList = new ArrayList<>();
        getAllRecipes();
        mRecipeAdapter = new RecipeAdapter(mMainActivity.getApplicationContext(), mRecipeList);

        mRecipeListView = mBinding.recipesListView;
        mRecipeListView.setAdapter(mRecipeAdapter);

        mRecipeListView.setOnItemClickListener((parent, view, position, id) -> {
            Recipe recipe = (Recipe) parent.getItemAtPosition(position);
            Log.d("Selected", recipe.toString());
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe), recipe);
            navController.navigate(R.id.nav_recipe, bundle);
        });


        if (mMainActivity.getUserId() != 0) mBinding.addRecipe.setOnClickListener(
                view -> navController.navigate(R.id.nav_new_recipe));
        else mBinding.addRecipe.hide();


        mBinding.recipeSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForRec();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) searchForRec();

                return true;
            }
        });

        return  root;
    }


    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                mainActivity,
                R.array.options_array,
                android.R.layout.simple_spinner_item);


        // Make a dropdown of options
        AutoCompleteTextView viewOptionDropdown = mBinding.viewOptionDropdown;
        viewOptionDropdown.setText(null);
        viewOptionDropdown.setAdapter(adapter);

        viewOptionDropdown.setOnItemClickListener((parent, view, position, id) -> {
            mSelected = adapter.getItem(position).toString();
            doFiltering(mainActivity);
        });
        doFiltering(mainActivity);
    }

    private void doFiltering(MainActivity mainActivity){
        if(mSelected==null) return;
        if(mSelected.equals(getString(R.string.filter_tic))) makeFilterTICAlert(mainActivity);
        else if(mSelected.equals(getString(R.string.filter_tpc))) makeFilterTPCAlert(mainActivity);
        else if(mSelected.equals(getString(R.string.sort_price))) {
            mRecipeService.getAllOrderedRecipes(new CustomCallback<>() {
                @Override
                public void onSuccess(List<Recipe> recipes) {
                    if(getActivity()==null) return;
                    mRecipeList = recipes;
                    requireActivity().runOnUiThread(() -> updateListView());
                }

                @Override
                public void onFailure(List<Recipe> recipes) {
                    Log.d("Callback", "Failed to get all ordered recipes");
                }
            });

        }
        else if(mSelected.equals(getString(R.string.sort_title))) {
            mRecipeService.getAllOrderedRecipesByTitle(new CustomCallback<>() {
                @Override
                public void onSuccess(List<Recipe> recipes) {
                    if(getActivity()==null) return;
                    mRecipeList = recipes;
                    requireActivity().runOnUiThread(() -> updateListView());

                }

                @Override
                public void onFailure(List<Recipe> recipes) {
                    Log.d("Callback", "Failed to get all recipes ordered by title");
                }
            });
        }
        else if(mSelected.equals(getString(R.string.filter_clear))){
            getAllRecipes();
        }
    }


    /**
     * Gets all recipes available to the user, and displays them in the user interface
     */
    private void getAllRecipes(){
        mRecipeService.getAllRecipes(new CustomCallback<>() {
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
                    mRecipeList = recipes;
                    updateListView();
                    mMainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
                });
            }
        });
    }


    /**
     * Alert dialog that allows the user to input a maximum TPC to filter the recipe list by.
     * The filtered list is then sent to the UI. The user can only input numbers.
     * @param mainActivity The MainActivity of the application.
     */
    private void makeFilterTPCAlert(MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.title_filter_tpc));

        final EditText input = new EditText(mainActivity);
        input.setHint(R.string.max_tpc_hint);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton(R.string.confirm_button, (dialog, which) -> {
            int maxTPC = -2;
            try {
                maxTPC = Integer.parseInt(input.getText().toString());
            } catch (NumberFormatException e) {
                mainActivity.makeToast(R.string.invalid_price_input_toast, Toast.LENGTH_SHORT);
            }

            mRecipeService.getAllRecipesUnderTPC(maxTPC + 1, new CustomCallback<>() {
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
                    requireActivity().runOnUiThread(() ->
                            mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_SHORT));
                }
            });

        });

        alert.setNegativeButton(R.string.cancel_button_text, (dialog, which) -> dialog.cancel());
        alert.show();
    }


    /**
     * Makes an Alert dialog that allows the user to input a maximum TIC to filter the recipes by.
     * The filtered list is then sent to the UI. The user can only input numbers.
     * @param mainActivity The MainActivity of the application
     */
    private void makeFilterTICAlert(MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.title_filter_tic));

        final EditText input = new EditText(mainActivity);
        input.setHint(R.string.max_tic_hint);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton(R.string.confirm_button, (dialog, which) -> {
            int maxTIC = -2;
            try {
                maxTIC = Integer.parseInt(input.getText().toString());
            } catch (NumberFormatException e) {
                mainActivity.makeToast(R.string.invalid_price_input_toast, Toast.LENGTH_SHORT);
            }

            mRecipeService.getAllRecipesUnderTIC(maxTIC + 1, new CustomCallback<>() {
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
                    requireActivity().runOnUiThread(() ->
                            mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_SHORT));
                }
            });

        });

        alert.setNegativeButton(R.string.cancel_button_text, (dialog, which) -> dialog.cancel());
        alert.show();
    }


    /**
     * Updates the recipe list displayed in the Recipe ListView.
     */
    private void updateListView(){
        mRecipeAdapter.setList(mRecipeList);
        mRecipeAdapter.notifyDataSetChanged();
    }


    /**
     * Searches for recipes with titles matching the input in the search bar, and displays them in
     * the user interface. If the searchbar is empty, all recipes available to the user are shown.
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
                        mRecipeList = recipes;
                        updateListView();
                        mMainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
                    });
                }
            });
        }


        else getAllRecipes();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
