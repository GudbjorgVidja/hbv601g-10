package hbv601g.recipeapp.ui.pantry;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentPantryIngredientBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

/**
 * Fragment til að skoða tiltekið ingredient í pantry.
 */
public class PantryIngredientFragment extends Fragment {

    FragmentPantryIngredientBinding binding;
    private IngredientMeasurement mPantryIngredient;
    private UserService mUserService;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments() != null){
            mPantryIngredient = getArguments().getParcelable(getString(R.string.selected_pantry_item));
        }
        binding = FragmentPantryIngredientBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        MainActivity mainActivity = ((MainActivity) getActivity());

        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mUserService = new UserService(new NetworkingService());


    }


    private void setPantryIngredient(){
        biding.
    }
}