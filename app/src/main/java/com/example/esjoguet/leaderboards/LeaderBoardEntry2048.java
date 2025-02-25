package com.example.esjoguet.leaderboards;

public class LeaderBoardEntry2048 {
    private String username;
    private int moves;
    private String time;
    private int score;

    public LeaderBoardEntry2048(String username, int moves, String time, int score) {
        this.username = username;
        this.moves = moves;
        this.time = time;
        this.score = score;
    }

    public String getUsername() { return username; }
    public int getMoves() { return moves; }
    public String getTime() { return time; }
    public int getScore() { return score; }
}
