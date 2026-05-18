package com.example.videoapp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.videoapp.R;
import com.example.videoapp.data.SessionManager;
import com.example.videoapp.helper.YoutubeHelper;
import com.example.videoapp.viewmodel.VideoViewModel;

public class HomeFragment extends Fragment {

    private VideoViewModel videoViewModel;
    private SessionManager sessionManager;
    private WebView webView;
    private EditText etUrl;
    private String currentUrl = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoViewModel = new ViewModelProvider(requireActivity()).get(VideoViewModel.class);
        sessionManager = new SessionManager(requireContext());

        etUrl = view.findViewById(R.id.et_url);
        webView = view.findViewById(R.id.web_view);
        Button btnPlay = view.findViewById(R.id.btn_play);
        Button btnAddPlaylist = view.findViewById(R.id.btn_add_playlist);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        setupWebView();

        // Observe URL selected from PlaylistFragment — only fill the field, don't auto-play
        videoViewModel.selectedUrl.observe(getViewLifecycleOwner(), url -> {
            if (url != null && !url.isEmpty()) {
                etUrl.setText(url);
                videoViewModel.selectedUrl.setValue(null); // consume
            }
        });

        btnPlay.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a YouTube URL", Toast.LENGTH_SHORT).show();
                return;
            }
            String videoId = YoutubeHelper.extractVideoId(url);
            if (videoId == null) {
                Toast.makeText(requireContext(), "Invalid YouTube URL", Toast.LENGTH_SHORT).show();
                return;
            }
            currentUrl = url;
            webView.loadUrl(YoutubeHelper.buildWatchUrl(videoId));
        });

        btnAddPlaylist.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a URL first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (YoutubeHelper.extractVideoId(url) == null) {
                Toast.makeText(requireContext(), "Invalid YouTube URL", Toast.LENGTH_SHORT).show();
                return;
            }
            int userId = sessionManager.getUserId();
            videoViewModel.addVideo(url, userId);
            Toast.makeText(requireContext(), "Added to playlist", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> logout(view));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDomStorageEnabled(true);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        );
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
    }

    /** Called from PlaylistFragment when user taps an entry to load it here */
    public void loadUrl(String url) {
        if (etUrl != null) etUrl.setText(url);
        String videoId = YoutubeHelper.extractVideoId(url);
        if (videoId != null && webView != null) {
            webView.loadUrl(YoutubeHelper.buildWatchUrl(videoId));
        }
    }

    private void logout(View view) {
        sessionManager.logout();
        webView.loadUrl("about:blank");
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        Navigation.findNavController(view).navigate(R.id.loginFragment, null, navOptions);
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroyView();
    }
}
