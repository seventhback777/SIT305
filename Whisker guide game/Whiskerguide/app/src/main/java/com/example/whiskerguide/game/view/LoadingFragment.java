package com.example.whiskerguide.game.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.whiskerguide.R;
import com.example.whiskerguide.WhiskerGuideApp;
import com.example.whiskerguide.cat.engine.InitCallback;

public class LoadingFragment extends Fragment {

    private TextView tip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tip = view.findViewById(R.id.tip);

        WhiskerGuideApp app = WhiskerGuideApp.get();
        app.tryInitMediaPipe(new InitCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(),
                        "On-device model loaded 🐱", Toast.LENGTH_SHORT).show();
                goToGame();
            }

            @Override
            public void onError(String error) {
                tip.setText("On-device model unavailable, using Mock engine\n(" + error + ")");
                tip.postDelayed(LoadingFragment.this::goToGame, 1500);
            }
        });
    }

    private void goToGame() {
        if (!isAdded()) return;
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new GameFragment())
                .commit();
    }
}
