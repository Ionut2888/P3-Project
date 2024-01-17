package org.example;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String playerName;
    private double wallet;
    private int roundsPlayed;
    private int roundsWon;
    private int roundsLost;
    private List<Bet> bets;
    private Bet latestBet;

    public Player(String playerName, double wallet) {
        this.playerName = playerName;
        this.wallet = wallet;
        this.roundsPlayed = 0;
        this.roundsWon = 0;
        this.roundsLost = 0;
        this.bets = new ArrayList<>();
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getWallet() {
        return wallet;
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

    public List<Bet> getBets() {
        return bets;
    }

    public void addBet(Bet bet) {
        bets.add(bet);
        latestBet = bet;
    }

    public Bet getLatestBet() {
        return latestBet;
    }

    public void incrementRoundsPlayed() {
        roundsPlayed++;
    }

    public void incrementRoundsWon() {
        roundsWon++;
    }

    public void incrementRoundsLost() {
        roundsLost++;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }
}
