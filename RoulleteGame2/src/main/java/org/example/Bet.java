package org.example;

record Bet(String betType, double betAmount) {

    public String getBetType() {
        return betType;
    }

    public double getBetAmount() {
        return betAmount;
    }
}
