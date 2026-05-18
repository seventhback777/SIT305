package com.example.lostandfoundapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfoundapp.R;
import com.example.lostandfoundapp.adapter.PostAdapter;
import com.example.lostandfoundapp.database.DatabaseHelper;

public class ListFragment extends Fragment implements PostAdapter.OnPostClickListener {

    private DatabaseHelper dbHelper;
    private PostAdapter adapter;
    private Spinner spinnerFilter;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        navController = NavHostFragment.findNavController(this);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        spinnerFilter = view.findViewById(R.id.spinner_filter);
        Button btnBack = view.findViewById(R.id.btn_back);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PostAdapter(this);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> navController.navigateUp());

        String[] filterOptions = getResources().getStringArray(R.array.categories_with_all);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPosts(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (spinnerFilter != null && spinnerFilter.getSelectedItem() != null) {
            loadPosts(spinnerFilter.getSelectedItem().toString());
        }
    }

    private void loadPosts(String category) {
        if ("All".equals(category)) {
            adapter.setPosts(dbHelper.getAllPosts());
        } else {
            adapter.setPosts(dbHelper.getPostsByCategory(category));
        }
    }

    @Override
    public void onPostClick(int postId) {
        Bundle args = new Bundle();
        args.putInt("postId", postId);
        navController.navigate(R.id.action_list_to_detail, args);
    }
}
