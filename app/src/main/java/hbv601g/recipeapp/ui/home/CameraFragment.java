package hbv601g.recipeapp.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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

import hbv601g.recipeapp.MainActivity;
import hbv601g.recipeapp.R;
import hbv601g.recipeapp.databinding.FragmentCameraBinding;
import hbv601g.recipeapp.localstorage.PhotoBaseLab;

/**
 * This fragment provides camera functionality, showing a preview of the camera view, and letting
 * users take pictures. The following sources were used during the making of this fragment: <a
 * href="https://www.geeksforgeeks.org/android-how-to-request-permissions-in-android-application
 * /">Request permissions - Geeks for Geeks</a>
 * <a href="https://developer.android.com/training/permissions/requesting#java">Request permissions
 * - android development</a>
 * <a href="https://stackoverflow.com/questions/73340946/camerax-using-java-android-studio">Camerax
 * - Stack overflow</a>
 * <a href="https://developer.android.com/codelabs/camerax-getting-started#3">CameraX kotlin code
 * lab</a>
 */
public class CameraFragment extends Fragment
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    /**
     * The tag used for logging in this fragment
     */
    private final String TAG = "Camera";
    /**
     * Binds the UI components in the layout to data sources in the fragment
     */
    private FragmentCameraBinding mBinding;
    /**
     * Handles the request and result for runtime permissions like the camera
     */
    private ActivityResultLauncher<String> requestPermissionLauncher;

    /**
     * Does the initial creation of the fragment. Along with default functionality, it initializes
     * the requestPermissionLauncher used for requesting runtime permissions
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved
     *         state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                Log.d(TAG, "Camera permission granted");
                                startCamera();
                            } else {
                                Log.d(TAG, "Camera permission denied");
                            }
                        });
    }

    /**
     * Creates the view and makes it active on the screen. Inflates the viewbinding to create and
     * instance of it, checks for and requests permissions and sets listeners
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in
     *         the fragment,
     * @param container If non-null, this is the parent view that the fragment's UI should
     *         be attached to.  The fragment should not add the view itself, but this can be used to
     *         generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a
     *         previous saved state as given here.
     * @return the outermost View in the layout file associated with the viewbinding of the
     *         fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = mBinding.getRoot();

        if (checkCameraPermission()) startCamera();
        else requestPermissionLauncher.launch(Manifest.permission.CAMERA);

        mBinding.imageCaptureButton.setOnClickListener(v -> takePhoto());

        return root;
    }


    /**
     * Checks whether the app has permission to use the camera
     *
     * @return true if the app has permission, otherwise false
     */
    private boolean checkCameraPermission() {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        boolean granted = ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, Manifest.permission.CAMERA + (granted ? " " : " not") + " granted");
        return granted;
    }


    /**
     * Starts the camera to display the preview, using the back camera
     */
    private void startCamera() {
        PreviewView previewView = mBinding.viewFinder;
        LifecycleCameraController cameraController =
                new LifecycleCameraController(requireContext());
        cameraController.bindToLifecycle(this);
        cameraController.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
        previewView.setController(cameraController);
    }

    /**
     * Takes a photo using the camera by capturing a snapshot of the preview.
     */
    private void takePhoto() {
        CameraController cameraController = mBinding.viewFinder.getController();
        if (cameraController == null) {
            Toast.makeText(getContext(), R.string.camera_no_access_toast, Toast.LENGTH_LONG).show();
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        try {
            cameraController.takePicture(ContextCompat.getMainExecutor(mainActivity),
                    new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {
                            super.onCaptureSuccess(image);
                            Toast.makeText(getContext(), R.string.camera_photo_success_toast,
                                    Toast.LENGTH_SHORT).show();
                            PhotoBaseLab.get(getActivity()).addPhoto(rotateImage(image));
                            image.close();
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Toast.makeText(requireContext(), R.string.camera_photo_failure_toast,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IllegalStateException ise) {
            Toast.makeText(requireContext(), R.string.camera_not_initialized_toast,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Rotates the given image to align with the screen rotation
     *
     * @param image an ImageProxy containing the image to be rotated
     * @return a bitmap of the rotated image
     */
    private Bitmap rotateImage(ImageProxy image) {
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        int imageRotation = image.getImageInfo().getRotationDegrees();
        int screenRotation = mainActivity.getWindowManager().getDefaultDisplay().getRotation();
        Bitmap bitmap = image.toBitmap();
        Matrix m = new Matrix();
        m.postRotate(imageRotation - screenRotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    /**
     * Called when the fragment is no longer in use. Cleans up the databinding
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }
}