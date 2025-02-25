package com.example.esjoguet.leaderboards;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.example.esjoguet.R;
import java.util.List;

public class LeaderBoardAdapter2048 extends RecyclerView.Adapter<LeaderBoardAdapter2048.ViewHolder> {
    private List<LeaderBoardEntry2048> leaderboardEntries;

    public LeaderBoardAdapter2048(List<LeaderBoardEntry2048> leaderboardEntries) {
        this.leaderboardEntries = leaderboardEntries;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard_2048, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LeaderBoardEntry2048 entry = leaderboardEntries.get(position);
        holder.username.setText(entry.getUsername());
        holder.moves.setText("Moves: " + entry.getMoves());
        holder.time.setText("Time: " + entry.getTime());
        holder.score.setText("Score: " + entry.getScore());
    }

    @Override
    public int getItemCount() {
        return leaderboardEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, moves, time, score;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            moves = itemView.findViewById(R.id.moves);
            time = itemView.findViewById(R.id.time);
            score = itemView.findViewById(R.id.score);
        }
    }
}