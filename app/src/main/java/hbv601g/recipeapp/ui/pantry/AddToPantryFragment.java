package hbv601g.recipeapp.ui.pantry;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentAddToPantryBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

/**
 * A fragment for adding an ingredient to the pantry
 */
public class AddToPantryFragment extends Fragment {

    private FragmentAddToPantryBinding mBinding;
    private EditText mQuantityInput;
    private Ingredient mIngredient;
    private UserService mUserService;
    private AutoCompleteTextView mUnitDropdown;
    private Unit mSelectedUnit;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments() != null) {
            mIngredient = getArguments().getParcelable(getString(R.string.selected_ingredient));
        }

        // Attempting to update mSelectedUnit when relevant
        if (savedInstanceState != null){
            String unitString = savedInstanceState.getString(getString(R.string.selected_unit), "");
            try {
                mSelectedUnit = Unit.valueOf(unitString);
            } catch (IllegalArgumentException e) {
                Log.d("Add to pantry fragment", "Illegal argument for unit");
            }
        }

        mBinding = FragmentAddToPantryBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mUserService = new UserService(new NetworkingService());

        if(mIngredient != null){
            setIngredient();
        }

        Button confirmButton = mBinding.addToPantryConfirmButton;
        mQuantityInput = mBinding.addToPantryInputQuantity;


        confirmButton.setOnClickListener(v -> {
            String quantityText = mQuantityInput.getText().toString().trim();
            boolean isValid = !quantityText.isEmpty();
            if(mSelectedUnit == null){
                isValid = false;
                mBinding.unitDropdown.setError(getString(R.string.field_required_error));
            }
            if(mainActivity.getUserId() == 0) {
                navController.popBackStack();
            } else if(isValid){

                mUserService.addIngredientToPantry(
                        mainActivity.getUserId(),
                        mIngredient.getId(),
                        mSelectedUnit,
                        Double.parseDouble(mQuantityInput.getText().toString()),
                        new CustomCallback<>() {
                            @Override
                            public void onSuccess(IngredientMeasurement ingredientMeasurement) {
                                if(getActivity() == null) return;
                                requireActivity().runOnUiThread(() -> {
                                    mainActivity.makeToast(R.string.add_to_pantry_success, Toast.LENGTH_LONG);
                                    navController.popBackStack();
                                });
                            }

                            @Override
                            public void onFailure(IngredientMeasurement ingredientMeasurement) {
                                if(getActivity() == null) return;
                                requireActivity().runOnUiThread(() -> {
                                    if (ingredientMeasurement == null)
                                        mainActivity.makeToast(R.string.add_to_pantry_failed, Toast.LENGTH_LONG);
                                    else
                                        mainActivity.makeToast(R.string.add_to_pantry_already_existing, Toast.LENGTH_LONG);

                                    navController.popBackStack();
                                });
                            }
                        });

            } else {
                mQuantityInput.setError(getString(R.string.field_required_error));
            }

        });

        return root;
    }


    @Override
    public void onResume() {
        super.onResume();

        mUnitDropdown = mBinding.unitDropdown;

        ArrayAdapter<Unit> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Unit.values());

        mUnitDropdown.setAdapter(adapter);

        mUnitDropdown.setOnItemClickListener((parent, view, position, id) -> {
            mUnitDropdown.setError(null);
            mSelectedUnit = adapter.getItem(position);
        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Ensures the selected unit is kept until the fragment is closed
        if(mSelectedUnit != null)
            outState.putCharSequence(getString(R.string.selected_unit), mSelectedUnit.name());

    }

    /**
     * Sets the title of the ingredient in the UI
     */
    private void setIngredient(){
        mBinding.addToPantryItemTitle.setText(mIngredient.getTitle());
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mBinding = null;
    }

}