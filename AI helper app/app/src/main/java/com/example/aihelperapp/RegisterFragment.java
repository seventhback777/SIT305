package com.example.aihelperapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

public class RegisterFragment extends Fragment {

    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        com.google.android.material.textfield.TextInputEditText etFullName = view.findViewById(R.id.etFullName);
        com.google.android.material.textfield.TextInputEditText etUsername = view.findViewById(R.id.etUsername);
        com.google.android.material.textfield.TextInputEditText etEmail = view.findViewById(R.id.etEmail);
        com.google.android.material.textfield.TextInputEditText etConfirmEmail = view.findViewById(R.id.etConfirmEmail);
        com.google.android.material.textfield.TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        com.google.android.material.textfield.TextInputEditText etPassword = view.findViewById(R.id.etPassword);
        com.google.android.material.textfield.TextInputEditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        TextView tvError = view.findViewById(R.id.tvError);
        TextView tvGoToLogin = view.findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> {
            String fullName = getText(etFullName);
            String username = getText(etUsername);
            String email = getText(etEmail);
            String confirmEmail = getText(etConfirmEmail);
            String phone = getText(etPhone);
            String password = getText(etPassword);
            String confirmPassword = getText(etConfirmPassword);

            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() ||
                    confirmEmail.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                tvError.setText("Please fill in all fields");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            tvError.setVisibility(View.GONE);
            userViewModel.register(fullName, username, email, confirmEmail, password, confirmPassword, phone);
        });

        tvGoToLogin.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_register_to_login));

        userViewModel.registerSuccess.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success))
                Navigation.findNavController(view).navigate(R.id.action_register_to_interest);
        });

        userViewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                tvError.setText(msg);
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private String getText(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
