package com.example.aichatapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.aichatapp.R;
import com.example.aichatapp.database.AppDatabase;
import com.example.aichatapp.model.User;

public class LoginFragment extends Fragment {

    private EditText etUsername;
    private Button btnGo;
    private AppDatabase db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etUsername = view.findViewById(R.id.et_username);
        btnGo      = view.findViewById(R.id.btn_go);
        db         = AppDatabase.getDatabase(requireContext());

        btnGo.setOnClickListener(v -> {
            String username = etUsername.getText() != null
                    ? etUsername.getText().toString().trim() : "";

            if (username.isEmpty()) {
                etUsername.setError(getString(R.string.error_empty_username));
                return;
            }
            loginOrRegister(username);
        });
    }

    private void loginOrRegister(String username) {
        btnGo.setEnabled(false);

        AppDatabase.getExecutor().execute(() -> {
            User existing = db.userDao().findByUsername(username);
            long userId;

            if (existing != null) {
                userId = existing.uid;
            } else {
                userId = db.userDao().insertUser(new User(username));
            }

            long finalUserId = userId;
            requireActivity().runOnUiThread(() -> {
                btnGo.setEnabled(true);

                if (finalUserId <= 0) {
                    Toast.makeText(requireContext(),
                            R.string.error_db, Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle args = new Bundle();
                args.putLong("userId", finalUserId);
                args.putString("username", username);

                NavController nav = NavHostFragment.findNavController(this);
                nav.navigate(R.id.action_loginFragment_to_chatFragment, args);
            });
        });
    }
}
