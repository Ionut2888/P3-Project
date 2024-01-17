package org.example;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.*;

public class RouletteGame implements GameInterface {
    private static final String CONFIG_FILE = "config.properties";
    private static final String STATS_FILE = "player_stats.txt";
    private static Player player;

    public static void main(String[] args) {
        RouletteGame game = new RouletteGame();
        game.startGame();
    }

    @Override
    public void startGame() {
        try {
            Properties config = loadConfiguration();

            Scanner scanner = new Scanner(System.in);

            player = new Player("Alex", Double.parseDouble(config.getProperty("initialBalance")));
            RouletteTable table = new RouletteTable();

            while (player.getWallet() > 0) {
                player.incrementRoundsPlayed();

                System.out.print("Enter the type of bet (Red/Black/Number) or 'quit' to exit: ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("quit")) {
                    break;
                }

                System.out.print("Enter the bet amount: ");
                double betAmount = scanner.nextDouble();
                scanner.nextLine();

                validateInput(input, betAmount);

                Bet redBet = new Bet(input, betAmount);
                player.addBet(redBet);

                // Spin the wheel using threads
                Thread spinThread = new Thread(() -> {
                    int result = table.spinWheel();
                    displayResults(result);
                });

                spinThread.start();
                spinThread.join();  // Wait for the spin to finish

                savePlayerStats(player);
            }

            System.out.println("Game Over. Your balance is zero or you chose to quit.");
            displayPlayerData(player.getPlayerName());
        } catch (IOException | InvalidBetException | InsufficientFundsException | GameConfigurationException | InterruptedException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static void displayResults(int result) {
        System.out.println("The winning number is: " + result);

        String winningColor = determineColor(result);
        System.out.println("The winning color is: " + winningColor);

        BetEvaluator redBlackEvaluator = new RedBlackBetEvaluator(player.getLatestBet().getBetType());
        boolean redBlackWin = redBlackEvaluator.determineWin(result);

        BetEvaluator numberEvaluator = new NumberBetEvaluator(player.getLatestBet().getBetAmount());
        boolean numberWin = numberEvaluator.determineWin(result);

        player.setWallet(player.getWallet() - player.getLatestBet().getBetAmount());
        int wins=0;
        if (redBlackWin) {
            player.setWallet(player.getWallet() + player.getLatestBet().getBetAmount() * 2);
            player.incrementRoundsWon();
            System.out.println("Red/Black bet wins!");
            wins++;
        } else {
            System.out.println("Red/Black bet loses.");
        }

        if (numberWin) {
            player.setWallet(player.getWallet() + player.getLatestBet().getBetAmount() * 36);
            if(wins == 0) {
                player.incrementRoundsWon();
                wins++;
            }
            System.out.println("Number bet wins!");
        } else {
            System.out.println("Number bet loses.");
        }
        if(wins ==0)
            player.incrementRoundsLost();

        displayResults();
    }

    private static String determineColor(int number) {
        if (number == 0) {
            return "Green";
        } else if ((number >= 1 && number <= 10) || (number >= 19 && number <= 28)) {
            return "Red";
        } else {
            return "Black";
        }
    }

    private static Properties loadConfiguration() throws IOException, GameConfigurationException {
        Properties config = new Properties();

        try (InputStream input = new FileInputStream(RouletteGame.CONFIG_FILE)) {
            config.load(input);
        } catch (FileNotFoundException e) {
            throw new GameConfigurationException("Configuration file not found: " + RouletteGame.CONFIG_FILE);
        }

        return config;
    }

    private static void validateInput(String betType, double betAmount) throws InvalidBetException, InsufficientFundsException {
        List<String> validBetTypes = Arrays.asList("Red", "Black", "0", "32", "15", "19", "4", "21", "2", "25", "17", "34", "6", "27", "13", "36", "11", "30", "8", "23", "10", "5", "24", "16", "33", "1", "20", "14", "31", "9", "22", "18", "29", "7", "28", "12", "35", "3", "26"
        );

        if (player == null) {
            throw new NullPointerException("Player is null. Cannot access wallet information.");
        }

        if (!validBetTypes.contains(betType)) {
            throw new InvalidBetException("Invalid bet type. Valid types are: Red, Black, Number");
        }

        if (betAmount <= 0) {
            throw new InvalidBetException("Invalid bet amount. Please enter a positive amount.");
        }

        if (betAmount > player.getWallet()) {
            throw new InsufficientFundsException("Insufficient funds. Please place a smaller bet.");
        }
    }

    private static void savePlayerStats(Player player) throws IOException {
        PlayerStatistics playerStats = new PlayerStatistics(
                player.getPlayerName(),
                player.getRoundsPlayed(),
                player.getRoundsWon(),
                player.getRoundsLost(),
                calculateWinPercentage(player)
        );

        insertOrUpdatePlayerStatistics(playerStats);
    }

    public static void insertOrUpdatePlayerStatistics(PlayerStatistics playerStats) {
        try (Connection connection = DatabaseConnection.connect()) {
            if (playerExists(connection, playerStats.getPlayerName())) {
                updatePlayerStatistics(connection, playerStats);
            } else {
                insertPlayerStatistics(connection, playerStats);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean playerExists(Connection connection, String playerName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM player_statistics WHERE player_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    private static void updatePlayerStatistics(Connection connection, PlayerStatistics playerStats) throws SQLException {
        if (isValidPlayerStatistics(playerStats)) {
            System.out.println("Invalid player statistics. Data not updated.");
            return;
        }
        //updating player statistics
        String sql = "UPDATE player_statistics " +
                "SET rounds_played = rounds_played + ?, " +
                "    rounds_won = rounds_won + ?, " +
                "    rounds_lost = rounds_lost + ?, " +
                "    win_percentage = (rounds_won + ?) / (rounds_played + ?) * 100 " +
                "WHERE player_name = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, playerStats.getRoundsPlayed());
            statement.setInt(2, playerStats.getRoundsWon());
            statement.setInt(3, playerStats.getRoundsLost());
            statement.setInt(4, playerStats.getRoundsWon());
            statement.setInt(5, playerStats.getRoundsPlayed());
            statement.setString(6, playerStats.getPlayerName());

            statement.executeUpdate();
        }

    }

    private static void insertPlayerStatistics(Connection connection, PlayerStatistics playerStats) throws SQLException {
        //validate input
        if (isValidPlayerStatistics(playerStats)) {
            System.out.println("Invalid player statistics. Data not inserted.");
            return;
        }

        //SQL inserting player statistics
        String sql = "INSERT INTO player_statistics (player_name, rounds_played, rounds_won, rounds_lost, win_percentage) " +
                "VALUES (?, ?, ?, ?, ?)";

        //SQL query
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerStats.getPlayerName());
            statement.setInt(2, playerStats.getRoundsPlayed());
            statement.setInt(3, playerStats.getRoundsWon());
            statement.setInt(4, playerStats.getRoundsLost());
            statement.setDouble(5, playerStats.getWinPercentage());

            statement.executeUpdate();
        }
    }

    private static boolean isValidPlayerStatistics(PlayerStatistics playerStats) {
        return isValidPlayerName(playerStats.getPlayerName()) &&
                isValidRounds(playerStats.getRoundsPlayed()) &&
                isValidRounds(playerStats.getRoundsWon()) &&
                isValidRounds(playerStats.getRoundsLost()) &&
                isValidWinPercentage(playerStats.getWinPercentage());
    }

    private static boolean isValidPlayerName(String playerName) {
        return !playerName.isEmpty();
    }

    private static boolean isValidRounds(int rounds) {
        return rounds >= 1;
    }

    private static boolean isValidWinPercentage(double winPercentage) {
        return winPercentage >= 0 && winPercentage <= 100;
    }

    private static double calculateWinPercentage(Player player) {
        int roundsPlayed = player.getRoundsPlayed();
        int roundsWon = player.getRoundsWon();

        if (roundsPlayed == 0) {
            return 0.0; //division by zero
        }

        return ((double) roundsWon / roundsPlayed) * 100;
    }

    private static void displayResults() {
        System.out.println("Remaining wallet balance for " + player.getPlayerName() + ": $" + player.getWallet());
    }

    private static void displayPlayerData(String playerName) {
        try (Connection connection = DatabaseConnection.connect()) {
            if ("admin".equalsIgnoreCase(playerName)) {
                displayAllPlayerData(connection);
            } else {
                displaySinglePlayerData(connection, playerName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void displayAllPlayerData(Connection connection) throws SQLException {
        String sql = "SELECT * FROM player_statistics";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                displayPlayerData(resultSet);
            }
        }
    }

    private static void displaySinglePlayerData(Connection connection, String playerName) throws SQLException {
        if (!playerExists(connection, playerName)) {
            System.out.println("Player not found: " + playerName);
            return;
        }

        String sql = "SELECT * FROM player_statistics WHERE player_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    displayPlayerData(resultSet);
                }
            }
        }
    }

    private static void displayPlayerData(ResultSet resultSet) throws SQLException {
        String playerName = resultSet.getString("player_name");
        int roundsPlayed = resultSet.getInt("rounds_played");
        int roundsWon = resultSet.getInt("rounds_won");
        int roundsLost = resultSet.getInt("rounds_lost");
        double winPercentage = resultSet.getDouble("win_percentage");

        System.out.println("Player Name: " + playerName);
        System.out.println("Rounds Played: " + roundsPlayed);
        System.out.println("Rounds Won: " + roundsWon);
        System.out.println("Rounds Lost: " + roundsLost);
        System.out.println("Win Percentage: " + winPercentage + "%\n");
    }

}
