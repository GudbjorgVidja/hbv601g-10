package hbv601g.recipeapp.ui.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientAdapter;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.databinding.FragmentCreateRecipeAddIngredientsBinding;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.IngredientService;

public class AddIngredientMeasurementFragment extends Fragment {
    private FragmentCreateRecipeAddIngredientsBinding binding;
    private Ingredient ingredient;
    private Unit unit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateRecipeAddIngredientsBinding.inflate(
                inflater, container, false
        );
        View root = binding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        NavController navController = Navigation.findNavController(
                mainActivity, R.id.nav_host_fragment_activity_main
        );

        IngredientService tempServ = new IngredientService
                                            (
                                                new NetworkingService(),
                                                mainActivity.getUserId()
                                            );
        List<Ingredient> ingredientList= tempServ.getAllIngredients();
        if(ingredientList == null){
            ingredientList = new ArrayList<>();
        }

        IngredientAdapter inadApter = new IngredientAdapter
                                                (
                                                    mainActivity.getApplicationContext(),
                                                    ingredientList
                                                );
        binding.spinner.setAdapter(inadApter);

        ArrayAdapter<CharSequence> unitApter = ArrayAdapter.createFromResource
                                                (
                                                    mainActivity.getApplicationContext(),
                                                    R.array.unit_array,
                                                    android.R.layout.simple_spinner_item
                                                );
        unitApter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner2.setAdapter(unitApter);

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ingredient = (Ingredient) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ingredient = null;
            }
        });

        binding.spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch ((String) parent.getItemAtPosition(position)){
                    case "ml":
                        unit = Unit.ML;
                        break;

                    case "g":
                        unit = Unit.G;
                        break;

                    case "kg":
                        unit = Unit.KG;
                        break;

                    case "dl":
                        unit = Unit.DL;
                        break;

                    case "tsp":
                        unit = Unit.TSP;
                        break;

                    case "tbsp":
                        unit = Unit.TBSP;
                        break;

                    case "cup":
                        unit = Unit.CUP;
                        break;

                    default:
                        unit = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                unit = null;
            }
        });

        binding.createIngredient.setOnClickListener(view -> {
            Toast.makeText(
                    getActivity(), "Missing dependency: User story 1",
                    Toast.LENGTH_SHORT
            ).show();
        });

        binding.addIngredientToRecipe1.setOnClickListener(view -> {
            IngredientMeasurement ingreMeas = addIngredientMeasurement();
            if(ingreMeas != null){
                Gson gson = new Gson();
                String ingredientMeasurement = gson.toJson(ingreMeas);

                navController.getPreviousBackStackEntry().getSavedStateHandle()
                        .set("ingredientMeasurement", ingredientMeasurement);
                navController.popBackStack();
            }
            else {
                Toast.makeText(getContext(), "Missing information", Toast.LENGTH_SHORT).show();
            }

        });

        binding.cancelAddIngredientToRecipe.setOnClickListener(view -> {
            getParentFragmentManager().popBackStack();
        });

        return root;
    }

    private IngredientMeasurement addIngredientMeasurement(){
        String temp = binding.editTextNumber.getText().toString();
        if(temp.isEmpty()){
            return null;
        }

        double value = Double.parseDouble(temp);
        if(Double.isNaN(value)){
            return null;
        }

        return new IngredientMeasurement(ingredient, unit, value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
