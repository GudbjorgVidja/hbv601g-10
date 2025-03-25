package hbv601g.recipeapp.ui.home;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.databinding.FragmentCameraBinding;

/**
 * <a href="https://www.geeksforgeeks.org/android-how-to-request-permissions-in-android-application/">Request permissions - Geeks for Geeks</a>
 * <a href="https://developer.android.com/training/permissions/requesting#java">Request permissions - android development</a>
 * <a href="https://stackoverflow.com/questions/73340946/camerax-using-java-android-studio">Camerax - Stack overflow</a>
 * <a href="https://developer.android.com/codelabs/camerax-getting-started#3">CameraX kotlin code lab</a>
 */
public class CameraFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    private final String TAG = "Camera";
    private FragmentCameraBinding mBinding;
    private ImageCapture mImageCapture;
    private ExecutorService mExecutorService;
    private final String CAMERA_PERMISSION_STRING = "android.permission.CAMERA";
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted");
                        startCamera();
                    } else {
                        Log.d(TAG, "Camera permission denied");
                    }
                });
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        if (checkCameraPermission()) startCamera();
        else requestPermissionLauncher.launch(CAMERA_PERMISSION_STRING);

        mBinding.imageCaptureButton.setOnClickListener(v -> takePhoto());
        return root;
    }


    /**
     * Checks whether the app has permission to use the camera
     * @return true if the app has permission, otherwise false
     */
    private boolean checkCameraPermission() {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        boolean granted = ContextCompat.checkSelfPermission(mainActivity,CAMERA_PERMISSION_STRING)==PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, CAMERA_PERMISSION_STRING + (granted ? " granted" : " not granted") );

        return granted;
    }


    /**
     * Starts the camera to display the preview
     */
    private void startCamera() {
        Log.d(TAG, "starting camera");
        PreviewView previewView = mBinding.viewFinder;
        LifecycleCameraController cameraController = new LifecycleCameraController(getContext());
        cameraController.bindToLifecycle(this);
        cameraController.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
        previewView.setController(cameraController);
    }

    /**
     * Captures a snapshot of the preview
     */
    private void takePhoto() {
        CameraController cameraController = mBinding.viewFinder.getController();
        if (cameraController == null) {
            Toast.makeText(getContext(), "Unable to take photo", Toast.LENGTH_SHORT).show();
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        cameraController.takePicture(ContextCompat.getMainExecutor(mainActivity), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                //super.onCaptureSuccess(image);
                Toast.makeText(getContext(), "Photo captured successfully!", Toast.LENGTH_SHORT).show();
                image.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                //super.onError(exception);
                Log.e("CameraX", "Photo capture failed: " + exception.getMessage(), exception);

            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
    }
}