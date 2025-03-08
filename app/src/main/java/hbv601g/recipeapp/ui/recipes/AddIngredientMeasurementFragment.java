package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientAdapter;
import hbv601g.recipeapp.databinding.FragmentCreateRecipeAddIngredientsBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.IngredientService;

public class AddIngredientMeasurementFragment extends Fragment {
    private FragmentCreateRecipeAddIngredientsBinding binding;
    private Ingredient ingredient;
    private Unit unit;
    private IngredientMeasurement ingreMeas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateRecipeAddIngredientsBinding.inflate(
                inflater, container, false
        );
        View root = binding.getRoot();
        ingreMeas = null;

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        IngredientService tempServ = new IngredientService
                                            (
                                                new NetworkingService(),
                                                mainActivity.getUserId()
                                            );

        IngredientAdapter inadApter = new IngredientAdapter
                                                (
                                                    mainActivity.getApplicationContext(),
                                                    tempServ.getAllIngredients()
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

        binding.spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ingredient = (Ingredient) parent.getItemAtPosition(position);
            }
        });

        binding.spinner2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
        });

        binding.createIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        getActivity(), "Missing dependency: User story 1",
                        Toast.LENGTH_SHORT
                ).show();            }
        });

        binding.addIngredientToRecipe1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingreMeas = addIngredientMeasurement();
                onDestroyView();
            }
        });

        binding.createIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestroyView();
            }
        });

        return root;
    }

    private IngredientMeasurement addIngredientMeasurement(){
        double value = Double.parseDouble(binding.editTextNumber.getText().toString());
        if(Double.isNaN(value)){
            return null;
        }

        return new IngredientMeasurement(ingredient, unit, value);
    }

    public IngredientMeasurement getIngredientMeasurement(){
        return ingreMeas;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
