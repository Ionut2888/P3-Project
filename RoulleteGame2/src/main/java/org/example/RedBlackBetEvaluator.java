package org.example;

class RedBlackBetEvaluator implements BetEvaluator {
    private final String color;

    public RedBlackBetEvaluator(String color) {
        this.color = color;
    }

    @Override
    public boolean determineWin(int result) {
        String winningColor = determineColor(result);
        return color.equalsIgnoreCase(winningColor);
    }

    private String determineColor(int number) {
        if (number == 0) {
            return "Green";
        } else if ((number >= 1 && number <= 10) || (number >= 19 && number <= 28)) {
            // In ranges 1-10 and 19-28, odd numbers are red, and even are black
            return (number % 2 == 1) ? "Red" : "Black";
        } else {
            // In ranges 11-18 and 29-36, odd numbers are black, and even are red
            return (number % 2 == 1) ? "Black" : "Red";
        }
    }

}
