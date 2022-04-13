/* =========================================================================

    Compass Navigator
    Copyright (C) 2019,2022 Draekko, Benoit Touchette

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.

   ========================================================================= */

package com.draekko.compassnavigator;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.SYSTEM_UI_LAYOUT_FLAGS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentManager;

import com.draekko.common.lib.GeomagneticField2020;
import com.draekko.compassnavigator.dialogs.NoSensorErrorDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import com.draekko.compassnavigator.dialogs.CalibrateDialogFragment;
import com.draekko.compassnavigator.dialogs.CalibrateDialogFragment.OnDialogClickListener;
import com.draekko.compassnavigator.views.CompassView;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

@SuppressLint({"NewApi"})
public class CompassActivity extends AppCompatActivity
        implements
        Preference.OnPreferenceChangeListener,
        LocationListener,
        SensorEventListener {

    private static final String TAG = "CompassActivity";
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final long UPDATE_INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 5000;
    private static final int ALL_LOCATION_PERMISSIONS_RESULT = 1011;
    public static final int LOCATION_REQUEST = 1000;
    public static final int GPS_REQUEST = 1001;
    private static final int SETTINGS_REQUEST = 2000;

    private static int M = 10;
    private static int height;
    private static int width;
    private static SharedPreferences preferences;
    private static Activity mStaticActivity;
    private static Settings settings;

    private boolean mNight = false;
    private boolean mAltRose = false;
    private boolean mGpsDecl = true;
    private boolean mManDecl = true;
    private float mManualDecl = 0.0f;
    private int mBearingDirection = 0;

    protected float screenDensity;
    private Float[] angles;
    private int count = 0;
    private double curRotate = 0.0d;
    private double curRotateBG = 0.0d;
    private float curScale = 1.0f;
    private boolean hasItem = false;
    private boolean hasMagnetic = true;
    private boolean hasAccel = true;
    private boolean hasMagneticCheck = false;
    private boolean hasAccelCheck = false;
    private float lastangle = 0.0f;
    private long lasttime = 0;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private CompassView compassView;

    private LinearLayout mDeclinationLLFrame;
    private ImageButton mSettingsButtons;
    private TextView mTextDegrees;
    private TextView mTextBezelDegrees;
    private TextView mTextBezelRevDegrees;
    private TextView mTextOrientation;
    private TextView mTextDeclinatonDegrees;
    private TextView mTextDeclinatonOrientation;

    private float tx;
    private float ty;
    private boolean mUserPermissionDenied = false;
    private int mScreenRotation;
    private Typeface typeface;

    private ArrayList<String> permissions = new ArrayList<>();
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();

    /* GPS */
    private GeomagneticField2020 mGeomagneticField;
    private float[] mGravity;
    private float[] mGeomagnetic;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationManager locationManager;

    boolean isGPSEnabled = true;
    private double mWayLatitude = 0.0;
    private double mWayLongitude = 0.0;
    private double mDeclination = 0.0;

    private static SharedPreferences mSharedPreferences;

    private boolean gpsProviderEnabled = false;
    private boolean networkProviderEnabled = false;
    private Location mLocation;
    private FragmentManager mFragmentManager;

    private static int uiOptions =
            SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    SYSTEM_UI_LAYOUT_FLAGS |
                    SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    SYSTEM_UI_FLAG_FULLSCREEN;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SettingsActivity.startSettingsActivityForResult(mStaticActivity, SETTINGS_REQUEST);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTheme(R.style.AppTheme);
        mStaticActivity = this;

        mFragmentManager = getSupportFragmentManager();
        setupOnRequestPermissionsResult();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mStaticActivity);

        settings = new Settings();
        settings.load(mSharedPreferences);

        mAltRose = settings.getEnableAltRose();
        mNight = settings.getEnableNightMode();
        mGpsDecl = settings.getEnableGpsDeclination();
        mManDecl = settings.getEnableManualDeclination();
        mBearingDirection = settings.getBearingDirection();
        mManualDecl = settings.getManualDeclinationValue();

        if (Build.VERSION.SDK_INT < 30) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(uiOptions);
        } else {
            if (!hideSystemBars()) {
                View decorView = this.getWindow().getDecorView();
                //noinspection deprecation
                decorView.setSystemUiVisibility(uiOptions);
            }
        }

        mDeclinationLLFrame = (LinearLayout) findViewById(R.id.declinationLLFrame);
        mTextDeclinatonDegrees = (TextView) findViewById(R.id.declinationDegrees);
        mTextDeclinatonOrientation = (TextView) findViewById(R.id.declinationDirection);
        mSettingsButtons = (ImageButton) findViewById(R.id.settings);
        mTextDegrees = (TextView) findViewById(R.id.degrees);
        mTextBezelDegrees = (TextView) findViewById(R.id.degreesBezel);
        mTextBezelRevDegrees = (TextView) findViewById(R.id.degreesBezelReverse);
        mTextOrientation = (TextView) findViewById(R.id.direction);
        mSettingsButtons.setOnClickListener(onClickListener);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = permissionsToRequest(permissions);

        mDeclinationLLFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settings.getEnableGpsDeclination()) {

                } else {

                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.
                        toArray(new String[permissionsToRequest.size()]), ALL_LOCATION_PERMISSIONS_RESULT);
            }
        } else {
            Log.e(TAG, "Requires location service permission to run");
            finish();
            System.exit(0);
            return;
        }

        if (settings.getShowCalibration()) {
            CalibrateDialogFragment.newInstance(new OnDialogClickListener() {
                public void onOkClick() {
                    settings.setShowCalibration(false);
                    settings.save(mSharedPreferences);
                }
            }).show(mFragmentManager, "calibrate");
        }

        if (mGpsDecl) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mStaticActivity);
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10 * 1000); // 10 seconds
            locationRequest.setFastestInterval(5 * 1000); // 5 seconds
            locationManager = (LocationManager) mStaticActivity.getSystemService(Context.LOCATION_SERVICE);
            mSettingsClient = LocationServices.getSettingsClient(mStaticActivity);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            gpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            callLocationLoop();

            mLocationSettingsRequest = builder.build();
            builder.setAlwaysShow(true);
            turnGPSOn();
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            mWayLatitude = location.getLatitude();
                            mWayLongitude = location.getLongitude();

                            mGeomagneticField = new GeomagneticField2020((float)mWayLatitude, (float)mWayLongitude, (float)location.getAltitude(), location.getTime());
                            mDeclination = mGeomagneticField.getDeclination();
                            Log.i(TAG, String.format(Locale.US, "LATLNG [%f, %f] DECL[%f] [C]", mWayLatitude, mWayLongitude, mDeclination));
                            if (mDeclination < 0) {
                                mTextDeclinatonOrientation.setText("W");
                            } else if (mDeclination > 0) {
                                mTextDeclinatonOrientation.setText("E");
                            } else {
                                mTextDeclinatonOrientation.setText("");
                            }
                            if (settings.getAutoUpdateManualDeclination()) {
                                settings.setManualDeclinationValue(rounded((float)mDeclination));
                                mManualDecl = rounded((float)mDeclination);
                            }
                            if (mManDecl) {
                                mDeclination = mManualDecl;
                            }
                            mTextDeclinatonDegrees.setText(String.format(Locale.US, "%.1f°", Math.abs(rounded((float)mDeclination))));
                        }
                    }
                }
            };
        }

        angles = new Float[M];
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        typeface = Typeface.createFromAsset(getAssets(), "fonts/lcd.ttf");

        mTextDeclinatonDegrees.setTypeface(typeface);
        mTextDeclinatonOrientation.setTypeface(typeface);
        mTextOrientation.setTypeface(typeface);
        mTextDegrees.setTypeface(typeface);
        mTextBezelDegrees.setTypeface(typeface);
        mTextBezelRevDegrees.setTypeface(typeface);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        screenDensity = getResources().getDisplayMetrics().density;

        compassView = findViewById(R.id.compass);
        compassView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int H = v.getHeight();
                int W = v.getWidth();
                float x = event.getX();
                float y = event.getY();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    tx = x;
                    ty = y;
                    count = 0;
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    count++;
                    float X = x - 225.0f;
                    float Y = y - 225.0f;
                    float TX = tx - 225.0f;
                    float TY = ty - 225.0f;
                    float ddd = (float) (57.29577951308232d * ((double) ((float) Math.asin((double) (-((float) (((double) ((X * TY) - (Y * TX))) / Math.sqrt((double) (((X * X) + (Y * Y)) * ((TX * TX) + (TY * TY)))))))))));
                    //float ddd = (float) Math.toDegrees(((double) ((float) Math.asin((double) (-((float) (((double) ((X * TY) - (Y * TX))) / Math.sqrt((double) (((X * X) + (Y * Y)) * ((TX * TX) + (TY * TY)))))))))));
                    for (int i = 1; i < M; i++) {
                        angles[i - 1] = angles[i];
                    }
                    angles[M - 1] = ddd;
                    if (((double) Math.abs(ddd)) > 0.1d) {
                        tx = x;
                        ty = y;
                        lastangle -= ddd;
                        if (lastangle < 0) lastangle += 360;
                        if (lastangle > 360) lastangle -= 360;
                        mTextBezelDegrees.setText(((int) lastangle) + "°");
                        float revAngle = lastangle - 180;
                        if (revAngle < 0) revAngle += 360;
                        if (revAngle > 360) revAngle -= 360;
                        mTextBezelRevDegrees.setText(((int) revAngle) + "°");
                        compassView.setBezelDegrees(lastangle);
                        settings.setBearingDirection((int)lastangle);
                        lasttime = System.currentTimeMillis();
                    }
                    return true;
                } else {
                    return true;
                }
            }
        });

        mTextBezelDegrees.setText("0°");
        mTextBezelRevDegrees.setText("180°");

        getLocation();

        compassView.setNight(mNight);
        if (mNight) {
            final int color = getColor(R.color.nightred);
            mTextOrientation.setTextColor(color);
            mTextDeclinatonOrientation.setTextColor(color);
            mSettingsButtons.setImageTintList(new ColorStateList(new int[][]{{}}, new int[]{ color }));
        } else {
            final int tcolor = getColor(R.color.offwhite);
            mTextOrientation.setTextColor(tcolor);
            mTextDeclinatonOrientation.setTextColor(tcolor);
            final int bcolor = getColor(R.color.daytintbtn);
            mSettingsButtons.setImageTintList(new ColorStateList(new int[][]{{}}, new int[]{ bcolor }));
        }
        if (mAltRose) {
            compassView.setRose(2);
        } else {
            compassView.setRose(1);
        }
        lastangle = mBearingDirection;
        compassView.setBezelDegrees(mBearingDirection);
        int bearing = mBearingDirection - 180;
        if (bearing < 0) bearing += 360;
        if (bearing > 360) bearing -= 360;
        mTextBezelDegrees.setText(mBearingDirection + "°");
        mTextBezelRevDegrees.setText(bearing + "°");

        if (mGpsDecl || mManDecl) {
            if (mManDecl) {
                String orientation = " W";
                if (mManualDecl < 0) {
                    orientation = " W";
                } else if (mManualDecl > 0) {
                    orientation = " E";
                } else {
                    orientation = "";
                }
                mTextDeclinatonDegrees.setText(String.format(Locale.US, "%.1f°", Math.abs(rounded((float)mManualDecl))));
                mTextDeclinatonOrientation.setText(String.valueOf(orientation));
            }
            mDeclinationLLFrame.setVisibility(View.VISIBLE);
        } else {
            mDeclinationLLFrame.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        settings.save(mSharedPreferences);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_FASTEST);
        hasMagnetic = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        hasAccel = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (!(hasMagneticCheck || hasMagnetic)) {
            if (mFragmentManager.findFragmentByTag("noSensorError") == null) {
                NoSensorErrorDialogFragment.newInstance(null).show(mFragmentManager, "noSensorError");
            }
            hasMagneticCheck = true;
        }
        if (!(hasAccelCheck || hasAccel)) {
            if (mFragmentManager.findFragmentByTag("noSensorError") == null) {
                NoSensorErrorDialogFragment.newInstance(null).show(mFragmentManager, "noSensorError");
            }
            hasAccelCheck = true;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int arg1) {
    }

    @Override
    protected void onDestroy() {
        if (mGpsDecl && mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String[] compass_direction = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
        double angle;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                switch (mScreenRotation) {
                    case Surface.ROTATION_0:
                        orientation[1] = -orientation[1];
                        break;

                    case Surface.ROTATION_90: {
                        orientation[0] += Math.PI / 2f;
                        float tmpOldPitch = orientation[1];
                        orientation[1] = -orientation[2];
                        orientation[2] = -tmpOldPitch;
                        break;
                    }

                    case Surface.ROTATION_180: {
                        orientation[0] = (float) (orientation[0] > 0f ? (orientation[0] - Math.PI) : (orientation[0] + Math.PI));// offset
                        orientation[2] = -orientation[2];
                        break;
                    }

                    case Surface.ROTATION_270: {
                        orientation[0] -= Math.PI / 2;
                        float tmpOldPitch = orientation[1];
                        orientation[1] = orientation[2];
                        orientation[2] = tmpOldPitch;
                        break;
                    }
                }
                angle = Math.toDegrees(orientation[0]);
                if (angle < 0) {
                    angle += 360;
                }
                if (angle > 360) {
                    angle -= 360;
                }
                curRotateBG = 0.0d - angle;
                mTextDegrees.setText(((int) angle) + "°");
                int index = (int) ((angle + 22.5d) / 45.0d);
                if (index < compass_direction.length && index >= 0) {
                    mTextOrientation.setText(" " + compass_direction[(int) ((angle + 22.5d) / 45.0d)]);
                }
                if (compassView == null) {
                    return;
                }
                if (!mGpsDecl) {
                    mDeclination = 0;
                }
                if (hasItem) {
                    if (angle < 0) angle += 360;
                    if (angle > 360) angle -= 360;
                    if (mGpsDecl) {
                        angle -= mDeclination;
                        if (angle < 0) angle += 360;
                        if (angle > 360) angle -= 360;
                    } else if (mManDecl && !mGpsDecl) {
                        angle -= mManualDecl;
                        if (angle < 0) angle += 360;
                        if (angle > 360) angle -= 360;
                    }
                    compassView.setRoseDegrees((float) angle);
                } else {
                    hasItem = true;
                }
            }
        }
    }

    private void callLocationLoop() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callLocationLoop();
            }
        }, 100);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (isGPSEnabled) {
            return;
        }
        if (gpsProviderEnabled || networkProviderEnabled) {
            if (networkProviderEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 1000, 30, (LocationListener) mStaticActivity);
                if (locationManager != null) {
                    mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    String providerType = "network";
                    Log.d(TAG, "network lbs provider:" +
                            (mLocation == null ? "null" : String.valueOf(mLocation.getLatitude()) + ","
                                    + String.valueOf(mLocation.getLongitude())));
                    if (mLocation != null) {
                        mWayLatitude = mLocation.getLatitude();
                        mWayLongitude = mLocation.getLongitude();
                    }
                }
            }

            if (gpsProviderEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000, 1, (LocationListener) mStaticActivity);
                if (locationManager != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    String providerType = "gps";
                    Log.d(TAG, "gps lbs provider:" +
                            (mLocation == null ? "null" : String.valueOf(mLocation.getLatitude()) + "," +
                                    String.valueOf(mLocation.getLongitude())));
                    if (mLocation != null) {
                        mWayLatitude = mLocation.getLatitude();
                        mWayLongitude = mLocation.getLongitude();
                    }
                }
            }
        }
    }

    private void getLocation() {
        if (!isGooglePlayServicesAvailable(mStaticActivity)) {
            return;
        }
        if (!mGpsDecl) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(mStaticActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mStaticActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mStaticActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST);
        } else {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(mStaticActivity, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mWayLatitude = location.getLatitude();
                        mWayLongitude = location.getLongitude();
                        mGeomagneticField = new GeomagneticField2020((float)mWayLatitude, (float)mWayLongitude, (float)location.getAltitude(), location.getTime());
                        mDeclination = mGeomagneticField.getDeclination();
                        Log.i(TAG, String.format(Locale.US, "LATLNG [%f, %f] DECL[%f]", mWayLatitude, mWayLongitude, mDeclination));
                        if (mDeclination < 0) {
                            mTextDeclinatonOrientation.setText(" W");
                        } else if (mDeclination > 0) {
                            mTextDeclinatonOrientation.setText(" E");
                        } else {
                            mTextDeclinatonOrientation.setText("");
                        }
                        if (settings.getAutoUpdateManualDeclination()) {
                            settings.setManualDeclinationValue(rounded((float)mDeclination));
                            mManualDecl = rounded((float)mDeclination);
                        }
                        if (mManDecl) {
                            mDeclination = mManualDecl;
                        }
                        mTextDeclinatonDegrees.setText(String.format(Locale.US, "%.1f°", Math.abs(rounded((float)mDeclination))));
                    } else {
                        if (ActivityCompat.checkSelfPermission(mStaticActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(mStaticActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mScreenRotation = getWindowManager().getDefaultDisplay().getRotation();
    }

    private void setupOnRequestPermissionsResult() {
        ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if(result) {
                            Log.e(TAG, "onActivityResult: PERMISSION GRANTED");
                            mUserPermissionDenied = false;
                        } else {
                            Log.e(TAG, "onActivityResult: PERMISSION DENIED");
                            mUserPermissionDenied = true;
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mUserPermissionDenied = false;
                } else {
                    mUserPermissionDenied = true;
                }
                break;
            }
        }
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void turnGPSOn() {
        if (!isGooglePlayServicesAvailable(mStaticActivity)) {
            isGPSEnabled = false;
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "GPS is enabled");
            isGPSEnabled = true;
        } else {
            mSettingsClient
                    .checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(mStaticActivity, new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG, "GPS is enabled");
                            isGPSEnabled = true;
                        }
                    })
                    .addOnCanceledListener(new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            Log.i(TAG, "GPS is canceled");
                            isGPSEnabled = false;
                        }
                    })
                    .addOnFailureListener(mStaticActivity, new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            e.printStackTrace();
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                                    isGPSEnabled = false;
                                    try {
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(mStaticActivity, GPS_REQUEST);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.i(TAG, "Unable to execute request.");
                                    }
                                    break;
                                }
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                                    isGPSEnabled = false;
                                    String errorMsg = "Location settings are invalid, go to Settings to change them.";
                                    Log.e(TAG, errorMsg);
                                    Toast.makeText(mStaticActivity, errorMsg, Toast.LENGTH_LONG).show();
                                    break;
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Build.VERSION.SDK_INT < 30) {
                View decorView = getWindow().getDecorView();
                decorView.setSystemUiVisibility(uiOptions);
            } else {
                if (!hideSystemBars()) {
                    View decorView = this.getWindow().getDecorView();
                    //noinspection deprecation
                    decorView.setSystemUiVisibility(uiOptions);
                }
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_REQUEST:
                Intent intent = new Intent(mStaticActivity, CompassActivity.class);
                mStaticActivity.startActivity(intent);
                finish();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                //googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    public boolean isGooglePlayServicesAvailable1(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mWayLatitude = location.getLatitude();
            mWayLongitude = location.getLongitude();
            mGeomagneticField = new GeomagneticField2020((float)mWayLatitude, (float)mWayLongitude, (float)location.getAltitude(), location.getTime());
            mDeclination = mGeomagneticField.getDeclination();
            Log.i(TAG, String.format(Locale.US, "LATLNG [%f, %f] DECL[%f]", mWayLatitude, mWayLongitude, mDeclination));
            if (mDeclination < 0) {
                mTextDeclinatonOrientation.setText(" W");
            } else if (mDeclination > 0) {
                mTextDeclinatonOrientation.setText(" E");
            } else {
                mTextDeclinatonOrientation.setText("");
            }
            if (settings.getAutoUpdateManualDeclination()) {
                settings.setManualDeclinationValue(rounded((float)mDeclination));
                mManualDecl = rounded((float)mDeclination);
            }
            if (mManDecl) {
                mDeclination = mManualDecl;
            }
            mTextDeclinatonDegrees.setText(String.format(Locale.US, "%.1f°", Math.abs(rounded((float)mDeclination))));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private boolean hideSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(mStaticActivity.getWindow().getDecorView());
        if (windowInsetsController == null) {
            return false;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        return true;
    }
    
    private float rounded(float value) {
        float ret = 0.0f;
        int r1 = (int)(value * 10.0f);
        ret = (float)r1 / 10.0f;
        return ret;
    }
}
