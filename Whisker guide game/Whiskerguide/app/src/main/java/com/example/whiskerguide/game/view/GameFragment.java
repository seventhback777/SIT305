package com.example.whiskerguide.game.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.whiskerguide.R;
import com.example.whiskerguide.cat.view.CatFragment;
import com.example.whiskerguide.game.viewmodel.GameViewModel;

public class GameFragment extends Fragment {

    private GameView gameView;
    private TextView hpLabel, mpLabel;
    private ProgressBar hpBar, mpBar;
    private LinearLayout combatPanel;
    private GameViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);

        gameView = view.findViewById(R.id.game_view);
        hpLabel = view.findViewById(R.id.hp_label);
        mpLabel = view.findViewById(R.id.mp_label);
        hpBar = view.findViewById(R.id.hp_bar);
        mpBar = view.findViewById(R.id.mp_bar);
        combatPanel = view.findViewById(R.id.combat_panel);

        gameView.setMap(viewModel.getMapData());

        Button up = view.findViewById(R.id.btn_up);
        Button down = view.findViewById(R.id.btn_down);
        Button left = view.findViewById(R.id.btn_left);
        Button right = view.findViewById(R.id.btn_right);
        up.setOnClickListener(v -> viewModel.moveUp());
        down.setOnClickListener(v -> viewModel.moveDown());
        left.setOnClickListener(v -> viewModel.moveLeft());
        right.setOnClickListener(v -> viewModel.moveRight());

        Button basic = view.findViewById(R.id.btn_basic_attack);
        Button fireball = view.findViewById(R.id.btn_fireball);
        Button potion = view.findViewById(R.id.btn_potion);
        basic.setOnClickListener(v -> viewModel.basicAttack());
        fireball.setOnClickListener(v -> viewModel.castFireball());
        potion.setOnClickListener(v -> viewModel.usePotion());

        Button askCat = view.findViewById(R.id.btn_ask_cat);
        askCat.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CatFragment())
                        .addToBackStack(null)
                        .commit());

        viewModel.getPlayer().observe(getViewLifecycleOwner(), player -> {
            gameView.setPlayerPosition(player.getX(), player.getY());
            hpBar.setMax(player.getMaxHealth());
            hpBar.setProgress(player.getHealth());
            hpLabel.setText("HP " + player.getHealth() + "/" + player.getMaxHealth());
            mpBar.setMax(player.getMaxMana());
            mpBar.setProgress(player.getMana());
            mpLabel.setText("MP " + player.getMana() + "/" + player.getMaxMana());
        });

        viewModel.getEnemy().observe(getViewLifecycleOwner(), enemy -> {
            boolean inCombat = enemy != null;
            combatPanel.setVisibility(inCombat ? View.VISIBLE : View.GONE);
            gameView.setEnemyVisible(inCombat);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
