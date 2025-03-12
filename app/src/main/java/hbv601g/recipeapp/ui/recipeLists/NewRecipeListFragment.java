package hbv601g.recipeapp.ui.recipeLists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import hbv601g.recipeapp.databinding.FragmentNewRecipeListBinding;

public class NewRecipeListFragment extends Fragment {
    private FragmentNewRecipeListBinding mBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentNewRecipeListBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
