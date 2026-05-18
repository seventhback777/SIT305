package com.example.lostandfoundapp.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lostandfoundapp.R;
import com.example.lostandfoundapp.database.DatabaseHelper;
import com.example.lostandfoundapp.model.Post;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreatePostFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private String postType;

    private EditText etLocation;
    private TextView tvLatLng;
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;

    private FusedLocationProviderClient locationClient;

    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        NavController navController = NavHostFragment.findNavController(this);

        // Initialize Places SDK once
        if (!Places.isInitialized()) {
            String apiKey = getString(R.string.maps_api_key);
            Places.initialize(requireContext().getApplicationContext(), apiKey);
        }
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (getArguments() != null) {
            postType = getArguments().getString("postType", "Lost");
        }

        TextView tvPostType = view.findViewById(R.id.tv_post_type);
        EditText etName = view.findViewById(R.id.et_name);
        EditText etPhone = view.findViewById(R.id.et_phone);
        EditText etDescription = view.findViewById(R.id.et_description);
        EditText etDate = view.findViewById(R.id.et_date);
        etLocation = view.findViewById(R.id.et_location);
        tvLatLng = view.findViewById(R.id.tv_latlng);
        Button btnCurrentLocation = view.findViewById(R.id.btn_current_location);
        Button btnPickOnMap = view.findViewById(R.id.btn_pick_on_map);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        Button btnSubmit = view.findViewById(R.id.btn_submit);

        tvPostType.setText(postType);
        if ("Lost".equals(postType)) {
            tvPostType.setBackgroundResource(R.drawable.badge_lost);
        } else {
            tvPostType.setBackgroundResource(R.drawable.badge_found);
        }

        // Autocomplete launcher
        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK
                            && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        etLocation.setText(place.getAddress());
                        if (place.getLatLng() != null) {
                            selectedLat = place.getLatLng().latitude;
                            selectedLng = place.getLatLng().longitude;
                            updateLatLngLabel();
                        }
                    }
                });

        // Permission launcher (for current location)
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                granted -> {
                    Boolean fine = granted.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarse = granted.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                    if ((fine != null && fine) || (coarse != null && coarse)) {
                        fetchCurrentLocation();
                    } else {
                        Toast.makeText(requireContext(),
                                "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // Tap location field -> Places autocomplete
        etLocation.setOnClickListener(v -> launchAutocomplete());

        btnPickOnMap.setOnClickListener(v ->
                navController.navigate(R.id.action_create_to_picker));

        // Observe picker result on returning to this destination
        NavBackStackEntry currentEntry = navController.getCurrentBackStackEntry();
        if (currentEntry != null) {
            MutableLiveData<Double> latLive =
                    currentEntry.getSavedStateHandle().getLiveData(MapPickerFragment.RESULT_LAT);
            latLive.observe(getViewLifecycleOwner(), lat -> {
                if (lat == null) return;
                Double lng = currentEntry.getSavedStateHandle()
                        .get(MapPickerFragment.RESULT_LNG);
                String addr = currentEntry.getSavedStateHandle()
                        .get(MapPickerFragment.RESULT_ADDRESS);
                if (lng != null) {
                    selectedLat = lat;
                    selectedLng = lng;
                    etLocation.setText(addr != null ? addr : "");
                    updateLatLngLabel();
                }
                // Clear so it doesn't fire again on rotation
                currentEntry.getSavedStateHandle().remove(MapPickerFragment.RESULT_LAT);
                currentEntry.getSavedStateHandle().remove(MapPickerFragment.RESULT_LNG);
                currentEntry.getSavedStateHandle().remove(MapPickerFragment.RESULT_ADDRESS);
            });
        }

        btnCurrentLocation.setOnClickListener(v -> {
            if (hasLocationPermission()) {
                fetchCurrentLocation();
            } else {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });

        // Date picker on click
        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (dp, year, month, day) ->
                    etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            if (name.isEmpty() || phone.isEmpty() || description.isEmpty()
                    || date.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Post post = new Post();
            post.setPostType(postType);
            post.setName(name);
            post.setPhone(phone);
            post.setDescription(description);
            post.setDate(date);
            post.setLocation(location);
            post.setCategory(category);
            post.setImagePath(null);
            post.setLatitude(selectedLat);
            post.setLongitude(selectedLng);

            long result = dbHelper.insertPost(post);
            if (result != -1) {
                Toast.makeText(requireContext(), "Post created!", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_create_to_list);
            } else {
                Toast.makeText(requireContext(), "Failed to create post",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void launchAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields).build(requireContext());
        autocompleteLauncher.launch(intent);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation().addOnSuccessListener(loc -> {
            if (loc == null) {
                Toast.makeText(requireContext(),
                        "Unable to get location. Try again or pick on map.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            selectedLat = loc.getLatitude();
            selectedLng = loc.getLongitude();
            String addressText = reverseGeocode(selectedLat, selectedLng);
            etLocation.setText(addressText);
            updateLatLngLabel();
        }).addOnFailureListener(e ->
                Toast.makeText(requireContext(),
                        "Location error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
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

    private void updateLatLngLabel() {
        tvLatLng.setText(String.format(Locale.getDefault(),
                "Lat: %.5f,  Lng: %.5f", selectedLat, selectedLng));
    }
}
