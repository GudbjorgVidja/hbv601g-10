package hbv601g.recipeapp.ui.pantry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentPantryIngredientBinding;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

/**
 * A fragment for viewing a specific ingredient in a user's pantry
 */
public class PantryIngredientFragment extends Fragment {
    FragmentPantryIngredientBinding mBinding;
    private IngredientMeasurement mPantryIngredient;
    private UserService mUserService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments() != null){
            mPantryIngredient = getArguments().getParcelable(getString(R.string.selected_pantry_item));
        }
        mBinding = FragmentPantryIngredientBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();

        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mUserService = new UserService(new NetworkingService());

        if(mPantryIngredient != null){
            setPantryIngredient();
        }

        // A button to delete the ingredient from pantry
        mBinding.removeFromPantryButton.setOnClickListener(v -> {
            if(mainActivity.getUserId() != 0 && mPantryIngredient != null && mPantryIngredient.getIngredient() != null
                    && mPantryIngredient.getIngredient().getId() != 0){
                AlertDialog.Builder alert = makeAlert(navController, mainActivity);
                alert.show();
            } else {
                mainActivity.makeToast(R.string.remove_from_pantry_failed, Toast.LENGTH_LONG);
            }
        });

        return root;

    }

    /**
     * Creates an alert dialog asking the user to confirm the deletion of an ingredient from the
     * pantry. If the user confirms, the ingredient is removed from the pantry. If the user
     * cancels, nothing happens
     *
     * @param navController the NavController used for navigation between fragments
     * @param mainActivity the current activity
     * @return the alert to display to the user
     */
    private AlertDialog.Builder makeAlert(NavController navController, MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle("Remove from pantry");
        alert.setMessage("Are you sure you want to remove this item from the pantry?");
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean res = mUserService.removeIngredientFromPantry(mainActivity.getUserId(), mPantryIngredient.getIngredient().getId());
                if(res){
                    navController.popBackStack();
                    mainActivity.makeToast(R.string.remove_from_pantry_successful, Toast.LENGTH_LONG);
                } else {
                    mainActivity.makeToast(R.string.remove_from_pantry_failed, Toast.LENGTH_LONG);
                }
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return alert;
    }

    /**
     * Sets the information about the pantry ingredient in the UI
     */
    private void setPantryIngredient() {
        mBinding.pantryIngredientTitle.setText(mPantryIngredient.getIngredient().getTitle());
        String tmp;
        if (mPantryIngredient.getIngredient().getBrand() != null){
            tmp = getString(R.string.pantry_ingredient_quantity_brand, mPantryIngredient.getQuantity()+"", mPantryIngredient.getUnit().toString(), mPantryIngredient.getIngredient().getBrand());
        } else {
            tmp = mPantryIngredient.getQuantity() + mPantryIngredient.getUnit().toString();
        }
        mBinding.pantryIngredientQuantityUnit.setText(tmp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}