package hbv601g.recipeapp.ui.user;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ListView;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.RecipeListAdapter;
import hbv601g.recipeapp.databinding.FragmentUserBinding;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;
import hbv601g.recipeapp.service.UserService;

public class UserFragment extends Fragment{

    private FragmentUserBinding mBinding;
    private UserService mUserService;
    private List<RecipeList> mRecipeLists;
    private RecipeListService mRecipeListService;
    private ListView mRecipeListListView;
    private NavController mNavController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Gæti verið betra að hafa user sem argument, og ef það er ekki til staðar þá enginn user
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        mNavController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mBinding = FragmentUserBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        mUserService = new UserService(new NetworkingService());
        mRecipeListService = new RecipeListService(new NetworkingService(), mainActivity.getUserId());

        if(mainActivity.getUserId() != 0){

            mRecipeListService.getUserRecipeLists(mainActivity.getUserId(), new CustomCallback<>() {
                @Override
                public void onSuccess(List<RecipeList> recipeLists) {
                    if(getActivity() == null) return;
                    mRecipeLists = recipeLists;
                    requireActivity().runOnUiThread(() -> makeView(mainActivity, navController));
                }

                @Override
                public void onFailure(List<RecipeList> recipeLists) {
                    if(getActivity() == null) return;
                    mRecipeLists = recipeLists;
                    requireActivity().runOnUiThread(() -> {
                        makeView(mainActivity, navController);
                        mainActivity.makeToast(R.string.null_recipe_lists, Toast.LENGTH_LONG);
                    });
                }
            });


        }



        mBinding.logoutButton.setOnClickListener(v -> mainActivity.removeCurrentUser());

        if(mainActivity.getUserName() == null){
            mNavController.popBackStack();
            mNavController.navigate(R.id.nav_user_no_user);
        }

        mBinding.changePasswordButton.setOnClickListener(v -> {
            changePasswordAlert(mainActivity);
        });

        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        mBinding.createRecipeListButton.setOnClickListener(v -> {
            mNavController.navigate(R.id.navigation_new_recipe_list);
        });
        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        mBinding.deleteUserButton.setOnClickListener(v -> deleteUserAlert(mainActivity));

        return root;
    }

    /**
     * Create a dialog that validate the password of the user svo that only they can change there
     * password.
     *
     * @param activity : MainActivity value, is the activity of the Fragment
     */
    public void changePasswordAlert(MainActivity activity){
        EditText oldPass = new EditText(activity.getApplicationContext());
        oldPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

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

                    mUserService.deleteAccount(mainActivity.getUserId(), password, new CustomCallback<>() {
                        @Override
                        public void onSuccess(User user) {
                            if(getActivity() == null) return;
                            requireActivity().runOnUiThread(() -> {
                                mainActivity.removeCurrentUser();
                                mainActivity.makeToast(R.string.delete_user_success_toast,Toast.LENGTH_LONG);
                            });
                        }

                        @Override
                        public void onFailure(User user) {
                            if(getActivity() == null) return;
                            requireActivity().runOnUiThread(() ->
                                    mainActivity.makeToast(R.string.delete_user_failed_toast, Toast.LENGTH_LONG));

                        }
                    });

                    alertDialog.dismiss();

                }
            });
        });

        alertDialog.show();
    }


    /**
     * Updates the recipe lists in the ui
     * @param mainActivity - the MainActivity instance
     * @param navController - the navController instance
     */
    private void makeView(MainActivity mainActivity, NavController navController){
        mRecipeListListView = mBinding.userRecipeLists;

        RecipeListAdapter recipeListAdapter = new RecipeListAdapter(mainActivity.getApplicationContext(), mRecipeLists);
        mRecipeListListView.setAdapter(recipeListAdapter);

        mRecipeListListView.setOnItemClickListener((parent, view, position, id) -> {
            RecipeList recipeList = (RecipeList) parent.getItemAtPosition(position);
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_recipe_list), recipeList);
            navController.navigate(R.id.nav_recipe_list, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
