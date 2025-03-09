package hbv601g.recipeapp.ui.pantry;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Objects;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentAddToPantryBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

/**
 * Fragment þar sem ingredient er bætt við í pantry.
 */
public class AddToPantryFragment extends Fragment {

    private FragmentAddToPantryBinding binding;
    private EditText mQuantityInput;
    private Ingredient mIngredient;
    private UserService mUserService;
    private IngredientMeasurement mPantryIngredient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments() != null) {
            mIngredient = getArguments().getParcelable(getString(R.string.selected_ingredient));
        }
        binding = FragmentAddToPantryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        MainActivity mainActivity = ((MainActivity) getActivity());
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mUserService = new UserService(new NetworkingService());

        if(mIngredient != null){
            setIngredient();
        }

        Button confirmButton = binding.addToPantryConfirmButton;
        Spinner unitSpinner = binding.addToPantryUnitSpinner;
        mQuantityInput = binding.addToPantryInputQuantity;



        unitSpinner.setAdapter(new ArrayAdapter<Unit>(
                mainActivity.getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Unit.values()));


        confirmButton.setOnClickListener(v -> {
            String quantityText = mQuantityInput.getText().toString().trim();
            boolean quantityValid = !quantityText.isEmpty();
            if(mainActivity.getUserId() == 0) {
                navController.popBackStack();
            } else if(quantityValid){
                try{
                    mPantryIngredient = mUserService.addIngredientToPantry(
                            mainActivity.getUserId(),
                            mIngredient.getId(),
                            (Unit) unitSpinner.getSelectedItem(),
                            Double.parseDouble(mQuantityInput.getText().toString()));
                    navController.popBackStack();
                } catch (NullPointerException e){
                    mainActivity.makeToast(R.string.add_to_pantry_failed, Toast.LENGTH_LONG);
                    navController.popBackStack();
                }
            } else {
                mQuantityInput.setError(getString(R.string.field_required_error));
            }

        });

        return root;
    }

    private void setIngredient(){
        binding.addToPantryItemTitle.setText(mIngredient.getTitle());
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        binding = null;
    }

}