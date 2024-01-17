package org.example;

public class PlayerStatistics {
    private final String playerName;
    private final int roundsPlayed;
    private final int roundsWon;
    private final int roundsLost;
    private final double winPercentage;

    public PlayerStatistics(String playerName, int roundsPlayed, int roundsWon, int roundsLost, double winPercentage) {
        this.playerName = playerName;
        this.roundsPlayed = roundsPlayed;
        this.roundsWon = roundsWon;
        this.roundsLost = roundsLost;
        this.winPercentage = winPercentage;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public int getRoundsWon() {
        return roundsWon;
    }

    public int getRoundsLost() {
        return roundsLost;
    }

    public double getWinPercentage() {
        return winPercentage;
    }
}
