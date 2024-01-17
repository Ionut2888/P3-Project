package org.example;

import java.io.IOException;

interface GameInterface {
    void startGame() throws IOException, InvalidBetException, InsufficientFundsException, GameConfigurationException;
}
