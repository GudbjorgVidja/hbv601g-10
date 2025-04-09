package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientAdapter;
import hbv601g.recipeapp.databinding.FragmentAddIngredientMeasurementBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.IngredientService;

/**
 * A fragment for adding an ingredient measurement to a recipe
 */
public class AddIngredientMeasurementFragment extends Fragment {
    private FragmentAddIngredientMeasurementBinding mBinding;
    private List<Ingredient> mIngredients;
    private AutoCompleteTextView mUnitDropdown;
    private Unit mSelectedUnit;
    private AutoCompleteTextView mIngredientDropdown;
    private IngredientAdapter mIngredientAdapter;
    private Ingredient mSelectedIngredient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Fetching stored state if relevant
        if (savedInstanceState != null){
            String unitString = savedInstanceState.getString(getString(R.string.selected_unit), "");
            try {
                mSelectedUnit = Unit.valueOf(unitString);
            } catch (IllegalArgumentException e) {
                Log.d("SavedInstanceState", "Illegal argument for unit");
            }
            mIngredients = savedInstanceState.getParcelableArrayList(getString(R.string.saved_ingredients));
            mSelectedIngredient = savedInstanceState.getParcelable(getString(R.string.selected_ingredient));
        }
        if(mIngredients == null) mIngredients = new ArrayList<>();

        mBinding = FragmentAddIngredientMeasurementBinding.inflate(
                inflater, container, false
        );
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        NavController navController = Navigation.findNavController(
                mainActivity, R.id.nav_host_fragment_activity_main
        );

        IngredientService ingredientService = new IngredientService(
            new NetworkingService(),
            mainActivity.getUserId()
        );

        ingredientService.getAllIngredients(new CustomCallback<>() {
            @Override
            public void onSuccess(List<Ingredient> ingredients) {
                mIngredients = ingredients;
                mIngredientAdapter.setIngredientList(ingredients);
                mIngredientAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(List<Ingredient> ingredients) {
                Log.d("Callback", "Failed to get ingredients");
            }
        });



        mBinding.addIngredientToRecipe.setOnClickListener(view -> {
            if(isValid()){
                IngredientMeasurement ingredientMeasurement = new IngredientMeasurement(
                        mSelectedIngredient, mSelectedUnit,
                        Double.parseDouble(mBinding.ingredientQuantity.getText().toString()));
                Bundle res = new Bundle();
                res.putParcelable(getString(R.string.selected_ingredient_measurement), ingredientMeasurement);
                getParentFragmentManager().setFragmentResult(getString(R.string.request_ingredient_measurement), res);
                navController.popBackStack();
            }
            else {
                Toast.makeText(getContext(), getString(R.string.missing_information_toast), Toast.LENGTH_SHORT).show();
            }

        });

        mBinding.cancelAddIngredientToRecipe.setOnClickListener(view -> {
            getParentFragmentManager().popBackStack();
        });

        return root;
    }


    @Override
    public void onResume() {
        super.onResume();

        mUnitDropdown = mBinding.unitDropdown;
        ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, Unit.values());
        mUnitDropdown.setAdapter(unitAdapter);

        mUnitDropdown.setOnItemClickListener((parent, view, position, id) -> {
            mUnitDropdown.setError(null);
            mSelectedUnit = unitAdapter.getItem(position);
        });


        mIngredientDropdown = mBinding.ingredientDropdown;
        mIngredientAdapter = new IngredientAdapter(requireContext(), mIngredients);
        mIngredientDropdown.setAdapter(mIngredientAdapter);

        int selectedPos = mIngredientAdapter.getPosition(mSelectedIngredient);
        if(selectedPos != -1)
            mBinding.selectedIngredientContainer.addView(mIngredientAdapter.getView(selectedPos, null, null));

        mIngredientDropdown.setOnItemClickListener((parent, view, position, id) -> {
            mIngredientDropdown.setError(null);
            mSelectedIngredient = mIngredientAdapter.getItem(position);
            mIngredientDropdown.setText(" ");
            mBinding.selectedIngredientContainer.removeAllViews();
            mBinding.selectedIngredientContainer.addView(mIngredientAdapter.getView(position, null, null));
        });

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Ensures the selected unit is kept until the fragment is closed
        if(mSelectedUnit != null)
            outState.putCharSequence(getString(R.string.selected_unit), mSelectedUnit.name());
        if(mSelectedIngredient != null)
            outState.putParcelable(getString(R.string.selected_ingredient), mSelectedIngredient);
        if(mIngredients != null)
            outState.putParcelableArrayList(getString(R.string.saved_ingredients), (ArrayList<? extends Parcelable>) mIngredients);
    }


    /**
     * Verifies that no required field in the UI is empty, and sets an error for fields which do
     * not contain valid input
     *
     * @return a boolean value indicating the validity of the required fields
     */
    private boolean isValid(){
        boolean isValid = true;
        String errorMessage = getString(R.string.field_required_error);

        if(mBinding.ingredientQuantity.getText().toString().isEmpty()) {
            mBinding.ingredientQuantity.setError(errorMessage);
            isValid = false;
        }
        if(mSelectedUnit == null){
            mUnitDropdown.setError(errorMessage);
            isValid = false;
        }
        if(mSelectedIngredient == null){
            mIngredientDropdown.setError(errorMessage);
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
