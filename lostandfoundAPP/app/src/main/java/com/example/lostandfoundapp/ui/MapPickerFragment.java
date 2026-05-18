package com.example.lostandfoundapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lostandfoundapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapPickerFragment extends Fragment implements OnMapReadyCallback {

    public static final String RESULT_LAT = "picker_lat";
    public static final String RESULT_LNG = "picker_lng";
    public static final String RESULT_ADDRESS = "picker_address";

    private GoogleMap googleMap;
    private Marker marker;
    private FusedLocationProviderClient locationClient;

    private TextView tvAddress;
    private TextView tvLatLng;
    private Button btnConfirm;

    private double pickedLat = 0.0;
    private double pickedLng = 0.0;
    private String pickedAddress = "";

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAddress = view.findViewById(R.id.tv_address);
        tvLatLng = view.findViewById(R.id.tv_latlng);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setEnabled(false);

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                granted -> centerOnCurrentLocation());

        btnConfirm.setOnClickListener(v -> {
            NavController nav = NavHostFragment.findNavController(this);
            NavBackStackEntry previous = nav.getPreviousBackStackEntry();
            if (previous != null) {
                previous.getSavedStateHandle().set(RESULT_LAT, pickedLat);
                previous.getSavedStateHandle().set(RESULT_LNG, pickedLng);
                previous.getSavedStateHandle().set(RESULT_ADDRESS, pickedAddress);
            }
            nav.navigateUp();
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.picker_map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        map.getUiSettings().setZoomControlsEnabled(true);

        // Tap to set
        map.setOnMapClickListener(this::setMarker);
        map.setOnMapLongClickListener(this::setMarker);

        // Drag to refine
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(@NonNull Marker m) {}
            @Override public void onMarkerDrag(@NonNull Marker m) {}
            @Override
            public void onMarkerDragEnd(@NonNull Marker m) {
                updateSelection(m.getPosition());
            }
        });

        if (hasLocationPermission()) {
            centerOnCurrentLocation();
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
    private void centerOnCurrentLocation() {
        if (googleMap == null) return;
        if (!hasLocationPermission()) {
            // Fallback: center on a default location
            LatLng fallback = new LatLng(-37.8136, 144.9631); // Melbourne
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fallback, 12f));
            return;
        }
        googleMap.setMyLocationEnabled(true);
        locationClient.getLastLocation().addOnSuccessListener(loc -> {
            LatLng center;
            if (loc != null) {
                center = new LatLng(loc.getLatitude(), loc.getLongitude());
            } else {
                center = new LatLng(-37.8136, 144.9631);
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 14f));
        });
    }

    private void setMarker(LatLng pos) {
        if (marker == null) {
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .draggable(true)
                    .title("Selected location"));
        } else {
            marker.setPosition(pos);
        }
        updateSelection(pos);
    }

    private void updateSelection(LatLng pos) {
        pickedLat = pos.latitude;
        pickedLng = pos.longitude;
        pickedAddress = reverseGeocode(pickedLat, pickedLng);
        tvAddress.setText(pickedAddress);
        tvLatLng.setText(String.format(Locale.getDefault(),
                "Lat: %.5f,  Lng: %.5f", pickedLat, pickedLng));
        btnConfirm.setEnabled(true);
    }

    private String reverseGeocode(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(lat, lng, 1);
            if (list != null && !list.isEmpty()) {
                return list.get(0).getAddressLine(0);
            }
        } catch (Exception ignored) {}
        return String.format(Locale.getDefault(), "%.5f, %.5f", lat, lng);
    }
}
