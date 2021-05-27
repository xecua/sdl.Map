package jp.ac.titech.itpro.sdl.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final static String TAG = MainActivity.class.getSimpleName();

    private final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final static int REQ_PERMISSIONS = 1234;

    private TextView infoView;
    private GoogleMap map;

    private FusedLocationProviderClient locationClient;
    private LocationRequest request;
    private LocationCallback callback;

    private FloatingActionButton fabGpsFix;
    private Drawable gpsNotFixedIcon, gpsFixedIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        infoView = findViewById(R.id.info_view);
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (fragment != null) {
            Log.d(TAG, "onCreate: getMapAsync");
            fragment.getMapAsync(this);
        }

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        request = LocationRequest.create();
        request.setInterval(10000L);
        request.setFastestInterval(5000L);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Log.d(TAG, "onLocationResult");
                Location location = locationResult.getLastLocation();
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                infoView.setText(getString(R.string.latlng_format, ll.latitude, ll.longitude));
                if (map == null) {
                    Log.d(TAG, "onLocationResult: map == null");
                    return;
                }
                fabGpsFix.setImageDrawable(gpsFixedIcon);
                stopLocationUpdate();
                map.animateCamera(CameraUpdateFactory.newLatLng(ll));
            }
        };

        gpsNotFixedIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_gps_not_fixed_24, null);
        gpsFixedIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_gps_fixed_24, null);
        fabGpsFix = findViewById(R.id.fab_gps_fix);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startLocationUpdate(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        stopLocationUpdate();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady");
        map.moveCamera(CameraUpdateFactory.zoomTo(15f));
        this.map = map;
        map.setOnCameraMoveStartedListener(reason -> {
            if (reason == REASON_GESTURE) {
                fabGpsFix.setImageDrawable(gpsNotFixedIcon);
            }
        });
    }

    private void startLocationUpdate(boolean reqPermission) {
        Log.d(TAG, "startLocationUpdate");
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                if (reqPermission) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, REQ_PERMISSIONS);
                } else {
                    String text = getString(R.string.toast_requires_permission_format, permission);
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        locationClient.requestLocationUpdates(request, callback, null);
    }

    public void onClickFixGps(View view) {
        Log.d(TAG, "onClickFixGps");
        startLocationUpdate(true);
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] permissions, @NonNull int[] grants) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (reqCode == REQ_PERMISSIONS) {
            startLocationUpdate(false);
        }
    }

    private void stopLocationUpdate() {
        Log.d(TAG, "stopLocationUpdate");
        locationClient.removeLocationUpdates(callback);
    }
}