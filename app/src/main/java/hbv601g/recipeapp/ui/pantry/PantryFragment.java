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
            mPantryIngredients = mUserService.getUserPantry(mUid);
        } catch (NullPointerException e) {
            mPantryIngredients = new ArrayList<>();
        }



        mPantryListView = binding.pantryListView;

        PantryAdapter pantryAdapter = new PantryAdapter(mainActivity.getApplicationContext(), mPantryIngredients);
        mPantryListView.setAdapter(pantryAdapter);

        Log.d("PantryFragment", "Adapter item count: " + pantryAdapter.getCount());

        mPantryListView.setOnItemClickListener((parent, view, position, id) -> {
            IngredientMeasurement pantryItem = (IngredientMeasurement) parent.getItemAtPosition(position);
            Log.d("Selected", pantryItem.toString());

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