package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class AddIngredientMeasurementFragment extends Fragment {
    private FragmentAddIngredientMeasurementBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
                mainActivity.runOnUiThread(() -> {
                    makeIngredientView(mainActivity, ingredients);
                });
            }

            @Override
            public void onFailure(List<Ingredient> ingredients) {
                mainActivity.runOnUiThread(() -> {
                    makeIngredientView(mainActivity, ingredients);
                });
            }
        });

        mBinding.spinnerUnit.setAdapter(new ArrayAdapter<Unit>(
                mainActivity.getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Unit.values()));


        mBinding.addIngredientToRecipe.setOnClickListener(view -> {
            IngredientMeasurement ingrMeas = addIngredientMeasurement();
            if(ingrMeas != null){
                Bundle res = new Bundle();
                res.putParcelable(getString(R.string.selected_ingredient_measurement), ingrMeas);
                getParentFragmentManager().setFragmentResult(getString(R.string.request_msmt), res);
                navController.popBackStack();
            }
            else {
                Toast.makeText(getContext(), "Missing information", Toast.LENGTH_SHORT).show();
            }

        });

        mBinding.cancelAddIngredientToRecipe.setOnClickListener(view -> {
            getParentFragmentManager().popBackStack();
        });

        return root;
    }

    /**
     * makes the ui components which use a list of ingredients
     * @param mainActivity - the mainActivity
     * @param ingredients - the list of ingredients to use
     */
    private void makeIngredientView(MainActivity mainActivity, List<Ingredient> ingredients){
        mBinding.spinnerIngredient.setAdapter(
                new IngredientAdapter(mainActivity.getApplicationContext(), ingredients));
    }

    private IngredientMeasurement addIngredientMeasurement(){
        double value;
        Unit unit = (Unit) mBinding.spinnerUnit.getSelectedItem();
        Ingredient ingredient = (Ingredient) mBinding.spinnerIngredient.getSelectedItem();

        String temp = mBinding.ingredientQuantity.getText().toString();
        if(temp.isEmpty()){
            return null;
        }

        value = Double.parseDouble(temp);

        if(Double.isNaN(value) || unit == null || ingredient == null){
            return null;
        }

        return new IngredientMeasurement(ingredient, unit, value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
