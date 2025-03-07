package hbv601g.recipeapp.ui.ingredients;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentIngredientBinding;
import hbv601g.recipeapp.entities.Ingredient;
import hbv601g.recipeapp.networking.NetworkingService;
import hbv601g.recipeapp.service.IngredientService;

/**
 * Fragment með nánari upplýsingum um tiltekið ingredient, valið úr lista
 */
public class IngredientFragment extends Fragment{

    private FragmentIngredientBinding binding;
    private Ingredient mIngredient;
    private IngredientService mIngredientService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if(getArguments()!=null){
            mIngredient = getArguments().getParcelable(getString(R.string.selected_ingredient));
        }
        binding = FragmentIngredientBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        MainActivity mainActivity = ((MainActivity) getActivity());
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mIngredientService = new IngredientService(new NetworkingService(), mainActivity.getUserId());


        if(mIngredient != null) setIngredient();

        binding.deleteIngredientButton.setOnClickListener(v -> {
            if(mainActivity.getUserId()!=0 && mIngredient != null && mIngredient.getCreatedBy()!= null && mIngredient.getCreatedBy().getId()==mainActivity.getUserId()){
                AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
                alert.setTitle("Delete entry");
                alert.setMessage("Are you sure you want to delete?");
                alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean result= mIngredientService.deleteIngredient(mIngredient.getId());
                        if (result){
                            navController.popBackStack();
                            mainActivity.makeToast(R.string.delete_ingredient_success, Toast.LENGTH_LONG);
                        }

                        else mainActivity.makeToast(R.string.delete_ingredient_failed, Toast.LENGTH_LONG);


                    }
                });
                alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alert.show();
            }
            else {
                mainActivity.makeToast(R.string.unable_to_delete_ingredient_from_other, Toast.LENGTH_LONG);
            }
        });

        return root;
    }


    /**
     * Setur upplýsingar fyrir ingredientið í viðmóti
     */
    private void setIngredient(){
        binding.ingredientTitle.setText(mIngredient.getTitle());

        String tmp = mIngredient.getCreatedBy()==null? "Unknown" : mIngredient.getCreatedBy().getUsername();
        binding.ingredientCreator.setText(tmp);

        if(mIngredient.getBrand() != null) tmp = getString(R.string.ingredient_quantity_brand, mIngredient.getQuantity()+"", mIngredient.getUnit().toString(), mIngredient.getBrand());
        else tmp = mIngredient.getQuantity() + mIngredient.getUnit().toString();
        binding.ingredientQuantityUnit.setText(tmp);

        if(mIngredient.getStore() == null) tmp = (int) mIngredient.getPrice()+getString(R.string.currency);
        else tmp = getString(R.string.ingredient_price_store, (int) mIngredient.getPrice(), mIngredient.getStore());
        binding.ingredientPriceStore.setText(tmp);

        tmp = mIngredient.isPrivate() ? "private" : "public";
        binding.ingredientPrivate.setText(tmp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}