package com.example.eventapp;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.*;

public class EventListFragment extends Fragment {

    private EventViewModel viewModel;
    private EventAdapter   adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        adapter   = new EventAdapter();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Observe LiveData — list updates automatically
        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            adapter.setEvents(events);
        });

        // Item click → Edit mode (pass eventId)
        adapter.setListener(event -> {
            Bundle args = new Bundle();
            args.putInt("eventId", event.getId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_eventListFragment_to_addEditEventFragment, args);
        });

        // FAB → Create mode (no args)
        view.findViewById(R.id.fab_add).setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_eventListFragment_to_addEditEventFragment)
        );

        // Swipe right → delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Event event = adapter.getEventAt(viewHolder.getAdapterPosition());
                viewModel.deleteEvent(event);
                Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);
    }
}