package hbv601g.recipeapp.ui.user;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentChangePasswordBinding;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding mBinding;
    private UserService mUserService;
    private NavController mNavController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState
                            )
    {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        mBinding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();


        mUserService = new UserService(new NetworkingService());

        mNavController = Navigation.findNavController(
                mainActivity, R.id.nav_host_fragment_activity_main
        );

        mBinding.confirmNewPassword.setOnClickListener(v ->{
            confirmPass(mainActivity);
        });

        mBinding.cancelNewPassword.setOnClickListener(v -> {
            mNavController.popBackStack();
        });

        return root;
    }

    /**
     * This function set the color for the new Password and validate New Password fields as red
     *
     * @param empty : Boolean value, if one or both password filed are empty, else ture.
     */
    private void newPassInvalid(boolean empty){
        if(empty){
            mBinding.newPasswordInputLayout.setError(getString(R.string.new_password_empty_error));
            mBinding.validatePasswordInputLayout.setError(null);
        }
        else {
            mBinding.newPasswordInputLayout.setError(null);
            mBinding.validatePasswordInputLayout.setError
                    (
                            getString(R.string.new_password_invalid_error)
                    );
        }
    }

    /**
     * Creates a dialog let ask if the user if sure if they want to change there password.
     * @param activity The MainActivity of the application.
     */
    private void confirmAlert(MainActivity activity){
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(R.string.change_password_dialog_title);
        alert.setMessage(R.string.change_password_alert_message);

        if(getArguments() == null ||
                getArguments().getString(getString(R.string.selected_old_password)) == null
        ){
            Log.e("ChangePassword", "Missing old password");
            activity.makeToast(R.string.password_change_fail_toast, Toast.LENGTH_LONG);
            mNavController.popBackStack();
            return;
        }

        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mUserService.changePassword
                    (
                            activity.getUserId(),
                            mBinding.newPasswordInput.getText().toString(),
                            getArguments().getString(getString(R.string.selected_old_password))
                    );

            activity.makeToast(R.string.password_change_success_toast, Toast.LENGTH_LONG);
            mNavController.popBackStack();
        });

        alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
            mBinding.validateNewPasswordInput.setText("");
            activity.makeToast(R.string.password_change_fail_toast, Toast.LENGTH_LONG);
        });

        alert.show();
    }


    /**
     * The function checks if the new password is valid if it is the password for the user is
     * change.
     * @param activity The MainActivity of the application.
     */
    private void confirmPass(MainActivity activity){
        try {
            String nPass = mBinding.newPasswordInput.getText().toString();
            if(nPass.isEmpty()){
                newPassInvalid(true);
                return;
            }

            if(nPass.equals(mBinding.validateNewPasswordInput.getText().toString())){
                confirmAlert(activity);
            }
            else {
                newPassInvalid(false);
            }
        }
        catch (NullPointerException e){
            newPassInvalid(false);
        }
    }
}
