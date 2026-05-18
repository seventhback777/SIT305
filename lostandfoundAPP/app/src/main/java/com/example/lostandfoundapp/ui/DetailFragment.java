package com.example.lostandfoundapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lostandfoundapp.R;
import com.example.lostandfoundapp.database.DatabaseHelper;
import com.example.lostandfoundapp.model.Post;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private int postId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        NavController navController = NavHostFragment.findNavController(this);

        if (getArguments() != null) {
            postId = getArguments().getInt("postId", -1);
        }

        Post post = dbHelper.getPostById(postId);
        if (post == null) {
            Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show();
            navController.navigateUp();
            return;
        }

        TextView tvPostType = view.findViewById(R.id.tv_post_type);
        TextView tvCategory = view.findViewById(R.id.tv_category);
        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvPhone = view.findViewById(R.id.tv_phone);
        TextView tvDescription = view.findViewById(R.id.tv_description);
        TextView tvDate = view.findViewById(R.id.tv_date);
        TextView tvLocation = view.findViewById(R.id.tv_location);
        TextView tvTimestamp = view.findViewById(R.id.tv_timestamp);
        Button btnBack = view.findViewById(R.id.btn_back);
        Button btnRemove = view.findViewById(R.id.btn_remove);

        btnBack.setOnClickListener(v -> navController.navigateUp());

        tvPostType.setText(post.getPostType());
        tvCategory.setText(post.getCategory());
        tvName.setText(post.getName());
        tvPhone.setText(post.getPhone());
        tvDescription.setText(post.getDescription());
        tvDate.setText(post.getDate());
        tvLocation.setText(post.getLocation());
        tvTimestamp.setText(post.getTimestamp());

        if ("Lost".equals(post.getPostType())) {
            tvPostType.setBackgroundResource(R.drawable.badge_lost);
        } else {
            tvPostType.setBackgroundResource(R.drawable.badge_found);
        }

        // Mini map: show marker if the post has coordinates, otherwise hide the map
        SupportMapFragment detailMap = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.detail_map);
        if (detailMap != null) {
            if (post.hasLocation()) {
                final Post p = post;
                detailMap.getMapAsync(map -> {
                    LatLng pos = new LatLng(p.getLatitude(), p.getLongitude());
                    float hue = "Lost".equalsIgnoreCase(p.getPostType())
                            ? BitmapDescriptorFactory.HUE_RED
                            : BitmapDescriptorFactory.HUE_GREEN;
                    map.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(p.getPostType() + ": " + p.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
                });
            } else {
                // Old post without coordinates -> hide the map view
                if (detailMap.getView() != null) {
                    detailMap.getView().setVisibility(View.GONE);
                }
            }
        }

        btnRemove.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Remove Post")
                        .setMessage("Are you sure you want to remove this post?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            dbHelper.deletePost(postId);
                            Toast.makeText(requireContext(), "Post removed",
                                    Toast.LENGTH_SHORT).show();
                            navController.navigateUp();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());
    }
}
