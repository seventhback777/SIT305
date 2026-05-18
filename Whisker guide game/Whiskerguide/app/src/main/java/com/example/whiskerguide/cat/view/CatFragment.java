package com.example.whiskerguide.cat.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whiskerguide.R;
import com.example.whiskerguide.WhiskerGuideApp;
import com.example.whiskerguide.cat.viewmodel.CatViewModel;

public class CatFragment extends Fragment {

    private RecyclerView chatList;
    private ChatMessageAdapter adapter;
    private CatViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CatViewModel.class);

        chatList = view.findViewById(R.id.chat_list);
        adapter = new ChatMessageAdapter();
        chatList.setLayoutManager(new LinearLayoutManager(requireContext()));
        chatList.setAdapter(adapter);

        EditText edit = view.findViewById(R.id.edit_question);
        Button send = view.findViewById(R.id.btn_send);
        Button back = view.findViewById(R.id.btn_back);
        Button engineToggle = view.findViewById(R.id.btn_engine_toggle);

        send.setOnClickListener(v -> {
            String q = edit.getText().toString();
            if (q.trim().isEmpty()) return;
            viewModel.ask(q);
            edit.setText("");
        });

        back.setOnClickListener(v -> goBack());

        // Android 13+ 预测性返回 / Android 16 强制启用兼容:
        // 注册 OnBackPressedCallback,统一处理物理返回键、手势返回、按钮返回。
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goBack();
                    }
                });

        // 引擎切换:Mock ↔ MediaPipe。MediaPipe 未就绪时按钮 disable。
        WhiskerGuideApp app = WhiskerGuideApp.get();
        refreshEngineButton(engineToggle);
        engineToggle.setOnClickListener(v -> {
            if (!isAdded()) return;
            if (!app.isMediaPipeReady()) {
                Toast.makeText(requireContext(),
                        "AI engine still loading, please wait", Toast.LENGTH_SHORT).show();
                return;
            }
            app.setPreferMock(!app.isPreferMock());
            refreshEngineButton(engineToggle);
            Toast.makeText(requireContext(),
                    app.isPreferMock() ? "Switched to Mock engine" : "Switched to on-device AI (Gemma)",
                    Toast.LENGTH_SHORT).show();
        });

        viewModel.getMessages().observe(getViewLifecycleOwner(), msgs -> {
            adapter.submit(msgs);
            if (!msgs.isEmpty()) chatList.scrollToPosition(msgs.size() - 1);
        });

        viewModel.getThinking().observe(getViewLifecycleOwner(), thinking -> {
            boolean busy = Boolean.TRUE.equals(thinking);
            send.setEnabled(!busy);
            engineToggle.setEnabled(!busy);
            if (!busy) refreshEngineButton(engineToggle);
        });
    }

    private void goBack() {
        if (!isAdded()) return;
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void refreshEngineButton(Button btn) {
        WhiskerGuideApp app = WhiskerGuideApp.get();
        boolean ready = app.isMediaPipeReady();
        boolean finished = app.isMediaPipeInitFinished();

        if (app.isPreferMock()) {
            if (!finished) {
                btn.setText("Engine: Mock (AI loading)");
            } else if (ready) {
                btn.setText("Engine: Mock");
            } else {
                // 加载失败,只能用 Mock
                btn.setText("Engine: Mock (AI unavailable)");
            }
        } else {
            btn.setText(ready ? "Engine: AI" : "Engine: AI (loading...)");
        }
    }
}
