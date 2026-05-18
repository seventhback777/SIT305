package com.example.sportsapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportsapp.R;
import com.example.sportsapp.adapter.NewsAdapter;
import com.example.sportsapp.model.NewsItem;
import com.example.sportsapp.viewmodel.BookmarkViewModel;
import com.example.sportsapp.viewmodel.NewsViewModel;

public class DetailFragment extends Fragment {

    private NewsViewModel newsViewModel;
    private BookmarkViewModel bookmarkViewModel;
    private boolean currentlyBookmarked = false;
    private ImageButton btnBookmark;
    private ImageButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack = view.findViewById(R.id.btn_back);
        btnBookmark = view.findViewById(R.id.btn_bookmark);

        // Push buttons below the status bar dynamically.
        // This works regardless of whether edge-to-edge is fully resolved or not.
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int statusBarHeight = systemBars.top;
            int marginDp = (int) (12 * getResources().getDisplayMetrics().density);

            FrameLayout.LayoutParams backParams =
                    (FrameLayout.LayoutParams) btnBack.getLayoutParams();
            backParams.topMargin = statusBarHeight + marginDp;
            btnBack.setLayoutParams(backParams);

            FrameLayout.LayoutParams bookmarkParams =
                    (FrameLayout.LayoutParams) btnBookmark.getLayoutParams();
            bookmarkParams.topMargin = statusBarHeight + marginDp;
            btnBookmark.setLayoutParams(bookmarkParams);

            return WindowInsetsCompat.CONSUMED;
        });

        newsViewModel = new ViewModelProvider(requireActivity()).get(NewsViewModel.class);
        bookmarkViewModel = new ViewModelProvider(requireActivity()).get(BookmarkViewModel.class);

        Bundle args = getArguments();
        if (args == null) return;
        int newsId = args.getInt("newsId", -1);
        NewsItem item = newsViewModel.getNewsById(newsId);
        if (item == null) return;

        bindNewsItem(view, item);
        setupBackButton();
        setupBookmarkButton(item);
        setupRelatedNews(view, item);
    }

    private void bindNewsItem(View view, NewsItem item) {
        ((ImageView) view.findViewById(R.id.img_detail)).setImageResource(item.getImageResId());
        ((TextView) view.findViewById(R.id.tv_detail_category)).setText(item.getCategory());
        ((TextView) view.findViewById(R.id.tv_detail_title)).setText(item.getTitle());
        ((TextView) view.findViewById(R.id.tv_detail_date)).setText(item.getDate());
        ((TextView) view.findViewById(R.id.tv_detail_description)).setText(item.getDescription());
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            NavController navController =
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigateUp();
        });
    }

    private void setupBookmarkButton(NewsItem item) {
        bookmarkViewModel.isBookmarked(item.getId()).observe(getViewLifecycleOwner(), isBookmarked -> {
            currentlyBookmarked = isBookmarked;
            updateBookmarkIcon();
        });

        btnBookmark.setOnClickListener(v -> {
            if (currentlyBookmarked) {
                bookmarkViewModel.removeBookmark(item.getId());
            } else {
                bookmarkViewModel.addBookmark(item.getId());
            }
            currentlyBookmarked = !currentlyBookmarked;
            updateBookmarkIcon();
        });
    }

    private void updateBookmarkIcon() {
        if (btnBookmark == null || getContext() == null) return;
        btnBookmark.setImageDrawable(ContextCompat.getDrawable(requireContext(),
                currentlyBookmarked
                        ? R.drawable.ic_bookmark_filled
                        : R.drawable.ic_bookmark_outline));
    }

    private void setupRelatedNews(View view, NewsItem item) {
        RecyclerView rvRelated = view.findViewById(R.id.rv_related);
        rvRelated.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRelated.setNestedScrollingEnabled(false);

        NewsAdapter relatedAdapter = new NewsAdapter(relatedItem -> {
            Bundle args = new Bundle();
            args.putInt("newsId", relatedItem.getId());
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                    .navigate(R.id.detailFragment, args);
        });
        rvRelated.setAdapter(relatedAdapter);
        relatedAdapter.submitList(
                newsViewModel.getRelatedNews(item.getId(), item.getCategory()));
    }
}
