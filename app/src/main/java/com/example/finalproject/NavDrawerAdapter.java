package com.example.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NavDrawerAdapter extends RecyclerView.Adapter<NavDrawerAdapter.ViewHolder> {

    private List<NavItem> navItems;
    private OnItemClickListener onItemClickListener;

    public NavDrawerAdapter(List<NavItem> navItems, OnItemClickListener onItemClickListener) {
        this.navItems = navItems;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nav_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NavItem navItem = navItems.get(position);
        holder.icon.setImageResource(navItem.getIcon());
        holder.text.setText(navItem.getText());
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(navItem));
    }

    @Override
    public int getItemCount() {
        return navItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(NavItem navItem);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView text;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.nav_item_icon);
            text = itemView.findViewById(R.id.nav_item_text);
        }
    }
}
