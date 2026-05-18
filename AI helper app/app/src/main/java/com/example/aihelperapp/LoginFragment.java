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

public class LoginFragment extends Fragment {

    private UserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        if (userViewModel.isLoggedIn()) {
            navigateAfterLogin(view);
            return;
        }

        com.google.android.material.textfield.TextInputEditText etUsername = view.findViewById(R.id.etUsername);
        com.google.android.material.textfield.TextInputEditText etPassword = view.findViewById(R.id.etPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        TextView tvError = view.findViewById(R.id.tvError);
        TextView tvGoToRegister = view.findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            if (username.isEmpty() || password.isEmpty()) {
                tvError.setText("Please fill in all fields");
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            tvError.setVisibility(View.GONE);
            userViewModel.login(username, password);
        });

        tvGoToRegister.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_login_to_register));

        userViewModel.loginSuccess.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) navigateAfterLogin(view);
        });

        userViewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                tvError.setText(msg);
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void navigateAfterLogin(View view) {
        // 守卫：只有真正登录状态才跳转
        if (!userViewModel.isLoggedIn()) return;
        userViewModel.hasInterests().observe(getViewLifecycleOwner(), hasInterests -> {
            if (hasInterests == null) return;
            if (Boolean.TRUE.equals(hasInterests)) {
                Navigation.findNavController(view).navigate(R.id.action_login_to_home);
            } else {
                Navigation.findNavController(view).navigate(R.id.action_login_to_interest);
            }
        });
    }
}
