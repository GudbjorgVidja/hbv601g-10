package hbv601g.recipeapp.ui.user;

import android.app.AlertDialog;
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

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentUserBinding;
import hbv601g.recipeapp.exceptions.DeleteFailedException;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

public class UserFragment extends Fragment{

    private FragmentUserBinding mBinding;
    private UserService mUserService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Gæti verið betra að hafa user sem argument, og ef það er ekki til staðar þá enginn user
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mUserService = new UserService(new NetworkingService());
        mBinding = FragmentUserBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        mBinding.logoutButton.setOnClickListener(v -> mainActivity.removeCurrentUser());

        if(mainActivity.getUserName() == null){
            navController.popBackStack();
            navController.navigate(R.id.nav_user_no_user);
        }

        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        mBinding.createRecipeListButton.setOnClickListener(v -> {
            navController.navigate(R.id.navigation_new_recipe_list);
        });
        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        mBinding.deleteUserButton.setOnClickListener(v -> {
            deleteUserAlert(mainActivity);
        });
        return root;
    }

    /**
     * Creates and shows an alert dialog to confirm the deletion of a user account.
     * The user is asked for their password, and an attempt is made to delete the account,
     * unless they cancel the action
     * @param mainActivity the current activity
     */
    private void deleteUserAlert(MainActivity mainActivity) {
        EditText editText = new EditText(mainActivity.getApplicationContext());
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