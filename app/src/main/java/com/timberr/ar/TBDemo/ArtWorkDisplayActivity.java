package com.timberr.ar.TBDemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.location.Location;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.IBinder;
import android.util.ArraySet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.gms.maps.model.LatLng;
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
import com.google.ar.sceneform.ux.HandMotionView;
import com.timberr.ar.TBDemo.Utils.DataHelper;
import com.timberr.ar.TBDemo.Utils.FrameSelector;
import com.timberr.ar.TBDemo.Utils.BearingProvider;
import com.timberr.ar.TBDemo.Utils.ArtworkDisplayARFragment;
import com.timberr.ar.TBDemo.Utils.FileUtils;
import com.timberr.ar.TBDemo.Utils.LocationService;
import com.timberr.ar.TBDemo.Utils.PermissionHelper;
import com.timberr.ar.TBDemo.Utils.PhotoHelper;
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
  private DataHelper dataHelper;
  private Location target;
  private float currentDegree = 0f;
  private BearingProvider mBearingProvider;
  private boolean isRenderablePlaced;
  private boolean isPhotoVdoMode;
    private boolean isTargetReached;
  private VideoRecorder videoRecorder;
  private int height;
  private int width;
  // The BroadcastReceiver used to listen from broadcasts from the service
  private MyReceiver myReceiver;
  private ImageView nav_compass;
  private ImageView frame;
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
  // A reference to the service used to get location updates.
  private LocationService mService = null;

  // Tracks the bound state of the service.
  private boolean mBound = false;
  // Monitors the state of the connection to the service.
  private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };
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
      dataHelper=new DataHelper(this);
      arFragment = (ArtworkDisplayARFragment) getSupportFragmentManager().findFragmentById(R.id.artwork_fragment);
      nav_compass=findViewById(R.id.nav_compass_art);
      frame=findViewById(R.id.frame);
      dataHelper.setFrame(FrameSelector.chooseFrame());
      frame.setBackgroundResource(dataHelper.getFrame());
      snap=findViewById(R.id.snap);
      back=findViewById(R.id.back);
      photo_btn=findViewById(R.id.photo_btn);
      int arAssetDrawable=R.raw.wassenberg;
      target=new Location("artwork");
      switch (dataHelper.getArtworkReached()){
          case 1:
              target.setLatitude(51.099086);
              target.setLongitude(6.159257);
              arAssetDrawable=R.raw.wassenberg;
              break;
          case 2:
              target.setLatitude(51.100892);
              target.setLongitude(6.15771);
              arAssetDrawable=R.raw.burgwassenberg;
              break;
          case 3:
              target.setLatitude(51.131239);
              target.setLongitude(6.089418);
              arAssetDrawable=R.raw.effeld2;
              break;
          case 4:
              target.setLatitude(51.072432);
              target.setLongitude(5.992083);
              arAssetDrawable=R.raw.motte;
              break;
          case 5:
              target.setLatitude(51.031573);
              target.setLongitude(5.985918);
              arAssetDrawable=R.raw.brebern2;
              break;
          case 6:
              target.setLatitude(51.013813);
              target.setLongitude(6.037959);
              arAssetDrawable=R.raw.selfkant;
              break;
          case 7:
              target.setLatitude(51.06025);
              target.setLongitude(6.093267);
              arAssetDrawable=R.raw.heinsberg;
              break;
          case 8:
              target.setLatitude(51.058228);
              target.setLongitude(6.173339);
              arAssetDrawable=R.raw.adolfosee;
              break;
          case 9:
              target.setLatitude(51.050628);
              target.setLongitude(6.204579);
              arAssetDrawable=R.raw.millcoh;
              break;
          case 10:
              target.setLatitude(51.056557);
              target.setLongitude(6.208612);
              arAssetDrawable=R.raw.millich2;
              break;
          case 11:
              target.setLatitude(51.068524);
              target.setLongitude(6.277999);
              arAssetDrawable=R.raw.hh;
              break;
          case 12:
              target.setLatitude(51.068119);
              target.setLongitude(6.278686);
              arAssetDrawable=R.raw.hh2;
              break;
          case 13:
              target.setLatitude(51.121186);
              target.setLongitude(6.264326);
              arAssetDrawable=R.raw.tuichenundgebackenesobst;
              break;
          case 14:
              target.setLatitude(51.104477);
              target.setLongitude(6.177899);
              arAssetDrawable=R.raw.trauerweider;
              break;

      }
      isTargetReached=false;
      myReceiver = new MyReceiver();
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
                      arAssetDrawable)
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
        PhotoHelper.takePhoto(this,view,dataHelper.getFrame());
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
    protected void onStart() {
        super.onStart();
        arFragment.getPlaneDiscoveryController().hide();
        bindService(new Intent(this, LocationService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(!PermissionHelper.hasLocationPermission(this)){
            PermissionHelper.requestLocationPermission(this);
        }else if (!isPhotoVdoMode && !isTargetReached){
            mBearingProvider.resume();
            mBearingProvider.realign(target);
        }
        arFragment.onResume();
        try {
            arFragment.getArSceneView().resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
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

    /**
     * Receiver for broadcasts sent by {@link LocationService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
            if (location != null) {
                Log.d(TAG, "onReceive: "+isTargetReached);
                mBearingProvider.onLocationChanged(location);
                if (location.distanceTo(target)<=20){
                    isTargetReached=true;
                    mBearingProvider.stop();
                    nav_compass.clearAnimation();
                    nav_compass.setVisibility(View.GONE);
                    arFragment.getPlaneDiscoveryController().show();
                }
            }
        }
    }
    //change to photo-mode view
    private void setCameraPreview_Frame()
    {
        if(width<height)
            height = width;
        else
            width = height;
        ConstraintLayout parentLayout = (ConstraintLayout)findViewById(R.id.parent_layout);
        ConstraintSet set = new ConstraintSet();

        set.clone(parentLayout);
        // connect start and end point of views, in this case top of child to top of parent.
        set.constrainWidth(R.id.rel_Camera_Preview, width);
        set.constrainHeight(R.id.rel_Camera_Preview, height);
        set.applyTo(parentLayout);
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
        if (isTargetReached) {
            Frame frame = arFragment.getArSceneView().getArFrame();
            assert frame != null;
            Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
            for (Plane plane : planes) {
                //check to see if plane is being tracked by ARCore
                if (plane.getTrackingState() == TrackingState.TRACKING && !isRenderablePlaced) {
                    List<HitResult> hitResults = frame.hitTest(screenCentre().x, screenCentre().y);
                    for (HitResult hitResult : hitResults) {
                        //create Anchor
                        Anchor anchor = plane.createAnchor(hitResult.getHitPose());
                        placeRenderable(anchor);
                    }

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
      tbArt.setWorldPosition(new Vector3(anchor.getPose().tx(),anchor.getPose().compose(Pose.makeTranslation(0f,0.05f,0f)).ty(),anchor.getPose().tz()));
      tbArt.setRenderable(renderable);
      tbArt.setParent(anchorNode);
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
