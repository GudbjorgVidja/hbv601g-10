package hbv601g.recipeapp.ui.recipeLists;

import static android.view.View.GONE;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.RecipeAdapter;
import hbv601g.recipeapp.databinding.FragmentRecipeListBinding;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;

/**
 * Fragment for a single recipe list. Contains a title, the list creator, a description and a list of Recipes
 */
public class RecipeListFragment extends Fragment {
    private FragmentRecipeListBinding mBinding;
    private RecipeList mRecipeList;
    private RecipeList mClickedList;
    private RecipeListService mRecipeListService;
    private TextView mRecipeListTitle;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Clicked list is sent in a bundle to the fragment
        if(getArguments() != null){
            mClickedList = getArguments().getParcelable(getString(R.string.selected_recipe_list));
        }

        mBinding = FragmentRecipeListBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mRecipeListService = new RecipeListService(new NetworkingService(), mainActivity.getUserId());

        setListeners(mainActivity,navController);

        /*
          We use the ID of mClickedList to fetch the list from the API
          so that it will update when a recipe is added to the list while
          the list is still open.
         */
        mRecipeListService.getListById(mClickedList.getId(), new CustomCallback<>() {
            @Override
            public void onSuccess(RecipeList recipeList) {
                if(getActivity() == null) return;
                mRecipeList = recipeList;
                requireActivity().runOnUiThread(() -> {
                    setRecipeList(mainActivity,navController);
                    getRecipesFromList(mainActivity);
                });
            }

            @Override
            public void onFailure(RecipeList recipeList) {
                if(getActivity() == null) return;
                // if fetching the list fails, use mClickedList
                mRecipeList = mClickedList;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(mainActivity, getText(R.string.get_recipe_list_failed_toast), Toast.LENGTH_SHORT).show();
                    setRecipeList(mainActivity,navController);
                    getRecipesFromList(mainActivity);
                });
            }
        });

        return root;
    }

    private void setListeners(MainActivity mainActivity, NavController navController){
        if(mRecipeList != null && mRecipeList.getCreatedBy() != null){
            mBinding.recipeListCreatedBy.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                    bundle.putLong(getString(R.string.selected_user_id), mRecipeList.getCreatedBy().getId());
                    bundle.putString(getString(R.string.selected_user_name), mRecipeList.getCreatedBy().getUsername());
                    navController.navigate(R.id.nav_user_profile, bundle);
            });
        }

        if(mRecipeList != null && mRecipeList.getCreatedBy() != null && mainActivity.getUserId() != 0 &&
                mRecipeList.getCreatedBy().getId() == mainActivity.getUserId() ){
            mBinding.deleteListButton.setOnClickListener(
                    v -> makeDeleteListAlert(navController, mainActivity));
            mBinding.recipeListRenameButton.setOnClickListener(
                    v -> makeRenameAlert(mainActivity)
            );
        }
        else {
            mBinding.deleteListButton.setVisibility(GONE);
            mBinding.recipeListRenameButton.setVisibility(View.GONE);
        }

        // On click listener so the user can click and view recipes from the list
        mBinding.recipeListRecipes.setOnItemClickListener((parent, view, position, id) -> {
            Recipe recipe = (Recipe) parent.getItemAtPosition(position);
            Log.d("Selected", recipe.toString());
            makeRecipeChoiceAlert(navController, mainActivity, recipe);
        });
    }

    /**
     * Makes an alert dialog where the user can rename the recipe list. The dialog does not accept
     * an empty input when the user clicks save, if the user saves with a valid input the API is
     * called to rename the recipe list. If cancel is clicked, the dialog closes and nothing happens.
     * @param mainActivity The MainActivity of the app
     */
    private void makeRenameAlert(MainActivity mainActivity){
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(getString(R.string.title_rename_recipe_list));

        final EditText input = new EditText(mainActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(mRecipeListTitle.getText().toString());

        builder.setView(input);

        builder.setPositiveButton(getString(R.string.save_button), null);
        builder.setNegativeButton(getString(R.string.cancel_button_text), (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Listens for empty input when clicking save, makes a toast if input is empty.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String newTitle = input.getText().toString().trim();

            if (!newTitle.isEmpty()) {

                mRecipeListService.updateRecipeListTitle(mRecipeList.getId(), newTitle, new CustomCallback<>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if(getActivity() == null) return;
                        requireActivity().runOnUiThread(() -> mRecipeListTitle.setText(newTitle));
                    }

                    @Override
                    public void onFailure(Boolean aBoolean) {
                        if(getActivity() == null) return;
                        requireActivity().runOnUiThread(() ->
                                mainActivity.makeToast(R.string.rename_list_failed_toast, Toast.LENGTH_SHORT));
                    }
                });

                dialog.dismiss();
            } else {
                input.setError(getString(R.string.field_required_error));
            }
        });
    }

    /**
     * Function to set recipe list information in the UI.
     */
    private void setRecipeList(MainActivity mainActivity, NavController navController){
        mRecipeListTitle = mBinding.recipeListTitle;
        mBinding.recipeListTitle.setText(mRecipeList.getTitle());

        String tmp = mRecipeList.getCreatedBy() == null ? "Unknown" : mRecipeList.getCreatedBy().getUsername();
        mBinding.recipeListCreatedBy.setText(tmp);

        // TODO: harðkóðaðir strengir
        tmp = mRecipeList.getDescription().isEmpty() ? "No description available" : mRecipeList.getDescription();

        mBinding.recipeListDescription.setText(tmp);

    }

    private void getRecipesFromList(MainActivity mainActivity){
        mRecipeListService.getRecipesFromList(mRecipeList.getId(), new CustomCallback<>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if(getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    mRecipeList.setRecipes(recipes);
                    RecipeAdapter adapter = new RecipeAdapter(mainActivity.getApplicationContext(), mRecipeList.getRecipes());
                    mBinding.recipeListRecipes.setAdapter(adapter);
                });

            }

            @Override
            public void onFailure(List<Recipe> recipes) {
                if(getActivity() == null) return;
                requireActivity().runOnUiThread(() ->
                        // TODO: laga þetta. Harðkóðað
                        Toast.makeText(mainActivity, "Whoops, something went wrong!", Toast.LENGTH_LONG).show());
            }
        });
    }

    /**
     * Make an alert for the choices that the User make for a recipe they picked
     *
     * @param navController the navController instance
     * @param activity the current mainActivity
     * @param recipe Recipe value, is the recipe that the user picked.
     */
    private void makeRecipeChoiceAlert(NavController navController, MainActivity activity,
                                       Recipe recipe) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(recipe.getTitle());
        alert.setMessage(getString(R.string.recipe_choice_alert_message));
        alert.setNeutralButton(R.string.look_at_recipe_button, (dialog, which) -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe), recipe);
            navController.navigate(R.id.nav_recipe, bundle);
        });

        if (mRecipeList.getCreatedBy().getId() == activity.getUserId()) {
            alert.setNegativeButton(R.string.remove_button, (dialog, which) -> {
                removeRecipeAlert(navController, activity, recipe);
            });
        }

        alert.setPositiveButton(R.string.cancel_button_text, null);
        alert.show();
    }

    /**
     * Make a dialog to confirm if user wants to remove the recipe from the recipe list.
     *
     * @param navController the NavController being used for navigation.
     * @param mainActivity the MainActivity of the app.
     * @param recipe Recipe value, is the recipe that the user picked.
     */
    private void removeRecipeAlert(NavController navController, MainActivity mainActivity,
                                  Recipe recipe){
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(R.string.remove_recipe_from_recipe_list_alert_title);
        alert.setMessage(R.string.remove_recipe_from_recipe_list_alert_message);

        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mRecipeListService.removeRecipeFromList(mRecipeList, recipe, new CustomCallback<>() {
                @Override
                public void onSuccess(RecipeList recipeList) {
                    if(getActivity() == null) return;
                    mRecipeList = recipeList;
                    requireActivity().runOnUiThread(() -> {
                        mainActivity.makeToast(R.string.recipe_removed_from_list_success_toast,Toast.LENGTH_LONG);

                        // TODO: kannski passa að það þarf ekki að uppfæra öll gögn
                        mRecipeList.getRecipes().remove(recipe);
                        RecipeAdapter adapter = (RecipeAdapter) mBinding.recipeListRecipes.getAdapter();
                        adapter.setList(mRecipeList.getRecipes());
                        adapter.notifyDataSetChanged();
                        //setRecipeList(mainActivity, navController);
                    });
                }

                @Override
                public void onFailure(RecipeList recipeList) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        mainActivity.makeToast(R.string.recipe_removed_from_list_failed_toast, Toast.LENGTH_LONG);
                    });
                }
            });

        });

        alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
            makeRecipeChoiceAlert(navController, mainActivity, recipe);
        });

        alert.show();
    }

    /**
     * Makes an alert to delete this recipe list. If confirmed, the list gets deleted.
     * @param navController the navController instance
     * @param mainActivity the current mainActivity
     */
    private void makeDeleteListAlert(NavController navController, MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(getString(R.string.delete_list_alert_title));
        alert.setMessage(getString(R.string.delete_list_alert_message));
        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mRecipeListService.deleteRecipeList(mRecipeList.getId(), new CustomCallback<>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        navController.popBackStack();
                        mainActivity.makeToast(R.string.delete_list_success_toast, Toast.LENGTH_LONG);
                    });
                }

                @Override
                public void onFailure(Boolean aBoolean) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() ->
                            mainActivity.makeToast(R.string.delete_list_failed_toast, Toast.LENGTH_LONG));
                }
            });

        });

        alert.setNegativeButton(android.R.string.no, (dialog, which) -> {});
        alert.show();
    }

}
