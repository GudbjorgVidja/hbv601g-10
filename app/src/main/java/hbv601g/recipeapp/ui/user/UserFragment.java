package hbv601g.recipeapp.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.RecipeAdapter;
import hbv601g.recipeapp.adapters.RecipeListAdapter;
import hbv601g.recipeapp.databinding.FragmentUserBinding;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;

public class UserFragment extends Fragment{

    private FragmentUserBinding mBinding;
    private List<RecipeList> mRecipeLists;
    private RecipeListService mRecipeListService;
    private ListView mRecipeListListView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Gæti verið betra að hafa user sem argument, og ef það er ekki til staðar þá enginn user
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mBinding = FragmentUserBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        mRecipeListService = new RecipeListService(new NetworkingService(), mainActivity.getUserId());

        if(mainActivity.getUserId() != 0){
            try {
                mRecipeLists = mRecipeListService.getUserRecipeLists(mainActivity.getUserId());
            } catch(NullPointerException e) {
                mRecipeLists = new ArrayList<>();
                mainActivity.makeToast(R.string.null_recipe_lists, Toast.LENGTH_LONG);
            }
            mRecipeListListView = mBinding.userRecipeLists;

            RecipeListAdapter recipeListAdapter = new RecipeListAdapter(mainActivity.getApplicationContext(), mRecipeLists);
            mRecipeListListView.setAdapter(recipeListAdapter);

            mRecipeListListView.setOnItemClickListener((parent, view, position, id) -> {
                RecipeList recipeList = (RecipeList) parent.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                bundle.putParcelable(getString(R.string.selected_recipe_list), recipeList);
                navController.navigate(R.id.nav_recipe_list, bundle);
            });
        }





        mBinding.logoutButton.setOnClickListener(v -> mainActivity.removeCurrentUser());

        if(mainActivity.getUserName() == null){
            navController.popBackStack();
            navController.navigate(R.id.nav_user_no_user);
        }

        mBinding.changePasswordButton.setOnClickListener(v -> {
            navController.navigate(R.id.nav_change_password);
        });

        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        mBinding.createRecipeListButton.setOnClickListener(v -> {
            navController.navigate(R.id.navigation_new_recipe_list);
        });
        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}