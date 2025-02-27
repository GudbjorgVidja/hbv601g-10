package hbv601g.recipeapp.ui.ingredients;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientAdapter;
import hbv601g.recipeapp.databinding.FragmentIngredientsBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.IngredientService;

public class IngredientsFragment extends Fragment {
    private FragmentIngredientsBinding binding;
    private IngredientService mIngredientService;
    private List<Ingredient> mAllIngredients;
    private ListView mIngredientsListView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentIngredientsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivity mainActivity = ((MainActivity) getActivity());
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        long uid = mainActivity.getUserId();

        mIngredientService = new IngredientService(new NetworkingService(), uid);
        mAllIngredients = mIngredientService.getAllIngredients();
        mIngredientsListView = binding.ingredientsListView;

        // Gera adapter til að tengja lista af ingredients við listView hlutinn
        IngredientAdapter ingredientAdapter = new IngredientAdapter(mainActivity.getApplicationContext(), mAllIngredients);
        mIngredientsListView.setAdapter(ingredientAdapter);

        mIngredientsListView.setOnItemClickListener((parent, view, position, id) -> {
            Ingredient ingredient = (Ingredient) parent.getItemAtPosition(position);
            Log.d("Selected", ingredient.toString());

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_ingredient), ingredient);

            //navController.navigate(R.id.navigation_ingredient, bundle);
            //Log.d("Selected", "ingredient by " + ingredient.getCreatedBy().getUsername());

        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}