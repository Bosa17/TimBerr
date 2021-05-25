package com.timberr.ar.TBDemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.ux.ArFragment;
import com.timberr.ar.TBDemo.Utils.AugmentedImageNode;
import com.timberr.ar.TBDemo.Utils.SnackbarHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;

public class GuideCameraActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ImageView fitToScanView;
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
                Log.d("lol", "onUpdateFrame: "+filamentAsset);
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
                Log.e("lol", "onUpdateFrame: Exception",e);
            }
            switch (augmentedImage.getTrackingState()) {

                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Detected Image " + augmentedImage.getIndex();
                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        AugmentedImageNode node = new AugmentedImageNode(this);
                        node.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, node);
                        arFragment.getArSceneView().getScene().addChild(node);

                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

}