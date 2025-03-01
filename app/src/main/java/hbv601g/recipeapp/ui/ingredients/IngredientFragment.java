package hbv601g.recipeapp.ui.ingredients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentIngredientBinding;
import hbv601g.recipeapp.entities.Ingredient;

/**
 * Fragment með nánari upplýsingum um tiltekið ingredient, valið úr lista
 */
public class IngredientFragment extends Fragment{

    private FragmentIngredientBinding binding;
    private Ingredient mIngredient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if(getArguments()!=null){
            mIngredient = getArguments().getParcelable(getString(R.string.selected_ingredient));
        }

        binding = FragmentIngredientBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if(mIngredient != null) setIngredient();

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