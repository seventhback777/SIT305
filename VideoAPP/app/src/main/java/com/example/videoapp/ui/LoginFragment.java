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
import com.example.videoapp.data.SessionManager;
import com.example.videoapp.viewmodel.UserViewModel;

public class LoginFragment extends Fragment {

    private UserViewModel userViewModel;
    private EditText etUsername, etPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        TextView tvGoRegister = view.findViewById(R.id.tv_go_register);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            userViewModel.login(username, password);
        });

        tvGoRegister.setOnClickListener(v ->
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_login_to_register));

        userViewModel.loginError.observe(getViewLifecycleOwner(), code -> {
            if (code == null) return;
            if (code == UserViewModel.RESULT_LOGIN_USER_NOT_FOUND) {
                etUsername.setError("Username not found");
                etUsername.requestFocus();
            } else if (code == UserViewModel.RESULT_LOGIN_WRONG_PASSWORD) {
                etPassword.setError("Incorrect password");
                etPassword.requestFocus();
            }
            userViewModel.loginError.setValue(null);
        });

        userViewModel.loginResult.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                new SessionManager(requireContext()).saveUserId(user.id);
                userViewModel.loginResult.setValue(null);
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_login_to_home);
            }
        });
    }
}
