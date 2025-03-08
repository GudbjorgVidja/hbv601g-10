package hbv601g.recipeapp.ui.pantry;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.adapters.PantryAdapter;
import hbv601g.recipeapp.databinding.FragmentPantryBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.entities.IngredientMeasurement;
import hbv601g.recipeapp.entities.Unit;
import hbv601g.recipeapp.entities.User;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.UserService;

/**
 * Fragment fyrir pantry hjá user
 */

public class PantryFragment extends Fragment {

    private FragmentPantryBinding binding;
    private UserService mUserService;
    private List<IngredientMeasurement> mPantryIngredients;
    private ListView mPantryListView;
    private long mUid;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPantryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivity mainActivity = ((MainActivity) getActivity());
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);

        mUserService = new UserService(new NetworkingService());
        mUid = mainActivity.getUserId();


        try{
            mPantryIngredients = mUserService.getUserPantry(mUid);                  // Kallar á apa í gegnum service
        } catch (NullPointerException e) {
            mPantryIngredients = new ArrayList<>();                                 // Tómur listi ef ekkert svar
            mainActivity.makeToast(R.string.null_pantry_list, Toast.LENGTH_LONG);
        }



        mPantryListView = binding.pantryListView;


        // Adapter til að tengja listann við ListView
        PantryAdapter pantryAdapter = new PantryAdapter(mainActivity.getApplicationContext(), Objects.requireNonNullElseGet(mPantryIngredients, ArrayList::new));
        //PantryAdapter pantryAdapter = new PantryAdapter(mainActivity.getApplicationContext(), mPantryIngredients);
        mPantryListView.setAdapter(pantryAdapter);


        mPantryListView.setOnItemClickListener((parent, view, position, id) -> {
            IngredientMeasurement pantryItem = (IngredientMeasurement) parent.getItemAtPosition(position);

            Bundle bundle = new Bundle();
            bundle.putParcelable(getString(R.string.selected_pantry_item), pantryItem);
        });

        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}