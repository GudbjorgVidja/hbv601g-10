package hbv601g.recipeapp.ui.user;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.viewmodel.CreationExtras;
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
    private boolean mPasswordValid;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState
                            )
    {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        mBinding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        mPasswordValid = false;
        setView();

        mUserService = new UserService(new NetworkingService());

        NavController navController = Navigation.findNavController(
                mainActivity, R.id.nav_host_fragment_activity_main
        );

        mBinding.checkIfPasswordIsValid.setOnClickListener(v -> {
            validatePass();
            setView();

            if(mPasswordValid){
                mBinding.currentPasswordInput.setTextColor(Color.GREEN);
            }
            else {
                mBinding.currentPasswordInput.setTextColor(Color.RED);
                Toast.makeText(
                        getActivity(), R.string.password_invalid_toast, Toast.LENGTH_SHORT
                ).show();
            }
        });


        mBinding.confirmNewPassword.setOnClickListener(v ->{
                if(mPasswordValid) {confirmPass();}
        });

        return root;
    }

    /**
     * This function set the Visibility of confirm button, newPasswordInput and
     * validatePasswordInput field.
     */
    private void setView(){
        if(mPasswordValid) {
            mBinding.confirmNewPassword.setVisibility(VISIBLE);
            mBinding.newPasswordInputLayout.setVisibility(VISIBLE);
            mBinding.validateNewPasswordInput.setVisibility(VISIBLE);

            mBinding.currentPasswordInput.setEnabled(false);
            mBinding.checkIfPasswordIsValid.setVisibility(GONE);
        }
        else {
            mBinding.confirmNewPassword.setVisibility(GONE);
            mBinding.newPasswordInputLayout.setVisibility(GONE);
            mBinding.validateNewPasswordInput.setVisibility(GONE);
        }
    }

    /**
     * The function of the see if the pass word is the same as the password that the user has.
     */
    private void validatePass(){
        try{
            String input = mBinding.currentPasswordInput.getText().toString();
            mPasswordValid = true; // todo call servis
        }
        catch (NullPointerException e){
            mPasswordValid = false;
        }
    }

    /**
     * This function set the color for the new Password and validate New Password fields as red
     */
    private void newPassInvalid(){
        mBinding.newPasswordInput.setTextColor(Color.RED);
        mBinding.validateNewPasswordInput.setTextColor(Color.RED);
    }

    /**
     * The function checks if the new password is valid if it is the password for the user is
     * change.
     */
    private void confirmPass(){
        try {
            String nPass = mBinding.newPasswordInput.getText().toString();

            if(nPass.equals(mBinding.validateNewPasswordInput.getText().toString())){
                AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
                alert.setTitle(R.string.change_password_dialog_title);
                alert.setMessage(R.string.change_password_alert_message);

                alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // todo call saves for password;
                    // todo if stamet if it was sucksefull or not
                });

                alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
                   newPassInvalid();
                   mBinding.validateNewPasswordInput.setText("");
                });
            }
            else {
                newPassInvalid();
                Toast.makeText(
                        getActivity(), R.string.new_password_invalid_toast,
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
        catch (NullPointerException e){
            newPassInvalid();
            Toast.makeText(
                    getActivity(), R.string.password_has_no_input_toast, Toast.LENGTH_SHORT
            ).show();
        }
    }
}
