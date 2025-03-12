package hbv601g.recipeapp.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientMeasurementAdapter;
import hbv601g.recipeapp.databinding.FragmentCreateRecipeBinding;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Recipe;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.RecipeService;

public class NewRecipeFragment extends Fragment {
    private RecipeService recipeService;
    private FragmentCreateRecipeBinding binding;

    private List<IngredientMeasurement> list = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateRecipeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        IngredientMeasurementAdapter adapter = new IngredientMeasurementAdapter
                        (
                            mainActivity.getApplicationContext(), list
                        );
        binding.ingredients.setAdapter(adapter);

        NavController navController = Navigation.findNavController(
                mainActivity, R.id.nav_host_fragment_activity_main
        );

        long uid = mainActivity.getUserId();
        recipeService = new RecipeService(new NetworkingService(), uid);

        binding.addIngredient.setOnClickListener(view -> {
            navController.navigate(R.id.new_recipe_to_add_ingredient_measurement);
        });

        binding.createRecipe.setOnClickListener(view -> {
            Recipe recipe = createRecipe();
            if(recipe != null){
                Gson gson = new Gson();
                String newRecipe = gson.toJson(recipe);
                navController.getPreviousBackStackEntry().getSavedStateHandle()
                        .set("newRecipe", newRecipe);
                navController.popBackStack();
            }
            else {
                Toast.makeText(
                        getActivity(), R.string.user_not_logged_in, Toast.LENGTH_LONG
                ).show();
            }
        });

        binding.cancelRecipe.setOnClickListener(view -> {
            navController.popBackStack();
        });

        LifecycleOwner owner = getViewLifecycleOwner();
        navController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData("ingredientMeasurement")
                .observe(owner, new Observer<Object>() {
                    @Override
                    public void onChanged(Object o) {
                        String temp = (String) o;
                        if(!temp.isEmpty()){
                            Gson gson = new Gson();

                            Type collectionType = new TypeToken<IngredientMeasurement>(){}.getType();
                            JsonObject jsonObj = JsonParser.parseString(temp).getAsJsonObject();

                            list.add(gson.fromJson(jsonObj, collectionType));
                            adapter.notifyDataSetChanged();
                        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
