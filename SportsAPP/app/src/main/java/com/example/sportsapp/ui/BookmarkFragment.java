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
import com.example.sportsapp.adapter.NewsAdapter;
import com.example.sportsapp.viewmodel.BookmarkViewModel;

public class BookmarkFragment extends Fragment {

    private BookmarkViewModel bookmarkViewModel;
    private NewsAdapter newsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bookmarkViewModel = new ViewModelProvider(requireActivity()).get(BookmarkViewModel.class);

        RecyclerView rvBookmarks = view.findViewById(R.id.rv_bookmarks);
        View tvEmpty = view.findViewById(R.id.tv_empty); // LinearLayout in XML, not TextView

        rvBookmarks.setLayoutManager(new LinearLayoutManager(requireContext()));
        newsAdapter = new NewsAdapter(item -> {
            Bundle args = new Bundle();
            args.putInt("newsId", item.getId());
            Navigation.findNavController(view).navigate(R.id.detailFragment, args);
        });
        rvBookmarks.setAdapter(newsAdapter);

        bookmarkViewModel.bookmarkedNews.observe(getViewLifecycleOwner(), newsList -> {
            newsAdapter.submitList(newsList);
            if (newsList.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvBookmarks.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvBookmarks.setVisibility(View.VISIBLE);
            }
        });
    }
}
