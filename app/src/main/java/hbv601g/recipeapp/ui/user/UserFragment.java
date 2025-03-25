package hbv601g.recipeapp.ui.user;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import hbv601g.recipeapp.adapters.RecipeListAdapter;
import hbv601g.recipeapp.databinding.FragmentUserBinding;
import hbv601g.recipeapp.entities.RecipeList;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeListService;
import hbv601g.recipeapp.service.UserService;

public class UserFragment extends Fragment{

    private FragmentUserBinding mBinding;
    private List<RecipeList> mRecipeLists;
    private RecipeListService mRecipeListService;
    private ListView mRecipeListListView;
    private UserService mUserService;
    private NavController mNavController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Gæti verið betra að hafa user sem argument, og ef það er ekki til staðar þá enginn user
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mNavController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mBinding = FragmentUserBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        mUserService = new UserService(new NetworkingService());
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
                mNavController.navigate(R.id.nav_recipe_list, bundle);
            });
        }

        mBinding.logoutButton.setOnClickListener(v -> mainActivity.removeCurrentUser());

        if(mainActivity.getUserName() == null){
            mNavController.popBackStack();
            mNavController.navigate(R.id.nav_user_no_user);
        }

        mBinding.changePasswordButton.setOnClickListener(v -> {
            changePasswordAlert(mainActivity);
        });

        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        mBinding.createRecipeListButton.setOnClickListener(v -> {
            mNavController.navigate(R.id.navigation_new_recipe_list);
        });
        mBinding.usernameDisplay.setText(mainActivity.getUserName());

        return root;
    }

    /**
     * Create a dialog that validate the password of the user svo that only they can change there
     * password.
     *
     * @param activity : MainActivity value, is the activity of the Fragment
     */
    public void changePasswordAlert(MainActivity activity){
        EditText oldPass = new EditText(activity.getApplicationContext());
        oldPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle(R.string.validate_current_password_title);
        alert.setMessage(R.string.validate_current_password_alert_message);
        alert.setView(oldPass);

        alert.setNegativeButton(R.string.cancel_button_text, null);
        alert.setPositiveButton(R.string.confirm_button, (dialog, which) -> {
           String password = oldPass.getText().toString();
           if(password.isEmpty()) {
               oldPass.setError(getString(R.string.validate_current_password_alert_error));
           }
           else {
               if(mUserService.validatePassword(activity.getUserId(), password)){
                   mNavController.navigate(R.id.nav_change_password);
               }
               else {
                   oldPass.setText("");
                   activity.makeToast(R.string.password_invalid_toast,Toast.LENGTH_LONG);
               }
           }
        });

        alert.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}