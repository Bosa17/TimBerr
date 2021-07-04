package com.timberr.ar.TBDemo.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.timberr.ar.TBDemo.GuideCameraActivity;
import com.timberr.ar.TBDemo.R;

import java.util.concurrent.CompletableFuture;

public class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AugmentedImageNode";

    // The augmented image represented by this node.
    private AugmentedImage image;
    private Node node;
    // We use completable futures here to simplify
    // the error handling and asynchronous loading.  The loading is started with the
    // first construction of an instance, and then used when the image is set.
    private CompletableFuture<ModelRenderable> renderable;
    public AugmentedImageNode(Context context) {
        // Upon construction, start loading the models for the corners of the frame.
        if (renderable == null) {
            renderable = ModelRenderable.builder()
                    .setSource(
                            context,
                            R.raw.pocketguideseite2)
                    .setIsFilamentGltf(true)
                    .build();

        }
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image) {
        this.image = image;

        // If the models are not loaded, then recurse when all are loaded.
        if (!renderable.isDone()) {
            CompletableFuture.allOf(renderable)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
        }

        // Set the anchor based on the center of the image.
        setAnchor(image.createAnchor(image.getCenterPose()));

        node = new Node();
        node.setParent(this);
        node.setRenderable(renderable.getNow(null));
        Log.d(TAG, "setImage: "+ node.getRenderableInstance());
    }

    public FilamentAsset getAsset() throws NullPointerException{
        if (node.getRenderableInstance()!=null)
            return node.getRenderableInstance().getFilamentAsset();
        else
            return null;
    }

    public AugmentedImage getImage() {
        return image;
    }
}

