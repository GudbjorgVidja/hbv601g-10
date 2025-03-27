package hbv601g.recipeapp.ui.home;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentHomeBinding;
import hbv601g.recipeapp.localstorage.ImageLab;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding mBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = ((MainActivity) getActivity());
        assert mainActivity != null;

        mBinding.homeCameraButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(mainActivity, R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.nav_camera);
        });

        showPhoto();

        return root;
    }

    /**
     * gets a photo from the files directory and displays it if it exists. if there isn't a photo
     * there, a predetermined photo is shown
     */
    private void showPhoto(){
        Bitmap bitmap = ImageLab.get(getActivity()).getPhoto();
        if (bitmap!=null){
            mBinding.homeImage.setImageBitmap(bitmap);
        }
        else mBinding.homeImage.setImageResource(R.drawable.ic_home_black_24dp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}