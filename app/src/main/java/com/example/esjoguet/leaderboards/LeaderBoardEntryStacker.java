package com.example.esjoguet.leaderboards;

public class LeaderBoardEntryStacker {
    private String username;
    private int tries;
    private int smallPrizes;
    private int bigPrizes;

    public LeaderBoardEntryStacker(String username, int tries, int smallPrizes, int bigPrizes) {
        this.username = username;
        this.tries = tries;
        this.smallPrizes = smallPrizes;
        this.bigPrizes = bigPrizes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTries() {
        return tries;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }

    public int getSmallPrizes() {
        return smallPrizes;
    }

    public void setSmallPrizes(int smallPrizes) {
        this.smallPrizes = smallPrizes;
    }

    public int getBigPrizes() {
        return bigPrizes;
    }

    public void setBigPrizes(int bigPrizes) {
        this.bigPrizes = bigPrizes;
    }
}

