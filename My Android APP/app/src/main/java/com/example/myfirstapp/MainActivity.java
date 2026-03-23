package com.example.myfirstapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private EditText etInputValue;
    private Button btnConvert;
    private TextView tvResult;

    private final String[] categories = {"Currency", "Travel Units", "Temperature"};

    private final String[] currencyUnits = {"USD", "AUD", "EUR", "JPY", "GBP", "CAD"};
    private final String[] travelUnits = {"mpg", "km/L", "gallon", "liter", "nautical mile", "kilometer"};
    private final String[] temperatureUnits = {"Celsius", "Fahrenheit", "Kelvin"};

    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupCategorySpinner();
        setupConvertButton();
    }

    private void bindViews() {
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        etInputValue = findViewById(R.id.etInputValue);
        btnConvert = findViewById(R.id.btnConvert);
        tvResult = findViewById(R.id.tvResult);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);


        updateUnitSpinners("Currency");

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                updateUnitSpinners(selectedCategory);
                tvResult.setText("Result will appear here");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateUnitSpinners(String category) {
        String[] units;

        switch (category) {
            case "Currency":
                units = currencyUnits;
                break;
            case "Travel Units":
                units = travelUnits;
                break;
            case "Temperature":
                units = temperatureUnits;
                break;
            default:
                units = new String[]{};
                break;
        }

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(unitAdapter);
        spinnerTo.setAdapter(unitAdapter);

        if (units.length > 1) {
            spinnerTo.setSelection(1);
        }
    }

    private void setupConvertButton() {
        btnConvert.setOnClickListener(v -> performConversion());
    }

    private void performConversion() {
        String inputText = etInputValue.getText().toString().trim();

        if (inputText.isEmpty()) {
            Toast.makeText(this, "Please enter a value.", Toast.LENGTH_SHORT).show();
            return;
        }

        double inputValue;
        try {
            inputValue = Double.parseDouble(inputText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = spinnerCategory.getSelectedItem().toString();
        String fromUnit = spinnerFrom.getSelectedItem().toString();
        String toUnit = spinnerTo.getSelectedItem().toString();

        // Identity conversion
        if (fromUnit.equals(toUnit)) {
            tvResult.setText("Converted Value: " + decimalFormat.format(inputValue) + " " + toUnit);
            Toast.makeText(this, "Source and destination units are the same.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Negative value handling
        if (!category.equals("Temperature") && inputValue < 0) {
            Toast.makeText(this, "Negative values are not allowed for this category.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double result = convertValue(category, fromUnit, toUnit, inputValue);
            tvResult.setText("Converted Value: " + decimalFormat.format(result) + " " + toUnit);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private double convertValue(String category, String fromUnit, String toUnit, double value) {
        switch (category) {
            case "Currency":
                return convertCurrency(fromUnit, toUnit, value);

            case "Travel Units":
                return convertTravelUnits(fromUnit, toUnit, value);

            case "Temperature":
                return convertTemperature(fromUnit, toUnit, value);

            default:
                throw new IllegalArgumentException("Unsupported conversion category.");
        }
    }

    // ---------------- Currency ----------------
    private double convertCurrency(String fromUnit, String toUnit, double value) {
        double valueInUSD = toUSD(fromUnit, value);
        return fromUSD(toUnit, valueInUSD);
    }

    private double toUSD(String fromUnit, double value) {
        switch (fromUnit) {
            case "USD":
                return value;
            case "AUD":
                return value / 1.55;
            case "EUR":
                return value / 0.92;
            case "JPY":
                return value / 148.50;
            case "GBP":
                return value / 0.78;
            default:
                throw new IllegalArgumentException("Unsupported currency unit.");
        }
    }

    private double fromUSD(String toUnit, double valueInUSD) {
        switch (toUnit) {
            case "USD":
                return valueInUSD;
            case "AUD":
                return valueInUSD * 1.55;
            case "EUR":
                return valueInUSD * 0.92;
            case "JPY":
                return valueInUSD * 148.50;
            case "GBP":
                return valueInUSD * 0.78;
            default:
                throw new IllegalArgumentException("Unsupported currency unit.");
        }
    }

    // ---------------- Travel Units ----------------
    private double convertTravelUnits(String fromUnit, String toUnit, double value) {
        // Fuel efficiency
        if ((fromUnit.equals("mpg") || fromUnit.equals("km/L")) &&
                (toUnit.equals("mpg") || toUnit.equals("km/L"))) {

            if (fromUnit.equals("mpg") && toUnit.equals("km/L")) {
                return value * 0.425;
            } else if (fromUnit.equals("km/L") && toUnit.equals("mpg")) {
                return value / 0.425;
            }
        }

        // Liquid volume
        if ((fromUnit.equals("gallon") || fromUnit.equals("liter")) &&
                (toUnit.equals("gallon") || toUnit.equals("liter"))) {

            if (fromUnit.equals("gallon") && toUnit.equals("liter")) {
                return value * 3.785;
            } else if (fromUnit.equals("liter") && toUnit.equals("gallon")) {
                return value / 3.785;
            }
        }

        // Distance
        if ((fromUnit.equals("nautical mile") || fromUnit.equals("kilometer")) &&
                (toUnit.equals("nautical mile") || toUnit.equals("kilometer"))) {

            if (fromUnit.equals("nautical mile") && toUnit.equals("kilometer")) {
                return value * 1.852;
            } else if (fromUnit.equals("kilometer") && toUnit.equals("nautical mile")) {
                return value / 1.852;
            }
        }

        throw new IllegalArgumentException("These units are not compatible in Travel Units.");
    }

    // ---------------- Temperature ----------------
    private double convertTemperature(String fromUnit, String toUnit, double value) {
        double celsiusValue;

        switch (fromUnit) {
            case "Celsius":
                celsiusValue = value;
                break;
            case "Fahrenheit":
                celsiusValue = (value - 32) / 1.8;
                break;
            case "Kelvin":
                celsiusValue = value - 273.15;
                break;
            default:
                throw new IllegalArgumentException("Unsupported temperature unit.");
        }

        switch (toUnit) {
            case "Celsius":
                return celsiusValue;
            case "Fahrenheit":
                return (celsiusValue * 1.8) + 32;
            case "Kelvin":
                return celsiusValue + 273.15;
            default:
                throw new IllegalArgumentException("Unsupported temperature unit.");
        }
    }
}