package hbv601g.recipeapp.ui.recipeLists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.Objects;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentNewRecipeListBinding;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;

/**
 * Fragment for creating a new recipe list, title required and description optional.
 * The list is public by default, but can be made private.
 */
public class NewRecipeListFragment extends Fragment {
    private FragmentNewRecipeListBinding mBinding;
    private RecipeListService mRecipeListService;
    private RecipeList mRecipeList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentNewRecipeListBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = ((MainActivity) getActivity());
        assert mainActivity != null;

        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mRecipeListService = new RecipeListService(new NetworkingService(), mainActivity.getUserId());

        EditText titleInput = mBinding.newRecipeListTitleInput;
        EditText descriptionInput = mBinding.newRecipeListDescriptionInput;

        mBinding.confirmNewRecipeListButton.setOnClickListener(v -> {
            if(Objects.requireNonNull(titleInput.getText()).toString().isEmpty())
                titleInput.setError(getString(R.string.field_required_error));
            else{
                try {
                    mRecipeList = mRecipeListService.createRecipeList(
                            titleInput.getText().toString(),
                            Objects.requireNonNull(descriptionInput.getText()).toString(),
                            mBinding.newRecipeListPrivateSelection.isChecked());
                    navController.navigate(R.id.navigation_user);
                } catch (NullPointerException e){
                    mainActivity.makeToast(R.string.create_recipe_list_failed_toast, Toast.LENGTH_LONG);
                }
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
