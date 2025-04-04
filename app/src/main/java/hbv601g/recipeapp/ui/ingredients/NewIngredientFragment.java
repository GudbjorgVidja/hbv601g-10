package hbv601g.recipeapp.ui.ingredients;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Objects;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentNewIngredientBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.IngredientService;

/**
 * A fragment for having the user create a new ingredient
 */
public class NewIngredientFragment extends Fragment{
    private FragmentNewIngredientBinding mBinding;
    private EditText mQuantityField;
    private EditText mTitleField;
    private EditText mPriceField;
    private IngredientService mIngredientService;
    private AutoCompleteTextView mUnitDropdown;
    private Unit mSelectedUnit;

    private final String TAG = "New Ingredient Fragment";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null){
            String unitString = savedInstanceState.getString(getString(R.string.selected_unit), "");
            try {
                //Log.d(TAG, "unit string:" + unitString);
                mSelectedUnit = Unit.valueOf(unitString);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Illegal argument for unit");
            }
        }

        mBinding = FragmentNewIngredientBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        mIngredientService = new IngredientService(new NetworkingService(), mainActivity.getUserId());
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mQuantityField = mBinding.newIngredientQuantityInput;
        mTitleField = mBinding.newIngredientTitleInput;
        mPriceField = mBinding.newIngredientPriceInput;


        Button confirmButton = mBinding.confirmNewIngredientButton;
        EditText storeField = mBinding.newIngredientStoreInput;
        EditText brandField = mBinding.newIngredientBrandInput;
        SwitchMaterial privateSwitch = mBinding.newIngredientPrivateSelection;

        confirmButton.setOnClickListener(v -> {
            if(isValid()){
                CustomCallback<Ingredient> callback = new CustomCallback<>() {
                    @Override
                    public void onSuccess(Ingredient ingredient) {
                        if(getActivity() == null) return;
                        requireActivity().runOnUiThread(() -> {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(getString(R.string.selected_ingredient), ingredient);
                            navController.popBackStack();
                            navController.navigate(R.id.nav_ingredient, bundle);
                        });
                    }

                    @Override
                    public void onFailure(Ingredient ingredient) {
                        if(getActivity() == null) return;
                        requireActivity().runOnUiThread(() -> {
                            mainActivity.makeToast(R.string.create_ingredient_failed_toast, Toast.LENGTH_LONG);
                            navController.popBackStack();
                        });
                    }
                };

                mIngredientService.createIngredient(
                        mTitleField.getText().toString(),
                        Double.parseDouble(mQuantityField.getText().toString()),
                        mSelectedUnit,
                        Double.parseDouble(mPriceField.getText().toString()),
                        Objects.requireNonNull(storeField.getText()).toString(),
                        Objects.requireNonNull(brandField.getText()).toString(),
                        privateSwitch.isChecked(),
                        callback
                );

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
     * Verifies that the title, quantity and price have been set, which is a requirement for a valid
     * ingredient
     *
     * @return a boolean value indicating the validity of the required fields
     */
    private boolean isValid(){
        boolean isValid = true;
        String errorMessage = getString(R.string.field_required_error);

        if(mTitleField.getText().toString().isEmpty()){
            mTitleField.setError(errorMessage);
            isValid = false;
        }
        if(mQuantityField.getText().toString().isEmpty()) {
            mQuantityField.setError(errorMessage);
            isValid = false;
        }
        if(mPriceField.getText().toString().isEmpty()){
            mPriceField.setError(errorMessage);
            isValid = false;
        }
        if(mSelectedUnit == null){
            mUnitDropdown.setError(errorMessage);
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}