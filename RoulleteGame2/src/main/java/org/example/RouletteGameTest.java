package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouletteGameTest {

    @Test
    void testPlayerConstructor() {
        Player player = new Player("Alice", 1000.0);
        assertNotNull(player);
        assertEquals("Alice", player.getPlayerName());
        assertEquals(1000.0, player.getWallet());
        assertEquals(0, player.getRoundsPlayed());
        assertEquals(0, player.getRoundsWon());
        assertEquals(0, player.getRoundsLost());
        assertTrue(player.getBets().isEmpty());
    }

    @Test
    void testBetConstructor() {
        Bet bet = new Bet("Red", 10.0);
        assertNotNull(bet);
        assertEquals("Red", bet.getBetType());
        assertEquals(10.0, bet.getBetAmount());
    }

    @Test
    void testRouletteTableSpinWheel() {
        RouletteTable table = new RouletteTable();
        int result = table.spinWheel();
        assertTrue(result >= 0 && result <= 36);
    }

    @Test
    void testRedBlackBetEvaluator() {
        RedBlackBetEvaluator evaluator = new RedBlackBetEvaluator("Red");
        assertTrue(evaluator.determineWin(5));
        assertFalse(evaluator.determineWin(8));
    }

    @Test
    void testNumberBetEvaluator() {
        NumberBetEvaluator evaluator = new NumberBetEvaluator(15.0);
        assertTrue(evaluator.determineWin(15));
        assertFalse(evaluator.determineWin(20));
    }

}
