package hbv601g.recipeapp.ui.ingredients;

import static android.view.View.GONE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.IngredientAdapter;
import hbv601g.recipeapp.databinding.FragmentIngredientsBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.networking.CustomCallback;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.IngredientService;

/**
 * Fragment fyrir yfirlit yfir ingredients
 */
public class IngredientsFragment extends Fragment {
    private FragmentIngredientsBinding mBinding;
    private IngredientService mIngredientService;
    private List<Ingredient> mAllIngredients;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentIngredientsBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mIngredientService = new IngredientService(new NetworkingService(), mainActivity.getUserId());


        mIngredientService.getAllIngredients(new CustomCallback<>() {
            @Override
            public void onSuccess(List<Ingredient> ingredients) {
                if(getActivity() == null) return;
                mAllIngredients = ingredients;
                requireActivity().runOnUiThread(() ->
                        makeIngredientsView(mainActivity, navController));
            }

            @Override
            public void onFailure(List<Ingredient> ingredients) {
                if(getActivity() == null) return;
                mAllIngredients = new ArrayList<>();
                requireActivity().runOnUiThread(() ->
                        mainActivity.makeToast(R.string.null_ingredient_list, Toast.LENGTH_LONG));
            }

        });

        Button newIngredientButton = mBinding.newIngredientButton;
        if(mainActivity.getUserId() == 0) newIngredientButton.setVisibility(GONE);

        newIngredientButton.setOnClickListener(v -> navController.navigate(R.id.nav_new_ingredient));

        return root;
    }

    /**
     * updates the view with the ingredients list
     * @param mainActivity - the MainActivity
     * @param navController - the NavController
     */
    private void makeIngredientsView(MainActivity mainActivity, NavController navController){

            ListView ingredientsListView = mBinding.ingredientsListView;

            // Gera adapter til að tengja lista af ingredients við listView hlutinn
            IngredientAdapter ingredientAdapter = new IngredientAdapter(mainActivity.getApplicationContext(), mAllIngredients);
            ingredientsListView.setAdapter(ingredientAdapter);

            ingredientsListView.setOnItemClickListener((parent, view, position, id) -> {
                Ingredient ingredient = (Ingredient) parent.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                bundle.putParcelable(getString(R.string.selected_ingredient), ingredient);
                navController.navigate(R.id.nav_ingredient, bundle);
            });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}