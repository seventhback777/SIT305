package com.example.lostandfoundapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lostandfoundapp.R;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);

        Button btnLost = view.findViewById(R.id.btn_report_lost);
        Button btnFound = view.findViewById(R.id.btn_report_found);
        Button btnViewAll = view.findViewById(R.id.btn_view_all);
        Button btnShowOnMap = view.findViewById(R.id.btn_show_on_map);

        btnLost.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("postType", "Lost");
            navController.navigate(R.id.action_home_to_create, args);
        });

        btnFound.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("postType", "Found");
            navController.navigate(R.id.action_home_to_create, args);
        });

        btnViewAll.setOnClickListener(v -> navController.navigate(R.id.action_home_to_list));

        btnShowOnMap.setOnClickListener(v -> navController.navigate(R.id.action_home_to_map));
    }
}
