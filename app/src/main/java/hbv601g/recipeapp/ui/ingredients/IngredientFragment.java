package hbv601g.recipeapp.ui.ingredients;

import static android.view.View.GONE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

    private FragmentIngredientBinding mBinding;
    private Ingredient mIngredient;
    private IngredientService mIngredientService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if(getArguments()!=null){
            mIngredient = getArguments().getParcelable(getString(R.string.selected_ingredient));
        }
        mBinding = FragmentIngredientBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
        mIngredientService = new IngredientService(new NetworkingService(), mainActivity.getUserId());


        if(mIngredient != null) setIngredient();

        if(mIngredient != null && mainActivity.getUserId() != 0) {
            mBinding.addToPantryButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putParcelable(getString(R.string.selected_ingredient), mIngredient);
                navController.navigate(R.id.nav_add_to_pantry, bundle);
            });
        } else {
            mBinding.addToPantryButton.setVisibility(GONE);
        }

        if(mIngredient != null && mIngredient.getCreatedBy() != null && mainActivity.getUserId() != 0 &&
                mIngredient.getCreatedBy().getId() == mainActivity.getUserId() ){
            mBinding.deleteIngredientButton.setOnClickListener(v -> {
                    AlertDialog.Builder alert = makeAlert(navController, mainActivity);
                    alert.show();
            });
            mBinding.renameIngredientButton.setOnClickListener(v -> {
                    makeRenameAlert(navController,mainActivity);
            });
        }
        else {
            mBinding.deleteIngredientButton.setVisibility(GONE);
            mBinding.renameIngredientButton.setVisibility(GONE);
        }

        return root;
    }

    private void makeRenameAlert(NavController navController, MainActivity mainActivity){
        final EditText editText = new EditText(mainActivity.getApplicationContext());
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());

        alert.setTitle("Rename Ingredient");
        alert.setMessage("Please enter the new name of the ingredient:");

        alert.setView(editText);

        alert.setPositiveButton(getString(R.string.confirm_button), (dialog, whichButton) -> {
            String errorMessage = getString(R.string.field_required_error);
            String newTitle = editText.getText().toString();
            if (newTitle.isEmpty()) editText.setError(errorMessage);
            else {
                Ingredient ingredient = mIngredientService.changeIngredientTitle(mIngredient.getId(), newTitle);
                if (ingredient == null){
                    mainActivity.makeToast(R.string.rename_ingredient_failed_toast, Toast.LENGTH_LONG);
                }
                else{
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(getString(R.string.selected_ingredient), ingredient);
                    navController.popBackStack();
                    navController.navigate(R.id.nav_ingredient, bundle);
                }

            }
        });

        alert.setNegativeButton(getString(R.string.cancel_button_text), (dialog, whichButton) -> {
            dialog.cancel();
        });

        alert.show();
    }
    /**
     * Makes an alert dialog for deleting ingredients. After the user confirms their action
     * an attempt is made to delete the ingredient. If the user cancels the action, nothing happens
     * @param navController - the NavController being used for navigation
     * @param mainActivity - the MainActivity of the app
     * @return the alert (AlertDialog.Builder) that should be shown to the user
     */
    private AlertDialog.Builder makeAlert(NavController navController, MainActivity mainActivity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        alert.setTitle("Delete entry");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean result = mIngredientService.deleteIngredient(mIngredient.getId());
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
        return alert;
    }

    /**
     * Setur upplýsingar fyrir ingredientið í viðmóti
     */
    private void setIngredient(){
        mBinding.ingredientTitle.setText(mIngredient.getTitle());

        String tmp = mIngredient.getCreatedBy()==null? "Unknown" : mIngredient.getCreatedBy().getUsername();
        mBinding.ingredientCreator.setText(tmp);

        if(mIngredient.getBrand() != null) tmp = getString(R.string.ingredient_quantity_brand, mIngredient.getQuantity()+"", mIngredient.getUnit().toString(), mIngredient.getBrand());
        else tmp = mIngredient.getQuantity() + mIngredient.getUnit().toString();
        mBinding.ingredientQuantityUnit.setText(tmp);

        if(mIngredient.getStore() == null) tmp = (int) mIngredient.getPrice()+getString(R.string.currency);
        else tmp = getString(R.string.ingredient_price_store, (int) mIngredient.getPrice(), mIngredient.getStore());
        mBinding.ingredientPriceStore.setText(tmp);

        tmp = mIngredient.isPrivate() ? "private" : "public";
        mBinding.ingredientPrivate.setText(tmp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}