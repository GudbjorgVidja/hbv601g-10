package hbv601g.recipeapp.ui.pantry;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.PantryAdapter;
import hbv601g.recipeapp.databinding.FragmentPantryBinding;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

/**
 * A fragment for displaying a user's pantry
 */
public class PantryFragment extends Fragment {

    private FragmentPantryBinding mBinding;
    private UserService mUserService;
    private List<IngredientMeasurement> mPantryIngredients;
    private long mUid;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPantryBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mUserService = new UserService(new NetworkingService());

        // Check whether someone is logged in before getting the pantry
        if(mainActivity.getUserName() != null){
            mUid = mainActivity.getUserId();

            mUserService.getUserPantry(mUid, new CustomCallback<>() {
                @Override
                public void onSuccess(List<IngredientMeasurement> ingredientMeasurements) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        mPantryIngredients = ingredientMeasurements;
                        setPantryView(mainActivity, navController);
                    });
                }

                @Override
                public void onFailure(List<IngredientMeasurement> ingredientMeasurements) {
                    if(getActivity() == null) return;
                    requireActivity().runOnUiThread(() -> {
                        mPantryIngredients = ingredientMeasurements;
                        mainActivity.makeToast(R.string.null_pantry_list, Toast.LENGTH_LONG);
                    });
                }
            });


        } else {
            mainActivity.makeToast(R.string.pantry_no_user, Toast.LENGTH_LONG);
        }


        return root;

    }

    /**
     * Sets the ui components which use a list of pantry contents
     * @param mainActivity - the mainActivity instance
     * @param navController - the NavController
     */
    private void setPantryView(MainActivity mainActivity, NavController navController){
        ListView pantryListView = mBinding.pantryListView;

        // An adapter to connect the list to the list view
        PantryAdapter pantryAdapter = new PantryAdapter(mainActivity.getApplicationContext(),
                Objects.requireNonNullElseGet(mPantryIngredients, ArrayList::new));
        mPantryListView.setAdapter(pantryAdapter);

        pantryListView.setOnItemClickListener((parent, view, position, id) -> {
            IngredientMeasurement pantryItem = (IngredientMeasurement) parent.getItemAtPosition(position);
            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_pantry_item), pantryItem);
            navController.navigate(R.id.nav_pantry_ingredient, bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

}