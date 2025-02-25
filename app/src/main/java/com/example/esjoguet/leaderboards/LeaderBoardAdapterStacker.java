package com.example.esjoguet.leaderboards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.esjoguet.R;

import java.util.List;

import androidx.annotation.NonNull;


public class LeaderBoardAdapterStacker extends RecyclerView.Adapter<LeaderBoardAdapterStacker.ViewHolder> {

    private List<LeaderBoardEntryStacker> leaderboardEntries;

    public LeaderBoardAdapterStacker(List<LeaderBoardEntryStacker> leaderboardEntries) {
        this.leaderboardEntries = leaderboardEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_stacker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderBoardEntryStacker entry = leaderboardEntries.get(position);

        holder.tvUsername.setText(entry.getUsername());
        holder.tvTries.setText(String.valueOf(entry.getTries()));
        holder.tvSmallPrizes.setText(String.valueOf(entry.getSmallPrizes()));
        holder.tvBigPrizes.setText(String.valueOf(entry.getBigPrizes()));

        // Aplicar un color de fondo alternado para mejorar la legibilidad
        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(android.R.color.white);
        } else {
            holder.itemView.setBackgroundResource(R.color.lighter_gray);
        }
    }

    @Override
    public int getItemCount() {
        return leaderboardEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvTries, tvSmallPrizes, tvBigPrizes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTries = itemView.findViewById(R.id.tvTries);
            tvSmallPrizes = itemView.findViewById(R.id.tvSmallPrizes);
            tvBigPrizes = itemView.findViewById(R.id.tvBigPrizes);
        }
    }
}