package hbv601g.recipeapp.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import hbv601g.recipeapp.entities.User;

import java.util.Objects;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentSignupBinding;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

/**
 * A fragment for the signup screen
 */
public class SignupFragment extends Fragment {
    private FragmentSignupBinding mBinding;

    private UserService mUserService;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mUserService = new UserService(new NetworkingService());
        mBinding = FragmentSignupBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mBinding.signupButton.setOnClickListener(v -> {
            String username = Objects.requireNonNull(mBinding.usernameInput.getText()).toString();
            String password = Objects.requireNonNull(mBinding.passwordInput.getText()).toString();

            if(username.isEmpty() || password.isEmpty()){
                mainActivity.makeToast(R.string.login_not_empty_toast, Toast.LENGTH_LONG);
            }
            else{
                mUserService.signup(username, password, new CustomCallback<>() {
                    @Override
                    public void onSuccess(User user) {
                        if(getActivity() == null) return;
                        requireActivity().runOnUiThread(() -> {
                            mainActivity.updateCurrentUser(user, mBinding.passwordInput.getText().toString());
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
                                mainActivity.makeToast(R.string.signup_failed_toast, Toast.LENGTH_LONG));
                    }
                });
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
