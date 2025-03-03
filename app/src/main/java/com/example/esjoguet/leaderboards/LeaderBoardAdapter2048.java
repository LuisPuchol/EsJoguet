package com.example.esjoguet.leaderboards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.esjoguet.R;

import java.util.List;

import androidx.annotation.NonNull;

public class LeaderBoardAdapter2048 extends RecyclerView.Adapter<LeaderBoardAdapter2048.ViewHolder> {
    private List<LeaderBoardEntry2048> leaderboardEntries;

    public LeaderBoardAdapter2048(List<LeaderBoardEntry2048> leaderboardEntries) {
        this.leaderboardEntries = leaderboardEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_2048, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderBoardEntry2048 entry = leaderboardEntries.get(position);

        holder.tvUsername.setText(entry.getUsername());
        holder.tvMoves.setText(String.valueOf(entry.getMoves()));
        holder.tvTime.setText(entry.getTime());
        holder.tvScore.setText(String.valueOf(entry.getScore()));

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
        TextView tvUsername, tvMoves, tvTime, tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.username);
            tvMoves = itemView.findViewById(R.id.moves);
            tvTime = itemView.findViewById(R.id.time);
            tvScore = itemView.findViewById(R.id.score);
        }
    }
}