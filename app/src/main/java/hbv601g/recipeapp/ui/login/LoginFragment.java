package hbv601g.recipeapp.ui.login;

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
import hbv601g.recipeapp.databinding.FragmentLoginBinding;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

/**
 * A fragment for the login screen
 */
public class LoginFragment extends Fragment{
    private FragmentLoginBinding mBinding;
    private UserService mUserService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mUserService = new UserService(new NetworkingService());
        mBinding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        EditText usernameInput = mBinding.usernameInput;
        EditText passwordInput = mBinding.passwordInput;

        mBinding.loginButton.setOnClickListener(v -> {
            if(Objects.requireNonNull(usernameInput.getText()).toString().isEmpty() ||
                    Objects.requireNonNull(passwordInput.getText()).toString().isEmpty()){
                mainActivity.makeToast(R.string.login_not_empty_toast, Toast.LENGTH_LONG);
            }
            else{
                mUserService.logIn(
                        usernameInput.getText().toString(),
                        passwordInput.getText().toString(),
                        new CustomCallback<>() {
                            @Override
                            public void onSuccess(User user) {
                                if(getActivity() == null) return;
                                requireActivity().runOnUiThread(() -> {
                                    mainActivity.updateCurrentUser(user);
                                    navController.popBackStack();
                                    navController.popBackStack();
                                    Bundle bundle = new Bundle();
                                    bundle.putLong(getString(R.string.selected_user_id), mainActivity.getUserId());
                                    bundle.putString(getString(R.string.selected_user_name), mainActivity.getUserName());
                                    navController.navigate(R.id.nav_user, bundle);
                                });
                            }

                            @Override
                            public void onFailure(User user) {
                                if(getActivity() == null) return;
                                requireActivity().runOnUiThread(() ->
                                        mainActivity.makeToast(R.string.login_failed_toast, Toast.LENGTH_LONG)
                                );
                            }
                        }
                );


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