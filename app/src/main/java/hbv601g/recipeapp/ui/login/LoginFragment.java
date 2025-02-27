package hbv601g.recipeapp.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentLoginBinding;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

public class LoginFragment extends Fragment{
    private FragmentLoginBinding binding;
    private UserService userService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        userService = new UserService(new NetworkingService());
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivity mainActivity = ((MainActivity) getActivity());
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        TextView usernameTextView = binding.usernameInput;
        TextView passwordTextView = binding.passwordInput;

        binding.loginButton.setOnClickListener(v -> {
            User user;
            if(usernameTextView.getText() == null || passwordTextView.getText() == null){
                mainActivity.makeToast(R.string.login_not_empty_toast, Toast.LENGTH_LONG);
            }
            else{
                user = userService.logIn(
                        usernameTextView.getText().toString(),
                        passwordTextView.getText().toString());

                if(user == null){
                    mainActivity.makeToast(R.string.login_failed_toast, Toast.LENGTH_LONG);
                }
                else{
                    mainActivity.updateCurrentUser(user);
                    navController.navigate(R.id.navigation_user);
                }
            }

        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}