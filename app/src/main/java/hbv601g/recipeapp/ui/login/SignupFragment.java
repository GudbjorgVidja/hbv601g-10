package hbv601g.recipeapp.ui.login;

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
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

public class SignupFragment extends Fragment {
    private FragmentSignupBinding binding;

    private UserService mUserService;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mUserService = new UserService(new NetworkingService());
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivity mainActivity = ((MainActivity) getActivity());
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        binding.signupButton.setOnClickListener(v -> {
            String username = Objects.requireNonNull(binding.userNameInput.getText()).toString();
            String password = Objects.requireNonNull(binding.passwordInput.getText()).toString();

            User user = mUserService.signup(username,password);
            if(user == null){
                mainActivity.makeToast(R.string.signup_failed_toast, Toast.LENGTH_LONG);
            }
            else{
                mainActivity.updateUser(username,password);
                navController.navigate(R.id.navigation_user);
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