package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientMeasurementAdpater;
import hbv601g.recipeapp.databinding.FragmentCreateRecipeBinding;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

public class NewRecipeFragment extends Fragment {
    private RecipeService recipeService;
    private FragmentCreateRecipeBinding binding;
    private List<IngredientMeasurement> list;
    private Recipe recipe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateRecipeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recipe = null;
        list = new ArrayList<>();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        IngredientMeasurementAdpater adapter = new IngredientMeasurementAdpater
                (
                        mainActivity.getApplicationContext(), list
                );
        binding.ingredients.setAdapter(adapter);

        NavController navController = Navigation.findNavController(
                mainActivity, R.id.nav_host_fragment_activity_main
        );

        long uid = mainActivity.getUserId();
        recipeService = new RecipeService(new NetworkingService(), uid);

        binding.addIngredientToRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddIngredientMeasurementFragment nextFragment =
                        new AddIngredientMeasurementFragment();

                IngredientMeasurement ingerMes = nextFragment.getIngredientMeasurement();
                if(ingerMes != null) {
                    list.add(nextFragment.getIngredientMeasurement());
                    adapter.notifyDataSetChanged();
                }
            }
        });

        binding.crateRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recipe = createRecipe();
                onDestroyView();
            }
        });

        binding.cancelRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestroyView();
            }
        });

        return root;
    }

    private Recipe createRecipe(){
        String title =  binding.recipeName.getText().toString();
        String instructions = binding.instructions.getText().toString();
        Boolean isPrivate = binding.isPrivate.isActivated();
        List<IngredientMeasurement> ingredientMeasurementList = new ArrayList<>();

        ListAdapter ingredients= binding.ingredients.getAdapter();
        int size = ingredients.getCount();
        for(int i = 0; i < size; i++){
            ingredientMeasurementList.add((IngredientMeasurement) ingredients.getItem(i));
        }

        return  recipeService.createRecipe(
                title,instructions, ingredientMeasurementList, isPrivate
        );
    }

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
