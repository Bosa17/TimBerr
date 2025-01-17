package com.timberr.ar.Bilderreise;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.location.Location;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.IBinder;
import android.util.ArraySet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.iammert.library.cameravideobuttonlib.CameraVideoButton;
import com.timberr.ar.Bilderreise.Utils.DataHelper;
import com.timberr.ar.Bilderreise.Utils.ArtworkDisplayARFragment;
import com.timberr.ar.Bilderreise.Utils.FileUtils;
import com.timberr.ar.Bilderreise.Utils.LocationService;
import com.timberr.ar.Bilderreise.Utils.PermissionHelper;
import com.timberr.ar.Bilderreise.Utils.PhotoHelper;
import com.timberr.ar.Bilderreise.Utils.ScreenUtil;
import com.timberr.ar.Bilderreise.Utils.VideoRecorder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ArtWorkDisplayActivity extends AppCompatActivity implements FileUtils.photoSavedListener, VideoRecorder.VideoSavedListener {
  private static final String TAG = ArtWorkDisplayActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;
  private DataHelper dataHelper;
  private Location target;
  private boolean isRenderablePlaced;
  private boolean isPhotoVdoMode;
  private boolean isOutsideRadius;
  private VideoRecorder videoRecorder;
  private int height;
  private int width;
  // The BroadcastReceiver used to listen from broadcasts from the service
  private MyReceiver myReceiver;
  private Button snap;
  private Button back;
  private CameraVideoButton photo_btn;
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
      snap=findViewById(R.id.snap);
      back=findViewById(R.id.back);
      photo_btn=findViewById(R.id.photo_btn);
      target=new Location("artwork");
      String arAssetName = "wassenberg.glb";
      switch (dataHelper.getArtworkReached()){
          case 1:
              target.setLatitude(51.099086);
              target.setLongitude(6.159257);
              arAssetName = "wassenberg.glb";
              break;
          case 2:
              target.setLatitude(51.100892);
              target.setLongitude(6.15771);
              arAssetName="burgwassenberg.glb";
              break;
          case 3:
              target.setLatitude(51.131239);
              target.setLongitude(6.089418);
              arAssetName="effeld.glb";
              break;
          case 4:
              target.setLatitude(51.072432);
              target.setLongitude(5.992083);
              arAssetName="motte.glb";
              break;
          case 5:
              target.setLatitude(51.031573);
              target.setLongitude(5.985918);
              arAssetName="brebern.glb";
              break;
          case 6:
              target.setLatitude(51.013813);
              target.setLongitude(6.037959);
              arAssetName="selfkant.glb";
              break;
          case 7:
              target.setLatitude(51.06025);
              target.setLongitude(6.093267);
              arAssetName="heinsberg.glb";
              break;
          case 8:
              target.setLatitude(51.058228);
              target.setLongitude(6.173339);
              arAssetName="adolfosee.glb";
              break;
          case 9:
              target.setLatitude(51.050628);
              target.setLongitude(6.204579);
              arAssetName="millcoh.glb";
              break;
          case 10:
              target.setLatitude(51.056557);
              target.setLongitude(6.208612);
              arAssetName="millich.glb";
              break;
          case 11:
              target.setLatitude(51.068524);
              target.setLongitude(6.277999);
              arAssetName="hh.glb";
              break;
          case 12:
              target.setLatitude(51.068119);
              target.setLongitude(6.278686);
              arAssetName="hh2.glb";
              break;
          case 13:
              target.setLatitude(51.121186);
              target.setLongitude(6.264326);
              arAssetName="tuichenundgebackenesobst.glb";
              break;
          case 14:
              target.setLatitude(51.104477);
              target.setLongitude(6.177899);
              arAssetName="trauerweider.glb";
              break;
          case 21:
//              target.setLatitude(50.785559);
//              target.setLongitude(6.053371);
              target.setLatitude(51.103102);
              target.setLongitude(6.052899);
              arAssetName="blume.glb";
              break;
          case 22:
              target.setLatitude(51.051113);
              target.setLongitude(5.866353);
//              arAssetName=R.raw.pass;
              break;
          case 23:
              target.setLatitude(51.118383);
              target.setLongitude(6.192719);
              arAssetName="insektenhotel.glb";
              break;
          case 24:
              target.setLatitude(51.106498);
              target.setLongitude(6.177899);
              arAssetName="hase.glb";
              break;
          case 25:
              target.setLatitude(50.776399);
              target.setLongitude(6.083909);
              arAssetName="hase.glb";
              break;
          case 26:
              target.setLatitude(50.963899);
              target.setLongitude(6.121553);
              arAssetName="hase.glb";
              break;
          case 27:
              target.setLatitude(50.919995);
              target.setLongitude(6.120102);
              arAssetName="hase.glb";
              break;

      }
      isOutsideRadius =false;
      myReceiver = new MyReceiver();
      videoRecorder=new VideoRecorder(this);
      videoRecorder.setSceneView(arFragment.getArSceneView());
      videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_HIGH, Configuration.ORIENTATION_PORTRAIT);
      videoRecorder.setVideoSavedListener(this);
      FileUtils.setPhotoSavedListener(this);
      WeakReference<ArtWorkDisplayActivity> weakActivity = new WeakReference<>(this);
      try { // Initialize context
          Context context = createPackageContext("com.timberr.ar.Bilderreise", 0);
          // using AssetManager class we get assets
          AssetManager assetManager = context.getAssets();
          /* videoFileName is file name that needs to be accessed. Here if you saved assets in sub-directory then access like : "subdirectory/asset-name" */
          InputStream inputStream = assetManager.open(arAssetName);
          // String[] list = assetManager.list(""); // returns entire list of assets in directory
          ModelRenderable.builder()
                  .setSource(context, (Callable<InputStream>) inputStream)
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
                                      Toast.makeText(this, "Unable to load Artwork", Toast.LENGTH_LONG);
                              toast.setGravity(Gravity.CENTER, 0, 0);
                              toast.show();
                              return null;
                          });
      } catch (PackageManager.NameNotFoundException | IOException e) {
          e.printStackTrace();
      }
      // When you build a Renderable, Sceneform loads its resources in the background while returning
      // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().


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
      photo_btn.setVideoDuration(10000);
      photo_btn.enableVideoRecording(true);
      photo_btn.enablePhotoTaking(true);
      photo_btn.setActionListener(new CameraVideoButton.ActionListener() {
          @Override
          public void onStartRecord() {
              toggleVideo();
          }

          @Override
          public void onEndRecord() {
                toggleVideo();
          }

          @Override
          public void onDurationTooShortError() {

          }

          @Override
          public void onSingleTap() {
              try {
                  takePhoto();
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      });

      back.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              onBackPressed();
          }
      });
      arFragment.getPlaneDiscoveryController().show();
      arFragment.getArSceneView().getPlaneRenderer().setEnabled(true);
      arFragment.setOnTapArPlaneListener(
              (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                  if (renderable == null | isOutsideRadius | isRenderablePlaced | isPhotoVdoMode) {
                      return;
                  }

                  // Create the Anchor.
                  Anchor anchor = hitResult.createAnchor();
                  AnchorNode anchorNode = new AnchorNode(anchor);
                  anchorNode.setParent(arFragment.getArSceneView().getScene());

                  //Non-transformable renderable created
                  TransformableNode tbArt = new TransformableNode(arFragment.getTransformationSystem());
                  tbArt.setParent(anchorNode);
                  tbArt.setRenderable(renderable);
                  tbArt.getScaleController().setEnabled(false);
                  tbArt.getTranslationController().setEnabled(false);
                  tbArt.getRotationController().setEnabled(false);
                  tbArt.select();

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
                  arFragment.getPlaneDiscoveryController().hide();
                  arFragment.getPlaneDiscoveryController().setInstructionView(null);
                  arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                  isRenderablePlaced=true;
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
            revert();
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
        arFragment.getArSceneView().pause();
        arFragment.onPause();
    }

    private void startPhotoMode(){
        isPhotoVdoMode=true;
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
        snap.setVisibility(View.GONE);
        photo_btn.setVisibility(View.VISIBLE);
    }

    private void revert(){
        isPhotoVdoMode=false;
        snap.setVisibility(View.VISIBLE);
        photo_btn.setVisibility(View.GONE);
    }

    /**
     * Receiver for broadcasts sent by {@link LocationService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationService.EXTRA_LOCATION);
            if (location != null) {
                if (location.distanceTo(target)>=30 && !isRenderablePlaced){
                    isOutsideRadius =true;
                    Toast.makeText(getApplicationContext(),"Moved out of radius!",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
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
