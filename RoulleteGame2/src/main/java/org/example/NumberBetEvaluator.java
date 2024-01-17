package org.example;

class NumberBetEvaluator implements BetEvaluator {
    private final double number;

    public NumberBetEvaluator(double number) {
        this.number = number;
    }

    @Override
    public boolean determineWin(int result) {
        return result == (int) number;
    }
}
