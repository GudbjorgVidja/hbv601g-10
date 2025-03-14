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
import hbv601g.recipeapp.databinding.FragmentUserBinding;

public class UserFragment extends Fragment{

    private FragmentUserBinding mBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Gæti verið betra að hafa user sem argument, og ef það er ekki til staðar þá enginn user
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mBinding = FragmentUserBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        mBinding.logoutButton.setOnClickListener(v -> mainActivity.removeCurrentUser());

        if(mainActivity.getUserName() == null){
            navController.popBackStack();
            navController.navigate(R.id.nav_user_no_user);
        }

        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}