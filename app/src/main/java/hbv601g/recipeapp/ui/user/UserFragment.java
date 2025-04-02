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
import android.widget.ListView;
import android.widget.Toast;

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

/**
 * A fragment that displays a user's profile. This shows the current user's profile
 * by default (or a login/signup page if no user is logged in). By passing values
 * in a bundle while navigating to the fragment, it is possible to view the profile
 * of other users
 */
public class UserFragment extends Fragment{

    private FragmentUserBinding mBinding;
    private UserService mUserService;
    private List<RecipeList> mRecipeLists;
    private RecipeListService mRecipeListService;
    private ListView mRecipeListListView;
    private long mUidOfProfile;
    private String mNameOfProfile;
    private NavController mNavController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mNavController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mBinding = FragmentUserBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        mUserService = new UserService(new NetworkingService());
        mRecipeListService = new RecipeListService(new NetworkingService(), mainActivity.getUserId());

        getProfileInfo(mainActivity);

        if (mainActivity.getUserId() == 0 && mUidOfProfile == 0){
            setLoginView();
        }
        else setWithUserView(mainActivity);

        return root;
    }

    /**
     * gets information from arguments about which user's profile to display
     * @param mainActivity the current activity
     */
    private void getProfileInfo(MainActivity mainActivity){
        if (getArguments() != null && !getArguments().isEmpty()) {
            mUidOfProfile = getArguments().getLong(getString(R.string.selected_user_id));
            mNameOfProfile = getArguments().getString(getString(R.string.selected_user_name));
        }
        else {
            mUidOfProfile = mainActivity.getUserId();
            mNameOfProfile = mainActivity.getUserName();
        }
    }


    /**
     * sets the visibility of UI components to display the login and signup page,
     * and adds listeners to buttons.
     */
    private void setLoginView(){
        mBinding.usernameDisplay.setVisibility(GONE);
        mBinding.createRecipeListButton.setVisibility(GONE);
        mBinding.logoutButton.setVisibility(GONE);
        mBinding.userRecipeListSection.setVisibility(GONE);
        mBinding.userRecipeLists.setVisibility(GONE);
        mBinding.deleteUserButton.setVisibility(GONE);
        mBinding.changePasswordButton.setVisibility(GONE);
        mBinding.noUserButtonLayout.setVisibility(VISIBLE);
        mBinding.loginButton.setVisibility(VISIBLE);
        mBinding.signupButton.setVisibility(VISIBLE);

        mBinding.loginButton.setOnClickListener(v -> mNavController.navigate(R.id.nav_login));
        mBinding.signupButton.setOnClickListener(v -> mNavController.navigate(R.id.nav_signup));
    }

    /**
     * Sets the visibility of UI components to display a user profile (as opposed to displaying
     * the login/signup page). Displays the username connected to the displayed profile,
     * populates the list of recipe lists and displays them. Adds relevant listeners
     * @param mainActivity the current activity
     */
    private void setWithUserView(MainActivity mainActivity){
        mBinding.usernameDisplay.setVisibility(VISIBLE);
        mBinding.userRecipeListSection.setVisibility(VISIBLE);
        mBinding.userRecipeLists.setVisibility(VISIBLE);
        mBinding.noUserButtonLayout.setVisibility(GONE);
        mBinding.loginButton.setVisibility(GONE);
        mBinding.signupButton.setVisibility(GONE);

        mBinding.usernameDisplay.setText(mNameOfProfile);

        populateRecipeListOverview(mainActivity);

        if(mUidOfProfile == mainActivity.getUserId() || mUidOfProfile==0){
            displayOwnProfile(mainActivity);
        }
        else displayOtherUserProfile();
    }

    /**
     * Populates the list of recipe lists and displays them. Adds a listener to react to
     * list items being selected
     * @param mainActivity the current activity
     */
    private void populateRecipeListOverview(MainActivity mainActivity) {
        try {
            mRecipeLists = mRecipeListService.getUserRecipeLists(mUidOfProfile);
        } catch(NullPointerException e) {
            mRecipeLists = new ArrayList<>();
            mainActivity.makeToast(R.string.null_recipe_lists, Toast.LENGTH_LONG);
        }

        mRecipeListListView = mBinding.userRecipeLists;
        RecipeListAdapter recipeListAdapter = new RecipeListAdapter(mainActivity.getApplicationContext(), mRecipeLists);
        mRecipeListListView.setAdapter(recipeListAdapter);

        mRecipeListListView.setOnItemClickListener((parent, view, position, id) -> {
            RecipeList recipeList = (RecipeList) parent.getItemAtPosition(position);
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe_list), recipeList);
            mNavController.navigate(R.id.nav_recipe_list, bundle);
        });
    }

    /**
     * Sets the visibility of UI components specific to users viewing their own profiles
     * (but not when users are viewing profiles of other users). Sets listeners specific
     * to the same situation.
     *
     * @param mainActivity the current activity
     */
    private void displayOwnProfile(MainActivity mainActivity){
        mBinding.createRecipeListButton.setVisibility(VISIBLE);
        mBinding.logoutButton.setVisibility(VISIBLE);
        mBinding.deleteUserButton.setVisibility(VISIBLE);
        mBinding.changePasswordButton.setVisibility(VISIBLE);

        mBinding.logoutButton.setOnClickListener(v -> mainActivity.removeCurrentUser());
        mBinding.changePasswordButton.setOnClickListener(v -> changePasswordAlert(mainActivity));
        mBinding.deleteUserButton.setOnClickListener(v -> deleteUserAlert(mainActivity));
        mBinding.createRecipeListButton.setOnClickListener(
                v -> mNavController.navigate(R.id.navigation_new_recipe_list));
    }

    /**
     * Sets the visibility of UI components specific to users viewing the profile of another
     * user (but not when users are viewing their own profiles)
     */
    private void displayOtherUserProfile(){
        mBinding.createRecipeListButton.setVisibility(GONE);
        mBinding.logoutButton.setVisibility(GONE);
        mBinding.deleteUserButton.setVisibility(GONE);
        mBinding.changePasswordButton.setVisibility(GONE);
    }


    /**
     * Create a dialog that validate the password of the user svo that only they can change there
     * password.
     *
     * @param activity : MainActivity value, is the activity of the Fragment
     */
    public void changePasswordAlert(MainActivity activity){
        EditText oldPass = new EditText(activity.getApplicationContext());
        oldPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(R.string.validate_current_password_title);
        alert.setMessage(R.string.validate_current_password_alert_message);
        alert.setView(oldPass);
        alert.setPositiveButton(R.string.confirm_button, null);
        alert.setNegativeButton(R.string.cancel_button_text, null);

        AlertDialog dialog = alert.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String password = oldPass.getText().toString();
                if(password.isEmpty()) {
                    oldPass.setError(getString(R.string.validate_current_password_alert_error));
                }
                else {
                    if(mUserService.validatePassword(activity.getUserId(), password)){
                        dialog.dismiss();
                        mNavController.navigate(R.id.nav_change_password);
                    }
                    else {
                        oldPass.setText("");
                        activity.makeToast(R.string.password_invalid_toast,Toast.LENGTH_LONG);
                    }
	        }
            });
        });

        dialog.show();
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
        alert.setTitle(getString(R.string.delete_user_alert_title));
        alert.setMessage(getString(R.string.delete_user_alert_message));
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