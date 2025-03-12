package hbv601g.recipeapp.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentUserNoUserBinding;

public class UserNoUserFragment extends Fragment {

    private FragmentUserNoUserBinding mBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentUserNoUserBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();

        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mBinding.loginButton.setOnClickListener(v -> navController.navigate(R.id.navigation_login));

        mBinding.signupButton.setOnClickListener(v -> navController.navigate(R.id.navigation_signup));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}