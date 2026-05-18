package com.example.aihelperapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        RecyclerView rvHistory = view.findViewById(R.id.rvHistory);
        TextView tvEmpty = view.findViewById(R.id.tvEmpty);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        HistoryAdapter adapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);

        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        int userId = userViewModel.getLoggedInUserId();

        userViewModel.getHistoryLive(userId).observe(getViewLifecycleOwner(), histories -> {
            adapter.setItems(histories);
            tvEmpty.setVisibility(histories.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
