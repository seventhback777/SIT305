package com.example.sportsapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportsapp.R;
import com.example.sportsapp.adapter.FeaturedAdapter;
import com.example.sportsapp.adapter.NewsAdapter;
import com.example.sportsapp.viewmodel.NewsViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class HomeFragment extends Fragment {

    private NewsViewModel newsViewModel;
    private FeaturedAdapter featuredAdapter;
    private NewsAdapter newsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newsViewModel = new ViewModelProvider(requireActivity()).get(NewsViewModel.class);

        setupFeaturedRecyclerView(view);
        setupNewsRecyclerView(view);
        setupCategoryChips(view);
        observeViewModel();
    }

    private void setupFeaturedRecyclerView(View view) {
        RecyclerView rvFeatured = view.findViewById(R.id.rv_featured);
        rvFeatured.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        featuredAdapter = new FeaturedAdapter(item -> {
            Bundle args = new Bundle();
            args.putInt("newsId", item.getId());
            Navigation.findNavController(view).navigate(R.id.detailFragment, args);
        });

        rvFeatured.setAdapter(featuredAdapter);
        featuredAdapter.submitList(newsViewModel.getFeaturedNews());
    }

    private void setupNewsRecyclerView(View view) {
        RecyclerView rvNews = view.findViewById(R.id.rv_news);
        rvNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNews.setNestedScrollingEnabled(false); // Let NestedScrollView handle scrolling

        newsAdapter = new NewsAdapter(item -> {
            Bundle args = new Bundle();
            args.putInt("newsId", item.getId());
            Navigation.findNavController(view).navigate(R.id.detailFragment, args);
        });

        rvNews.setAdapter(newsAdapter);
    }

    private void setupCategoryChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_categories);

        String[] categories = {"All", "Football", "Basketball", "Cricket", "Tennis"};
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_selector);
            chip.setTextColor(requireContext().getColorStateList(R.color.chip_text_selector));
            chipGroup.addView(chip);
        }

        // Select "All" chip by default
        ((Chip) chipGroup.getChildAt(0)).setChecked(true);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip selectedChip = group.findViewById(checkedIds.get(0));
            if (selectedChip != null) {
                newsViewModel.setCategory(selectedChip.getText().toString());
            }
        });
    }

    private void observeViewModel() {
        newsViewModel.getFilteredNews().observe(getViewLifecycleOwner(), newsList -> {
            newsAdapter.submitList(newsList);
        });
    }
}
