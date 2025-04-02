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


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentRecipesBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;



        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        long uid = mainActivity.getUserId();
        mRecipeService = new RecipeService(new NetworkingService(), uid);


        try{
            mRecipeList = mRecipeService.getAllRecipes();
        } catch (NullPointerException e){
            mRecipeList = new ArrayList<>();
            mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
        }

        mRecipeListView = mBinding.recipesListView;

        RecipeAdapter recipeAdapter = new RecipeAdapter(mainActivity.getApplicationContext(), mRecipeList);
        mRecipeListView.setAdapter(recipeAdapter);

        mRecipeListView.setOnItemClickListener((parent, view, position, id) -> {
            Recipe recipe = (Recipe) parent.getItemAtPosition(position);
            Log.d("Selected", recipe.toString());

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe), recipe);
            navController.navigate(R.id.nav_recipe, bundle);
        });



        if(mainActivity.getUserId() != 0) {
            mBinding.addRecipe.setOnClickListener(view -> {
                navController.navigate(R.id.nav_new_recipe);
            });
        }
        else{
            mBinding.addRecipe.hide();
        }

        mBinding.recipeSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mRecipeList = searchForRec();
                recipeAdapter.setList(mRecipeList);
                recipeAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    mRecipeList = searchForRec();
                    recipeAdapter.setList(mRecipeList);
                    recipeAdapter.notifyDataSetChanged();
                }
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
        else if(mSelected.equals(getString(R.string.sort_price))) updateListView(mRecipeService.getAllOrderedRecipes());
        else if(mSelected.equals(getString(R.string.sort_title))) updateListView(mRecipeService.getAllOrderedRecipesByTitle());
        else if(mSelected.equals(getString(R.string.filter_clear))){
            try {
                mRecipeList = mRecipeService.getAllRecipes();
                updateListView(mRecipeList);
            } catch (NullPointerException e) {
                mRecipeList = new ArrayList<>();
                mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_LONG);
            }
        }
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
        input.setHint("Enter max TPC");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton(R.string.confirm_filter_button, (dialog, which) -> {
            try {
                int maxTPC = Integer.parseInt(input.getText().toString());
                List<Recipe> filteredRecipes = mRecipeService.getAllRecipesUnderTPC(maxTPC + 1);
                if (filteredRecipes != null) {
                    updateListView(filteredRecipes);
                } else {
                    mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_SHORT);
                }
            } catch (NumberFormatException e) {
                mainActivity.makeToast(R.string.invalid_price_input_toast, Toast.LENGTH_SHORT);
            }
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
        input.setHint("Enter max TIC");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton(R.string.confirm_filter_button, (dialog, which) -> {
            try {
                int maxTIC = Integer.parseInt(input.getText().toString());
                List<Recipe> filteredRecipes = mRecipeService.getAllRecipesUnderTIC(maxTIC + 1);
                if (filteredRecipes != null) {
                    updateListView(filteredRecipes);
                } else {
                    mainActivity.makeToast(R.string.get_recipes_failed_toast, Toast.LENGTH_SHORT);
                }
            } catch (NumberFormatException e) {
                mainActivity.makeToast(R.string.invalid_price_input_toast, Toast.LENGTH_SHORT);
            }
        });

        alert.setNegativeButton(R.string.cancel_button_text, (dialog, which) -> dialog.cancel());
        alert.show();
    }


    /**
     * Sets the recipe list displayed in the Recipe ListView.
     * @param newList New list to send to the ListView
     */
    private void updateListView(List<Recipe> newList) {
        RecipeAdapter adapter = new RecipeAdapter(requireContext(), newList);
        mRecipeListView.setAdapter(adapter);
    }


    /**
     * Gets a search term from the UI and searches for recipes which contain it in the title
     *
     * @return a list of recipes containing the search term in the title
     */
    private List<Recipe> searchForRec() {
        String input = mBinding.recipeSearchBar.getQuery().toString();
        List<Recipe> searchResult = mRecipeService.getAllRecipes();

        if (!input.isEmpty()) searchResult = mRecipeService.SearchRecipe(input);

        if (searchResult == null) searchResult = new ArrayList<>();
        return searchResult;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
