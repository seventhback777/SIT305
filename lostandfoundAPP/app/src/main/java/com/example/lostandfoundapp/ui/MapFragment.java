package com.example.lostandfoundapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lostandfoundapp.R;
import com.example.lostandfoundapp.database.DatabaseHelper;
import com.example.lostandfoundapp.model.Post;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private DatabaseHelper dbHelper;
    private FusedLocationProviderClient locationClient;

    private TextView tvRadius;
    private TextView tvCount;
    private SeekBar seekRadius;

    private int radiusKm = 10;
    private LatLng userLocation;          // null until permission + fix
    private List<Post> allPosts = new ArrayList<>();
    private com.google.android.gms.maps.model.Circle radiusCircle;

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        tvRadius = view.findViewById(R.id.tv_radius);
        tvCount = view.findViewById(R.id.tv_count);
        seekRadius = view.findViewById(R.id.seek_radius);

        seekRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusKm = progress + 1;          // 1..50
                tvRadius.setText(String.format(Locale.getDefault(),
                        "Radius: %d km", radiusKm));
                refreshMap();
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                granted -> {
                    Boolean fine = granted.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarse = granted.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                    if ((fine != null && fine) || (coarse != null && coarse)) {
                        loadUserLocation();
                    } else {
                        Toast.makeText(requireContext(),
                                "Showing all items (no location permission)",
                                Toast.LENGTH_SHORT).show();
                        refreshMap();
                    }
                });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_view);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        map.getUiSettings().setZoomControlsEnabled(true);

        allPosts = dbHelper.getAllPosts();

        if (hasLocationPermission()) {
            loadUserLocation();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void loadUserLocation() {
        if (googleMap != null) googleMap.setMyLocationEnabled(true);
        locationClient.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                userLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
            }
            refreshMap();
        }).addOnFailureListener(e -> refreshMap());
    }

    private void refreshMap() {
        if (googleMap == null) return;
        googleMap.clear();

        int shown = 0;
        LatLng firstMarker = null;

        for (Post p : allPosts) {
            if (!p.hasLocation()) continue;

            LatLng pos = new LatLng(p.getLatitude(), p.getLongitude());

            // Radius filter (only if we know user location)
            if (userLocation != null) {
                float[] result = new float[1];
                Location.distanceBetween(
                        userLocation.latitude, userLocation.longitude,
                        pos.latitude, pos.longitude, result);
                double distanceKm = result[0] / 1000.0;
                if (distanceKm > radiusKm) continue;
            }

            float hue = "Lost".equalsIgnoreCase(p.getPostType())
                    ? BitmapDescriptorFactory.HUE_RED
                    : BitmapDescriptorFactory.HUE_GREEN;

            googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(p.getPostType() + ": " + p.getName())
                    .snippet(p.getLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));

            if (firstMarker == null) firstMarker = pos;
            shown++;
        }

        // Show radius circle around user
        if (userLocation != null) {
            radiusCircle = googleMap.addCircle(new CircleOptions()
                    .center(userLocation)
                    .radius(radiusKm * 1000.0)
                    .strokeColor(0x553F51B5)
                    .fillColor(0x223F51B5)
                    .strokeWidth(3f));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation,
                    zoomForRadius(radiusKm)));
        } else if (firstMarker != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstMarker, 10f));
        }

        tvCount.setText(String.format(Locale.getDefault(),
                "Showing %d items", shown));
    }

    private float zoomForRadius(int km) {
        if (km <= 2) return 14f;
        if (km <= 5) return 13f;
        if (km <= 10) return 12f;
        if (km <= 20) return 11f;
        if (km <= 35) return 10f;
        return 9f;
    }
}
