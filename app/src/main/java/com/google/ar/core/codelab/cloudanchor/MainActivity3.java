package com.google.ar.core.codelab.cloudanchor;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.rendering.LocationNode;
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

import static com.google.ar.core.codelab.cloudanchor.R.layout;

public class MainActivity3 extends AppCompatActivity {
    private static final int RC_PERMISSIONS = 0x123;
    private boolean installRequested;

    private GestureDetector gestureDetector;
    private Snackbar loadingMessageSnackbar = null;

    private ArSceneView arSceneView;

    //  avatar 's name tag ....
    private ModelRenderable marsRenderable;

    // avatar render
    private ViewRenderable sunRenderable;
    private ViewRenderable solarControlsRenderable;
    Pair<Float, Float> pair1= new Pair<>(18f, 0f);
    Pair<Float, Float> pair2= new Pair<>(0f, -18f);
    private HashMap<String, Pair<Float, Float>> road_map =  new HashMap<String, Pair<Float, Float>>();
    private final RotateSettings solarSettings = new RotateSettings();
    // True once scene is loaded
    private boolean hasFinishedLoading = false;

    // True once the scene has been placed.
    private boolean hasPlacedSolarSystem = false;

    // Astronomical units to meters ratio. Used for positioning the planets of the solar system.
    private static final float AU_TO_METERS = 0.5f;

    private LocationScene locationScene;
    // Renderables for this example
    private ModelRenderable andyRenderable;
    private ViewRenderable exampleLayoutRenderable;
    private volatile double testLatitude = 1.30006;
    private volatile double testLongitude = 103.78840;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity3);
        road_map.put("sheny", pair1);
        road_map.put("bomi", pair2);
        initAR();
    }

    private void initAR() {
        arSceneView = findViewById(R.id.ar_scene_view);

//        CompletableFuture<ViewRenderable> sunStage =
//                       ViewRenderable.builder().setView(this, R.layout.avatar_controls).build();
        CompletableFuture<ModelRenderable> marsStage =
                ModelRenderable.builder().setSource(this, Uri.parse("Mars.sfb")).build();

        CompletableFuture<ViewRenderable> sunStage =
                ViewRenderable.builder().setView(this, R.layout.avatar_render).build();

        CompletableFuture<ViewRenderable> solarControlsStage =
                ViewRenderable.builder().setView(this, R.layout.avatar_controls).build();


        CompletableFuture<ModelRenderable> andy = ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build();

        CompletableFuture<ViewRenderable> exampleLayout =
                ViewRenderable.builder().setView(this, R.layout.example_layout).build();

        CompletableFuture.allOf(
                andy,
                exampleLayout,
                sunStage,
                marsStage,
                solarControlsStage)
                .handle(
                        (notUsed, throwable) -> {
                            if (throwable != null) {
                                DemoUtils.displayError(this, "Unable to load renderable", throwable);
                                return null;
                            }
                            try {
                                marsRenderable = marsStage.get();
                                sunRenderable = sunStage.get();
                                solarControlsRenderable = solarControlsStage.get();

                                exampleLayoutRenderable = exampleLayout.get();
                                andyRenderable = andy.get();

                                // Everything finished loading successfully.
                                hasFinishedLoading = true;
                            } catch (InterruptedException | ExecutionException ex) {
                                DemoUtils.displayError(this, "Unable to load renderable", ex);
                            }
                            return null;
                        });

        // Set up a tap gesture detector.
        gestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                onSingleTap(e);
                                return true;
                            }

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }
                        });

        // Set a touch listener on the Scene to listen for taps.
        arSceneView
                .getScene()
                .setOnTouchListener(
                        (HitTestResult hitTestResult, MotionEvent event) -> {
                            if (!hasPlacedSolarSystem) {
                                return gestureDetector.onTouchEvent(event);
                            }
                            return false;
                        });
        arSceneView
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            if (loadingMessageSnackbar == null) {
                                return;
                            }

                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                if (plane.getTrackingState() == TrackingState.TRACKING) {
                                    hideLoadingMessage();
                                }
                            }
                        });

        // Lastly request CAMERA permission which is required by ARCore.
        DemoUtils.requestCameraPermission(this, RC_PERMISSIONS);

        // Set an update listener on the Scene that will hide the loading message once a Plane is detected.
        arSceneView
                .getScene()
                .setOnUpdateListener(
                        frameTime -> {
                            if (!hasFinishedLoading) {
                                return;
                            }

                            if (locationScene == null) {
                                // If our locationScene object hasn't been setup yet, this is a good time to do it
                                // We know that here, the AR components have been initiated.
                                locationScene = new LocationScene(this, this, arSceneView);

                                // Now lets create our location markers.
                                // First, a layout
                                LocationMarker layoutLocationMarker = new LocationMarker(
                                        testLongitude,
                                        testLatitude,
                                        getExampleView()
                                );

                                // An example "onRender" event, called every frame
                                // Updates the layout with the markers distance
                                layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = exampleLayoutRenderable.getView();
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        distanceTextView.setText(node.getDistance() + "M");
                                    }
                                });
                                // Adding the marker
                                locationScene.mLocationMarkers.add(layoutLocationMarker);

                                // Adding a simple location marker of a 3D model
                                locationScene.mLocationMarkers.add(
                                        new LocationMarker(
                                                103.78823,
                                                1.2996,
                                                getAndy()));
                            }

                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            if (locationScene != null) {
//                                locationScene.setAnchorRefreshInterval(1000 * 60);
                                float translateX = road_map.get("sheny").first;
                                float translateY = road_map.get("sheny").second;
                                locationScene.processFrame(translateX, translateY, frame);
                            }

                            if (loadingMessageSnackbar != null) {
                                for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                    if (plane.getTrackingState() == TrackingState.TRACKING) {
                                        hideLoadingMessage();
                                    }
                                }
                            }
                        });


        // Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        ARLocationPermissionHelper.requestPermission(this);


//        findViewById(R.id.update_coordinate).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AddCoordinatesDialogFragment dialog = new AddCoordinatesDialogFragment();
//                dialog.setOkListener(MainActivity3.this::onResolveOkPressed);
//                dialog.show(getSupportFragmentManager(), "Resolve");
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView == null) {
            return;
        }

        if (arSceneView.getSession() == null) {
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = DemoUtils.hasCameraPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            showLoadingMessage();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationScene != null) {
            locationScene.pause();
        }
        if (arSceneView != null) {
            arSceneView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (arSceneView != null) {
            arSceneView.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!DemoUtils.hasCameraPermission(this)) {
            if (!DemoUtils.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                DemoUtils.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void onSingleTap(MotionEvent tap) {
        if (!hasFinishedLoading) {
            // We can't do anything yet.
            return;
        }

        Frame frame = arSceneView.getArFrame();
        if (frame != null) {
            if (!hasPlacedSolarSystem && tryPlaceSolarSystem(tap, frame)) {
                hasPlacedSolarSystem = true;
            }
        }
    }

    private boolean tryPlaceSolarSystem(MotionEvent tap, Frame frame) {
        if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    // Create the Anchor.
                    Anchor anchor = hit.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arSceneView.getScene());
                    Node solarSystem = createSolarSystem();
                    anchorNode.addChild(solarSystem);
                    return true;
                }
            }
        }

        return false;
    }

    private Node createSolarSystem() {
        Node base = new Node();

        Node sun = new Node();
        sun.setParent(base);
        sun.setLocalPosition(new Vector3(0.0f, 0.5f, 0.0f));

        // avatar
        Node sunVisual = new Node();
        sunVisual.setParent(sun);
        sunVisual.setRenderable(sunRenderable);
        sunVisual.setLocalScale(new Vector3(0.35f, 0.35f, 0.35f));
        // avatar control panel
        Node solarControls = new Node();
        solarControls.setParent(sun);
        solarControls.setRenderable(solarControlsRenderable);
        solarControls.setLocalPosition(new Vector3(0.0f, 0.55f, 0.10f));

        // control panel
        View solarControlsView = solarControlsRenderable.getView();

        View asking = solarControlsView.findViewById(R.id.askings);
        asking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        View msgs = solarControlsView.findViewById(R.id.msgs);
        msgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


//        SeekBar orbitSpeedBar = solarControlsView.findViewById(R.id.orbitSpeedBar);
//        orbitSpeedBar.setProgress((int) (solarSettings.getOrbitSpeedMultiplier() * 10.0f));
//        orbitSpeedBar.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        float ratio = (float) progress / (float) orbitSpeedBar.getMax();
//                        solarSettings.setOrbitSpeedMultiplier(ratio * 10.0f);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                    }
//                });
//
//        SeekBar rotationSpeedBar = solarControlsView.findViewById(R.id.rotationSpeedBar);
//        rotationSpeedBar.setProgress((int) (solarSettings.getRotationSpeedMultiplier() * 10.0f));
//        rotationSpeedBar.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        float ratio = (float) progress / (float) rotationSpeedBar.getMax();
//                        solarSettings.setRotationSpeedMultiplier(ratio * 10.0f);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                    }
//                });

        // Toggle the solar controls on and off by tapping the sun.
//        sunVisual.setOnTapListener(
//                (hitTestResult, motionEvent) -> solarControls.setEnabled(!solarControls.isEnabled()));

        createPlanet("Mars", sun, 0.5f, 24f, marsRenderable, 0.0265f);

        return base;
    }

    private Node createPlanet(
            String name,
            Node parent,
            float auFromParent,
            float orbitDegreesPerSecond,
            ModelRenderable renderable,
            float planetScale) {
        RotatingNode orbit = new RotatingNode(solarSettings, true);
        orbit.setDegreesPerSecond(orbitDegreesPerSecond);
        orbit.setParent(parent);

        // Create the planet and position it relative to the sun.
        Planet planet = new Planet(this, name, planetScale, renderable, solarSettings);
        planet.setParent(orbit);
        planet.setLocalPosition(new Vector3(auFromParent * AU_TO_METERS, 0.0f, 0.0f));

        return planet;
    }

    private void showLoadingMessage() {
//        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
//            return;
//        }
//
//        loadingMessageSnackbar =
//                Snackbar.make(
//                        MainActivity3.this.findViewById(android.R.id.content),
//                        R.string.plane_finding,
//                        Snackbar.LENGTH_INDEFINITE);
//        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
//        loadingMessageSnackbar.show();
    }

    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }

    private Node getExampleView() {
        Node base = new Node();
        base.setRenderable(exampleLayoutRenderable);
        Context c = this;
        // Add  listeners etc here
        View eView = exampleLayoutRenderable.getView();
        eView.setOnTouchListener((v, event) -> {
            Toast.makeText(
                    c, "Location marker touched.", Toast.LENGTH_LONG)
                    .show();
            return false;
        });

        return base;
    }

    private Node getAndy() {
        Node base = new Node();
        base.setRenderable(andyRenderable);
        Context c = this;
        base.setOnTapListener((v, event) -> {
            Toast.makeText(
                    c, "Andy touched.", Toast.LENGTH_LONG)
                    .show();
        });
        return base;
    }

    private void onResolveOkPressed(double latitude, double longitude) {
        this.testLatitude = latitude;
        this.testLongitude = longitude;
    }
}


