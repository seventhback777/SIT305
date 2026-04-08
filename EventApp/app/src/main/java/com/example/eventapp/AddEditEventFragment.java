package com.example.eventapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddEditEventFragment extends Fragment {

    private EventViewModel viewModel;

    private EditText  etTitle, etLocation, etDescription, etDateTime;
    private Spinner   spinnerCategory;
    private Button    btnSave;

    private int  eventId = -1;      // -1 = create mode
    private long selectedDateTimeMs = -1;

    private final String[] CATEGORIES = {"Work", "Social", "Travel", "Health", "Other"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        etTitle       = view.findViewById(R.id.et_title);
        etLocation    = view.findViewById(R.id.et_location);
        etDescription = view.findViewById(R.id.et_description);
        etDateTime    = view.findViewById(R.id.et_date_time);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        btnSave       = view.findViewById(R.id.btn_save);

        // Set up category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // DateTime picker: tap field to open DatePicker → then TimePicker
        etDateTime.setFocusable(false);
        etDateTime.setClickable(true);
        etDateTime.setOnClickListener(v -> showDateTimePicker());

        // Determine mode from args
        if (getArguments() != null) {
            eventId = getArguments().getInt("eventId", -1);
        }

        if (eventId != -1) {
            // Edit mode: load event and pre-fill
            viewModel.getEventById(eventId).observe(getViewLifecycleOwner(), event -> {
                if (event == null) return;
                etTitle.setText(event.getTitle());
                etLocation.setText(event.getLocation());
                etDescription.setText(event.getDescription());
                selectedDateTimeMs = event.getDateTime();
                etDateTime.setText(formatDateTime(selectedDateTimeMs));

                // Set spinner to saved category
                for (int i = 0; i < CATEGORIES.length; i++) {
                    if (CATEGORIES[i].equals(event.getCategory())) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            });
        }

        btnSave.setOnClickListener(v -> saveEvent(view));
    }

    // ── Date + Time Picker ─────────────────────────────────────────────────────

    private void showDateTimePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (dp, year, month, day) -> {
                    // After date selected, open time picker
                    TimePickerDialog timePicker = new TimePickerDialog(
                            requireContext(),
                            (tp, hour, minute) -> {
                                Calendar selected = Calendar.getInstance();
                                selected.set(year, month, day, hour, minute, 0);
                                selected.set(Calendar.MILLISECOND, 0);
                                selectedDateTimeMs = selected.getTimeInMillis();
                                etDateTime.setText(formatDateTime(selectedDateTimeMs));
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                    );
                    timePicker.show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        // Prevent past dates at UI level
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    // ── Validation + Save ──────────────────────────────────────────────────────

    private void saveEvent(View view) {
        String title       = etTitle.getText().toString().trim();
        String location    = etLocation.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category    = spinnerCategory.getSelectedItem().toString();

        // Validate title
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date selected
        if (selectedDateTimeMs == -1) {
            Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date not in past
        if (selectedDateTimeMs < System.currentTimeMillis()) {
            Toast.makeText(getContext(), "Date cannot be in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventId == -1) {
            // Create mode
            Event newEvent = new Event(title, category, location, selectedDateTimeMs, description);
            viewModel.insertEvent(newEvent);
        } else {
            // Edit mode
            Event updatedEvent = new Event(title, category, location, selectedDateTimeMs, description);
            updatedEvent.setId(eventId);
            viewModel.updateEvent(updatedEvent);
        }

        // Return to EventListFragment
        Navigation.findNavController(view)
                .navigate(R.id.action_addEditEventFragment_to_eventListFragment);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String formatDateTime(long ms) {
        SimpleDateFormat sdf =
                new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault());
        return sdf.format(new Date(ms));
    }
}