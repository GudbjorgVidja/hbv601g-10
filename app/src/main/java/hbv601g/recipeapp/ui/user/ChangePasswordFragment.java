package hbv601g.recipeapp.ui.user;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
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
            confirmPass();
        });

        mBinding.cancelNewPassword.setOnClickListener(v -> {
            mNavController.popBackStack();
        });

        return root;
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
                    mUserService.changePassword
                            (
                                    ((MainActivity) getActivity()).getUserId(),
                                    mBinding.newPasswordInput.getText().toString()
                            );

                    mNavController.popBackStack();
                });

                alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
                   newPassInvalid();
                   mBinding.validateNewPasswordInput.setText("");
                });

                alert.show();
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
