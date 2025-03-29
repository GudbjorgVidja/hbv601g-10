package hbv601g.recipeapp.ui.user;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListView;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.RecipeListAdapter;
import hbv601g.recipeapp.databinding.FragmentUserBinding;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;
import hbv601g.recipeapp.exceptions.DeleteFailedException;
import hbv601g.recipeapp.service.UserService;

public class UserFragment extends Fragment{

    private FragmentUserBinding mBinding;
    private UserService mUserService;
    private List<RecipeList> mRecipeLists;
    private RecipeListService mRecipeListService;
    private ListView mRecipeListListView;
    private long mUidOfProfile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Gæti verið betra að hafa user sem argument, og ef það er ekki til staðar þá enginn user
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mUserService = new UserService(new NetworkingService());
        mBinding = FragmentUserBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        mRecipeListService = new RecipeListService(new NetworkingService(), mainActivity.getUserId());

        if (getArguments() != null) {
            mUidOfProfile= getArguments().getLong(getString(R.string.selected_user)) ;
        }
        else mUidOfProfile = 0;


        if(mainActivity.getUserId() != 0) setWithUserView(mainActivity,navController);
        else setNoUserView(mainActivity,navController);


        return root;
    }

    private void setNoUserView(MainActivity mainActivity, NavController navController){
        // Set visibility of UI components
        mBinding.usernameDisplay.setVisibility(GONE);
        mBinding.createRecipeListButton.setVisibility(GONE);
        mBinding.logoutButton.setVisibility(GONE);
        mBinding.userRecipeListSection.setVisibility(GONE);
        mBinding.userRecipeLists.setVisibility(GONE);
        mBinding.deleteUserButton.setVisibility(GONE);
        mBinding.noUserButtonLayout.setVisibility(VISIBLE);
        mBinding.loginButton.setVisibility(VISIBLE);
        mBinding.signupButton.setVisibility(VISIBLE);

        // Set button listeners
        mBinding.loginButton.setOnClickListener(v -> navController.navigate(R.id.nav_login));
        mBinding.signupButton.setOnClickListener(v -> navController.navigate(R.id.nav_signup));
    }

    private void setWithUserView(MainActivity mainActivity, NavController navController){
        // Set visibility of UI components
        mBinding.usernameDisplay.setVisibility(VISIBLE);
        mBinding.userRecipeListSection.setVisibility(VISIBLE);
        mBinding.userRecipeLists.setVisibility(VISIBLE);
        mBinding.noUserButtonLayout.setVisibility(GONE);
        mBinding.loginButton.setVisibility(GONE);
        mBinding.signupButton.setVisibility(GONE);

        // Populate the list of recipe lists and display them
        try {
            mRecipeLists = mRecipeListService.getUserRecipeLists(mainActivity.getUserId());
        } catch(NullPointerException e) {
            mRecipeLists = new ArrayList<>();
            mainActivity.makeToast(R.string.null_recipe_lists, Toast.LENGTH_LONG);
        }

        mRecipeListListView = mBinding.userRecipeLists;
        RecipeListAdapter recipeListAdapter = new RecipeListAdapter(mainActivity.getApplicationContext(), mRecipeLists);
        mRecipeListListView.setAdapter(recipeListAdapter);

        // Set the on click listener for the recipe list list view
        mRecipeListListView.setOnItemClickListener((parent, view, position, id) -> {
            RecipeList recipeList = (RecipeList) parent.getItemAtPosition(position);
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe_list), recipeList);
            navController.navigate(R.id.nav_recipe_list, bundle);
        });

        if(mUidOfProfile == mainActivity.getUserId() || mUidOfProfile==0){
            ownProfile(mainActivity,navController);
        }
        else setOtherProfile(mainActivity,navController);
    }

    private void ownProfile(MainActivity mainActivity, NavController navController){
        // Set visibility of UI components
        mBinding.createRecipeListButton.setVisibility(VISIBLE);
        mBinding.logoutButton.setVisibility(VISIBLE);
        mBinding.deleteUserButton.setVisibility(VISIBLE);


        // Set the username
        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        // Set listeners on buttons
        mBinding.logoutButton.setOnClickListener(v -> mainActivity.removeCurrentUser());

        mBinding.createRecipeListButton.setOnClickListener(
                v -> navController.navigate(R.id.navigation_new_recipe_list));

        mBinding.deleteUserButton.setOnClickListener(v -> deleteUserAlert(mainActivity));
    }

    private void setOtherProfile(MainActivity mainActivity, NavController navController){
            // Set visibility of UI components
            mBinding.createRecipeListButton.setVisibility(GONE);
            mBinding.logoutButton.setVisibility(GONE);
            mBinding.deleteUserButton.setVisibility(GONE);

        // Set the username
        mBinding.usernameDisplay.setText("That bitch");
    }



    /**
     * Creates and shows an alert dialog to confirm the deletion of a user account.
     * The user is asked for their password, and an attempt is made to delete the account,
     * unless they cancel the action
     * @param mainActivity the current activity
     */
    private void deleteUserAlert(MainActivity mainActivity) {
        EditText editText = new EditText(mainActivity.getApplicationContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(getString(R.string.delete_user_title));
        alert.setMessage(getString(R.string.delete_user_message));
        alert.setView(editText);
        alert.setPositiveButton(getString(R.string.confirm_button), null);
        alert.setNegativeButton(getString(R.string.cancel_button_text), null);

        AlertDialog alertDialog = alert.create();
        alertDialog.setOnShowListener(dialog -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String password = editText.getText().toString().trim();
                if (password.isEmpty()) editText.setError(getString(R.string.field_required_error));
                else{
                    try {
                        mUserService.deleteAccount(mainActivity.getUserId(), password);
                        mainActivity.removeCurrentUser();
                        mainActivity.makeToast(R.string.delete_user_success_toast,Toast.LENGTH_LONG);
                    } catch (DeleteFailedException e) {
                        mainActivity.makeToast(R.string.delete_user_failed_toast, Toast.LENGTH_LONG);
                    }
                    alertDialog.dismiss();
                }
            });
        });

        alertDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}