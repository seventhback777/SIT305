package com.example.aihelperapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class InterestFragment extends Fragment {

    private UserViewModel userViewModel;
    private TopicAdapter topicAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interest, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        RecyclerView rvTopics = view.findViewById(R.id.rvTopics);
        Button btnSave = view.findViewById(R.id.btnSaveInterests);
        TextView tvCount = view.findViewById(R.id.tvSelectedCount);

        topicAdapter = new TopicAdapter();
        topicAdapter.setOnSelectionChanged(() ->
                tvCount.setText(topicAdapter.getSelectedCount() + " selected"));

        rvTopics.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvTopics.setAdapter(topicAdapter);

        userViewModel.getAllTopics().observe(getViewLifecycleOwner(), topics ->
                topicAdapter.setTopics(topics));

        btnSave.setOnClickListener(v -> {
            if (topicAdapter.getSelectedCount() == 0) {
                Toast.makeText(requireContext(), "Please select at least one topic", Toast.LENGTH_SHORT).show();
                return;
            }
            userViewModel.saveInterests(topicAdapter.getSelectedIds());
        });

        userViewModel.interestSaved.observe(getViewLifecycleOwner(), saved -> {
            if (Boolean.TRUE.equals(saved))
                Navigation.findNavController(view).navigate(R.id.action_interest_to_home);
        });
    }
}
