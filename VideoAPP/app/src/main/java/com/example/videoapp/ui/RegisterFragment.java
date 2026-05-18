package com.example.videoapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.videoapp.R;
import com.example.videoapp.viewmodel.UserViewModel;

public class RegisterFragment extends Fragment {

    private UserViewModel userViewModel;
    private EditText etFullName, etUsername, etPassword, etConfirmPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        etFullName = view.findViewById(R.id.et_full_name);
        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        Button btnRegister = view.findViewById(R.id.btn_register);
        TextView tvGoLogin = view.findViewById(R.id.tv_go_login);

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirm = etConfirmPassword.getText().toString();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            userViewModel.register(fullName, username, password);
        });

        tvGoLogin.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigateUp());

        userViewModel.registerResult.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            userViewModel.registerResult.setValue(null); // consume

            switch (result) {
                case UserViewModel.RESULT_SUCCESS:
                    Toast.makeText(requireContext(), "Account created! Please log in.", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                            .navigateUp();
                    break;
                case UserViewModel.RESULT_USERNAME_TAKEN:
                    Toast.makeText(requireContext(), "Username already taken", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(requireContext(), "Registration failed, try again", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
