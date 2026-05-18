package com.example.videoapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapp.R;
import com.example.videoapp.adapter.PlaylistAdapter;
import com.example.videoapp.data.SessionManager;
import com.example.videoapp.viewmodel.VideoViewModel;

public class PlaylistFragment extends Fragment {

    private VideoViewModel videoViewModel;
    private PlaylistAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoViewModel = new ViewModelProvider(requireActivity()).get(VideoViewModel.class);
        sessionManager = new SessionManager(requireContext());

        RecyclerView rvPlaylist = view.findViewById(R.id.rv_playlist);
        View tvEmpty = view.findViewById(R.id.tv_empty);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        adapter = new PlaylistAdapter(
                entry -> {
                    // Load video in HomeFragment then switch to it
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                            .navigate(R.id.homeFragment);
                    // Pass URL via shared ViewModel or navigate with args
                    videoViewModel.selectedUrl.setValue(entry.videoUrl);
                },
                entry -> videoViewModel.deleteVideo(entry)
        );

        rvPlaylist.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPlaylist.setAdapter(adapter);

        int userId = sessionManager.getUserId();
        videoViewModel.getVideosForUser(userId).observe(getViewLifecycleOwner(), entries -> {
            adapter.submitList(entries);
            if (entries.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvPlaylist.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvPlaylist.setVisibility(View.VISIBLE);
            }
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build();
            Navigation.findNavController(view).navigate(R.id.loginFragment, null, navOptions);
        });
    }
}
