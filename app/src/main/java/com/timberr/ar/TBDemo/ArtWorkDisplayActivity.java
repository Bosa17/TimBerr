
package com.timberr.ar.TBDemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.media.CamcorderProfile;
import android.opengl.GLES11Ext;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.GLSurfaceTextureProducerView;
import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.timberr.ar.TBDemo.Utils.BearingProvider;
import com.timberr.ar.TBDemo.Utils.ArtworkDisplayARFragment;
import com.timberr.ar.TBDemo.Utils.FileUtils;
import com.timberr.ar.TBDemo.Utils.PermissionHelper;
import com.timberr.ar.TBDemo.Utils.PhotoHelper;
import com.timberr.ar.TBDemo.Utils.PreviewSurfaceTextureView;
import com.timberr.ar.TBDemo.Utils.ScreenUtil;
import com.timberr.ar.TBDemo.Utils.VideoRecorder;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ArtWorkDisplayActivity extends AppCompatActivity implements BearingProvider.ChangeEventListener, Scene.OnUpdateListener, FileUtils.photoSavedListener, VideoRecorder.VideoSavedListener {
  private static final String TAG = ArtWorkDisplayActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;
  private Location target;
  private float currentDegree = 0f;
  private BearingProvider mBearingProvider;
  private boolean isRenderablePlaced;
  private boolean isPhotoVdoMode;
  private VideoRecorder videoRecorder;
  private int height;
  private int width;

  private ImageView nav_compass;
  private View frame;
  private Button snap;
  private Button back;
  private Button photo_btn;
  private ArtworkDisplayARFragment arFragment;
  private Renderable renderable;



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

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (!checkIsSupportedDeviceOrFinish(this)) {
        return;
      }
      height = ScreenUtil.getScreenHeight(this);
      width = ScreenUtil.getScreenWidth(this);
      setContentView(R.layout.activity_ux);
      arFragment = (ArtworkDisplayARFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
      nav_compass=findViewById(R.id.nav_compass_art);
      frame=findViewById(R.id.frame);
      snap=findViewById(R.id.snap);
      back=findViewById(R.id.back);
      photo_btn=findViewById(R.id.photo_btn);
      target=new Location("next");
      target.setLatitude(50.768094);
      target.setLongitude(6.090876);
      mBearingProvider = new BearingProvider(this);
      mBearingProvider.setChangeEventListener(this);
      videoRecorder=new VideoRecorder(this);
      videoRecorder.setSceneView(arFragment.getArSceneView());
      videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_HIGH, Configuration.ORIENTATION_PORTRAIT);
      videoRecorder.setVideoSavedListener(this);
      FileUtils.setPhotoSavedListener(this);
      arFragment.getArSceneView().getScene().addOnUpdateListener(this);
      WeakReference<ArtWorkDisplayActivity> weakActivity = new WeakReference<>(this);
      // When you build a Renderable, Sceneform loads its resources in the background while returning
      // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
      ModelRenderable.builder()
              .setSource(
                      this,
                      R.raw.millicharv2)
              .setIsFilamentGltf(true)
              .build()
              .thenAccept(
                      modelRenderable -> {
                          ArtWorkDisplayActivity activity = weakActivity.get();
                          if (activity != null) {
                              activity.renderable = modelRenderable;
                          }
                      })
              .exceptionally(
                      throwable -> {
                          Toast toast =
                                  Toast.makeText(this, "Unable to load renderable", Toast.LENGTH_LONG);
                          toast.setGravity(Gravity.CENTER, 0, 0);
                          toast.show();
                          return null;
                      });

      isRenderablePlaced=false;
      isPhotoVdoMode=false;
      snap.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              //start transition to AR photo-video mode
              if(!PermissionHelper.hasCameraPermission(ArtWorkDisplayActivity.this)){
                  PermissionHelper.requestCameraPermission(ArtWorkDisplayActivity.this);
              }else {
                  startPhotoMode();
              }
          }
      });
      photo_btn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              //take photo
              try {
                  takePhoto();
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      });
      photo_btn.setOnTouchListener(new View.OnTouchListener() {
          @Override
          public boolean onTouch(View view, MotionEvent motionEvent) {
              switch (motionEvent.getAction()){
                  case MotionEvent.ACTION_UP :
                      if (videoRecorder.isRecording())
                        toggleVideo();
                      return false;
              }
              return false;
          }
      });
      photo_btn.setOnLongClickListener(new View.OnLongClickListener() {
          @Override
          public boolean onLongClick(View view) {
              try{
                  toggleVideo();
              } catch (Exception exception) {
                  exception.printStackTrace();
              }
              return true;
          }
      });
      back.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              onBackPressed();
          }
      });
    }

    //take picture from the AR Scene View
    private void takePhoto() {
        ArSceneView view = arFragment.getArSceneView();
        PhotoHelper.takePhoto(this,view);

    }

    //function to take vdo
    private void toggleVideo(){
      videoRecorder.onToggleRecord();
    }

    @Override
    public void onBackPressed() {
        if (!isPhotoVdoMode)
            super.onBackPressed();
        else
            recreate();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(!PermissionHelper.hasLocationPermission(this)){
            PermissionHelper.requestLocationPermission(this);
        }else if (!isPhotoVdoMode){
            mBearingProvider.start(target);
        }
        arFragment.onResume();
        try {
            arFragment.getArSceneView().resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (videoRecorder.isRecording())
            toggleVideo();
        mBearingProvider.stop();
        arFragment.getArSceneView().pause();
        arFragment.onPause();
    }

    private void startPhotoMode(){
        isPhotoVdoMode=true;
        setCameraPreview_Frame();
        mBearingProvider.stop();
        nav_compass.clearAnimation();
        nav_compass.setVisibility(View.GONE);
        snap.setVisibility(View.GONE);
        photo_btn.setVisibility(View.VISIBLE);
        frame.setVisibility(View.VISIBLE);
    }


    //change to photo-mode view
    private void setCameraPreview_Frame()
    {
        RelativeLayout rel_Camera_Preview = (RelativeLayout)findViewById(R.id.rel_Camera_Preview);
        int width = rel_Camera_Preview.getWidth();
        int height = rel_Camera_Preview.getHeight();

        if(width<height)
            height = width;
        else
            width = height;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        arFragment.getView().setLayoutParams(layoutParams);
    }


    @Override
    public void onBearingChanged(double bearing) {
      // create a rotation animation (reverse turn degree degrees)
        rotateArrow((float) bearing);
    }

    private void rotateArrow(float angle){

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -angle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(500);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        nav_compass.startAnimation(ra);
        currentDegree = -angle;
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        Frame frame=arFragment.getArSceneView().getArFrame();
        assert frame != null;
        Collection<Plane> planes=frame.getUpdatedTrackables(Plane.class);
        for (Plane plane : planes) {
            //check to see if plane is being tracked by ARCore
            if (plane.getTrackingState() == TrackingState.TRACKING && !isRenderablePlaced){
                List<HitResult> hitResults= frame.hitTest(screenCentre().x,screenCentre().y);
                for (HitResult hitResult:hitResults){
                    //create Anchor
                    Anchor anchor=plane.createAnchor(hitResult.getHitPose());
                    placeRenderable(anchor);
                }

            }
        }
    }
    private Vector3 screenCentre(){
      return new Vector3(width/2.0f,height/2.0f,0f);
    }
    private void placeRenderable(Anchor anchor){
      if (renderable == null) {
          return;
      }
      AnchorNode anchorNode = new AnchorNode(anchor);
      anchorNode.setParent(arFragment.getArSceneView().getScene());

      //Non-transformable renderable created
      Node tbArt= new Node();
      tbArt.setParent(anchorNode);
      tbArt.setWorldPosition(new Vector3(anchor.getPose().tx(),anchor.getPose().compose(Pose.makeTranslation(0f,0.05f,0f)).ty(),anchor.getPose().tz()));
      tbArt.setRenderable(renderable);
      FilamentAsset filamentAsset = tbArt.getRenderableInstance().getFilamentAsset();
      if (filamentAsset.getAnimator().getAnimationCount() > 0) {
          animators.add(new AnimationInstance(filamentAsset.getAnimator(), 0, System.nanoTime()));
      }
      arFragment
                .getArSceneView()
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            Long time = System.nanoTime();
                            for (AnimationInstance animator : animators) {
                                animator.animator.applyAnimation(
                                        animator.index,
                                        (float) ((time - animator.startTime) / (double) SECONDS.toNanos(1))
                                                % animator.duration);
                                animator.animator.updateBoneMatrices();
                            }
                        });
        renderablePlaced();
    }
    private void renderablePlaced(){
      isRenderablePlaced=true;
      renderable=null;
    }

    @Override
    public void onLocationReached() {

    }

    @Override
    public void onDistanceChanged(float distance) {

    }

    /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPhotoSaved( String file_path) {
        Intent i = new Intent(this, PicturePreviewActivity.class);
        i.putExtra("path", file_path);
        startActivity(i);
    }

    @Override
    public void onVideoSaved(String file_path) {
        Intent i = new Intent(this, VideoPreviewActivity.class);
        i.putExtra("path", file_path);
        startActivity(i);
    }
}
