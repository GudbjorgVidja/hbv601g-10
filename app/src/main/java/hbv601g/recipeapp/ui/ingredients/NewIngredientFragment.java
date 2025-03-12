package hbv601g.recipeapp.ui.ingredients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.IngredientService;

/**
 * Fragment til að búa til nýtt ingredient, notandi slær inn upplýsingar.
 */
public class NewIngredientFragment extends Fragment{

    private FragmentNewIngredientBinding mBinding;
    private EditText mQuantityField;
    private EditText mTitleField;
    private EditText mPriceField;

    private Ingredient mIngredient;
    private IngredientService mIngredientService;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

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
        Spinner unitSpinner = mBinding.unitSpinner;
        EditText storeField = mBinding.newIngredientStoreInput;
        EditText brandField = mBinding.newIngredientBrandInput;
        SwitchMaterial privateSwitch = mBinding.newIngredientPrivateSelection;

        unitSpinner.setAdapter(new ArrayAdapter<Unit>(
                mainActivity.getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Unit.values()));

        confirmButton.setOnClickListener(v -> {
            if(mainActivity.getUserId() == 0)
                navController.popBackStack();

            else if(isValid()){
                try{
                    mIngredient = mIngredientService.createIngredient(
                            mTitleField.getText().toString(),
                            Double.parseDouble(mQuantityField.getText().toString()),
                            (Unit) unitSpinner.getSelectedItem(),
                            Double.parseDouble(mPriceField.getText().toString()),
                            Objects.requireNonNull(storeField.getText()).toString(),
                            Objects.requireNonNull(brandField.getText()).toString(),
                            privateSwitch.isChecked()
                    );
                } catch (NullPointerException e){
                    mainActivity.makeToast(R.string.create_ingredient_failed_toast, Toast.LENGTH_LONG);
                }


                 Bundle bundle = new Bundle();
                 bundle.putParcelable(getString(R.string.selected_ingredient), mIngredient);
                 navController.popBackStack();
                 navController.navigate(R.id.navigation_ingredient, bundle);

            }
        });


        return root;
    }


    /**
     * Staðfestir að titill, magn og verð séu skráð,
     * kröfur fyrir valid ingredient
     * @return hvort inntak sé valid
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

        return isValid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}