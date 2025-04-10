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
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

/**
 * A fragment for changing a user's password
 */
public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding mBinding;
    private UserService mUserService;
    private NavController mNavController;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mBinding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        mUserService = new UserService(new NetworkingService());

        mNavController =
                Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mBinding.confirmNewPassword.setOnClickListener(v -> confirmPass(mainActivity));
        mBinding.cancelNewPassword.setOnClickListener(v -> mNavController.popBackStack());

        return root;
    }

    /**
     * Sets errors on the input fields depending on their contents
     *
     * @param empty a boolean value indicating whether either input fields are empty
     */
    private void newPassInvalid(boolean empty) {
        if (empty) {
            mBinding.newPasswordInputLayout.setError(getString(R.string.field_required_error));
            mBinding.validatePasswordInputLayout.setError(null);
        } else {
            mBinding.newPasswordInputLayout.setError(null);
            mBinding.validatePasswordInputLayout.setError(
                    getString(R.string.new_password_invalid_error));
        }
    }

    /**
     * Creates and displays a dialog to ask the user to confirm their intention to change their
     * password
     *
     * @param activity The MainActivity of the application.
     */
    private void confirmAlert(MainActivity activity){
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(R.string.change_password_dialog_title);
        alert.setMessage(R.string.change_password_alert_message);

        if (getArguments() == null ||
                getArguments().getString(getString(R.string.selected_old_password)) == null) {
            Log.e("ChangePassword", "Missing old password");
            activity.makeToast(R.string.password_change_fail_toast, Toast.LENGTH_LONG);
            mNavController.popBackStack();
            return;
        }

        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            mUserService.changePassword(activity.getUserId(),
                    mBinding.newPasswordInput.getText().toString(),
                    getArguments().getString(getString(R.string.selected_old_password)),
                    new CustomCallback<>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            if (getActivity() == null) return;
                            requireActivity().runOnUiThread(() -> mNavController.popBackStack());
                        }

                        @Override
                        public void onFailure(Boolean aBoolean) {
                            if (getActivity() == null) return;
                            requireActivity().runOnUiThread(() -> mNavController.popBackStack());
                        }
                    });
        });

        alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
            mBinding.validateNewPasswordInput.setText("");
            activity.makeToast(R.string.password_change_fail_toast, Toast.LENGTH_LONG);
        });

        alert.show();
    }


    /**
     * Checks if the new password is valid. If it is, the password for the user is changed.
     *
     * @param activity The MainActivity of the application.
     */
    private void confirmPass(MainActivity activity){
        try {
            String nPass = mBinding.newPasswordInput.getText().toString();
            if(nPass.isEmpty()){
                newPassInvalid(true);
                return;
            }

            if (nPass.equals(mBinding.validateNewPasswordInput.getText().toString()))
                confirmAlert(activity);
            else newPassInvalid(false);
        }
        catch (NullPointerException e){
            newPassInvalid(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
