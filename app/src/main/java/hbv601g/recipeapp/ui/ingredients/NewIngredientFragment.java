package hbv601g.recipeapp.ui.ingredients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentNewIngredientBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.IngredientService;

public class NewIngredientFragment extends Fragment{

    private FragmentNewIngredientBinding binding;
    private Spinner unitSpinner;
    private TextView qtyField;
    private TextView titleField;
    private TextView storeField;
    private TextView priceField;

    private Button confirmButton;


    private Ingredient mIngredient;

    private IngredientService ingredientService;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNewIngredientBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        MainActivity mainActivity = ((MainActivity) getActivity());
        assert mainActivity != null;

        ingredientService = new IngredientService(new NetworkingService(), mainActivity.getUserId());
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);


        unitSpinner = binding.unitSpinner;
        qtyField = binding.newIngredientQuantityInput;
        titleField = binding.newIngredientTitleInput;
        storeField = binding.newIngredientStoreInput;
        priceField = binding.newIngredientPriceInput;
        confirmButton = binding.confirmNewIngredientButton;

        unitSpinner.setAdapter(new ArrayAdapter<Unit>(
                mainActivity.getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Unit.values()));

        confirmButton.setOnClickListener(v -> {
            CharSequence qty = qtyField.getText();
            CharSequence title = titleField.getText();
            CharSequence price = priceField.getText();
            Unit unit = (Unit) unitSpinner.getSelectedItem();
            if(titleField.getText().toString().isEmpty()){
                titleField.setError("oops, title cant be empty");
            }
            else if(qty != null && title != null && price != null){
                mIngredient = ingredientService.createIngredient(
                        title.toString(),
                        Double.parseDouble(qty.toString()),
                        unit,
                        Double.parseDouble(price.toString()), storeField.getText().toString(), null, false);

                navController.navigate(R.id.navigation_ingredients);
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}