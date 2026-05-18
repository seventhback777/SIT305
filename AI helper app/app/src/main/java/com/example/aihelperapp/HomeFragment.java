package com.example.aihelperapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {

    private UserViewModel userViewModel;
    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        RecyclerView rvTasks = view.findViewById(R.id.rvTasks);
        Button btnGenerate = view.findViewById(R.id.btnGenerate);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnProfile = view.findViewById(R.id.btnProfile);
        Button btnHistory = view.findViewById(R.id.btnHistory);
        Button btnUpgrade = view.findViewById(R.id.btnUpgrade);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvEmpty = view.findViewById(R.id.tvEmpty);

        int userId = userViewModel.getLoggedInUserId();

        taskAdapter = new TaskAdapter(task -> {
            Bundle args = new Bundle();
            args.putInt("taskId", task.id);
            Navigation.findNavController(view).navigate(R.id.action_home_to_quiz, args);
        });

        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(taskAdapter);

        taskViewModel.getTasksForUser(userId).observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.setTasks(tasks);
            tvEmpty.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
        });

        taskViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            btnGenerate.setEnabled(!Boolean.TRUE.equals(loading));
        });

        taskViewModel.errorMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        btnGenerate.setOnClickListener(v -> taskViewModel.generateAndSaveTasks(userId));

        btnLogout.setOnClickListener(v -> {
            userViewModel.logout();
            Navigation.findNavController(view).navigate(R.id.action_home_to_login);
        });

        btnProfile.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_home_to_profile));

        btnHistory.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_home_to_history));

        btnUpgrade.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_home_to_upgrade));
    }
}
