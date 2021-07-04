package com.timberr.ar.TBDemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;
import com.timberr.ar.TBDemo.Utils.AugmentedImageNode;
import com.timberr.ar.TBDemo.Utils.DataHelper;
import com.timberr.ar.TBDemo.Utils.PermissionHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GuideCameraActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ImageView fitToScanView;
    private Button back;

    private static class AnimationInstance {
        Animator animator;
        Long startTime;
        float duration;
        int index;
        AnimationInstance(Animator animator, int index, Long startTime) {
            this.animator = animator;
            this.startTime = startTime;
            this.duration = animator.getAnimationDuration(index);
            this.index = index;
        }
    }
    private final Set<AnimationInstance> animators = new ArraySet<>();
    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_camera);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.augmented_image_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        if (new DataHelper(this).getLanguage()==1)
            fitToScanView.setBackgroundResource(R.drawable.fit_to_scan_de);
        else
            fitToScanView.setBackgroundResource(R.drawable.fit_to_scan);
        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!PermissionHelper.hasPermission(this)){
            PermissionHelper.requestPermissions(this);
        }
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
        else
            recreate();
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            try {
                FilamentAsset filamentAsset = augmentedImageMap.get(augmentedImage).getAsset();
                Log.d("GuideCameraActivity", "onUpdateFrame: "+filamentAsset);
                if (filamentAsset!=null && filamentAsset.getAnimator().getAnimationCount() > 0) {
                    animators.add(new AnimationInstance(filamentAsset.getAnimator(), 0, System.nanoTime()));
                }
                Long time = System.nanoTime();
                for (AnimationInstance animator : animators) {
                    animator.animator.applyAnimation(
                            animator.index,
                            (float) ((time - animator.startTime) / (double) SECONDS.toNanos(1))
                                    % animator.duration);
                    animator.animator.updateBoneMatrices();
                }
            }catch (Exception e){
                Log.e("GuideCameraActivity", "onUpdateFrame: Exception",e);
            }
            switch (augmentedImage.getTrackingState()) {

                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);
                    AugmentedImageNode node;
                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        if (augmentedImage.getName().equals("trigger.png")) {
                            node = new AugmentedImageNode(this,"pocketguideseite2.glb" );
                        }
                        else
                            node = new AugmentedImageNode(this,"pocketguideseite1.glb" );
                        node.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, node);
                        arFragment.getArSceneView().getScene().addChild(node);
                    }
                    break;

                case STOPPED:
                    arFragment.getArSceneView().getScene().removeChild(augmentedImageMap.get(augmentedImage));
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!PermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, getText(R.string.permission_camera), Toast.LENGTH_LONG)
                    .show();
            if (!PermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                PermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

}