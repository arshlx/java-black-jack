import java.io.PrintStream;
import java.util.InputMismatchException;
import java.util.Scanner;

public class DriverClass {
    private static final int MIN = 1, MAX = 10000;
    private static final PrintStream syso = System.out;
    private static final Scanner scan = new Scanner(System.in);
    private static final BlackJackViewModel viewModel = new BlackJackViewModel();

    public static void main(String[] args) {
        syso.println("""
                -------------------------------------------------------
                Welcome to BLACKJACK by Arshdeep Singh!
                -------------------------------------------------------
                Bet and make your card values closer to 21, but not beyond.
                HIT to add another card.
                STAY to stop taking more cards.
                SURRENDER only allowed in the first decision.
                If your card values reach exactly 21, you BLACKJACK!
                -------------------------------------------------------
                Beat the DEALER and enjoy your time!
                -------------------------------------------------------""");

        initPlayers();
        viewModel.initDealer();
        syso.println("Lets place the bets!");
        initBets();
        showTable(false);
        selectMoves();
        viewModel.resetStack();
        playAgain();
    }

    private static void showTable(boolean revealDealerCards) {
        syso.println("\n\tDealer cards");
        if (revealDealerCards) {
            viewModel.getDealer().getCards().forEach(Card::getCardDisplay);
        } else viewModel.getDealer().getCards().get(0).getCardDisplay();
        syso.println("\n\t-----------------");
        syso.println("\n\tPlayer cards");
        viewModel.getPlayers().forEach(player -> {
            syso.println("\t" + player.getPlayerName() + "'s cards");
            player.getCards().forEach(Card::getCardDisplay);
            syso.println("\n\t-----------------");
        });
    }

    private static void initPlayers() {
        while (true) {
            syso.println("How many players do you want to add? (maximum 5 players. Press 0 to exit)");
            try {
                var numPlayers = scan.nextInt();
                if (viewModel.getNumPlayers() < 0) {
                    syso.println("Players cannot be negative.");
                } else if (numPlayers == 0) System.exit(0);
                else if (numPlayers > 5)
                    syso.println("Maximum number of players allowed is 5. Please choose between 1 to 5 players.");
                else {
                    viewModel.setNumPlayers(numPlayers);
                    break;
                }
            } catch (InputMismatchException e) {
                scan.nextLine();
                syso.println("Invalid input, please try again.");
            }
        }

        for (int index = 0; index < viewModel.getNumPlayers(); index++) {
            viewModel.getPlayers().add(initPlayerName());
        }
    }

    private static Player initPlayerName() {
        var playerName = "";
        var playerNumber = 0;
        if (viewModel.getPlayers().size() < 2) playerNumber = viewModel.getPlayers().size() + 1;
        else playerNumber = viewModel.getPlayers().size();
        while (true) {
            syso.println("What is the name of player " + playerNumber);
            playerName = scan.next();
            if (playerName.isEmpty()) {
                syso.println("Player name cannot be empty");
            } else break;
        }
        return createPlayer(playerName);
    }

    private static Player createPlayer(String name) {
        var startingMoney = 0;
        while (true) {
            syso.println(name + " please enter starting amount: ");
            try {
                startingMoney = scan.nextInt();
                if (startingMoney == 0) {
                    syso.println(name + ", you cannot enter the game with zero money.");
                } else if (startingMoney < 0) {
                    syso.println(name + ", you cannot enter the game with no money.");
                } else {
                    syso.println("Player created successfully.");
                    break;
                }
            } catch (InputMismatchException e) {
                scan.nextLine();
                syso.println("Invalid input, please try again.");
            }
        }
        return new Player(name, startingMoney, viewModel.throwTwoCards());
    }

    private static void initBets() {
        for (int index = 0; index < viewModel.getNumPlayers(); index++) {
            var player = viewModel.getPlayers().get(index);
            while (true) {
                syso.println(player.getPlayerName() + ", how much do you want to bet?");
                try {
                    var bet = scan.nextInt();
                    if (bet < MIN) {
                        syso.println(player.getPlayerName() + ", the bet needs to be at least $1");
                    } else if (bet > MAX) {
                        syso.println(player.getPlayerName() + ", the bet cannot be greater than $10,000");
                    } else if (bet > player.getMoney()) {
                        syso.println(player.getPlayerName() + ", you cannot bet more than the amount that you have in your account.");
                    } else {
                        player.setBet(bet);
                        break;
                    }

                } catch (InputMismatchException e) {
                    scan.nextLine();
                    syso.println("Invalid input, please try again.");
                }
            }
        }
    }

    private static void selectMoves() {
        for (int index = 0; index < viewModel.getPlayers().size(); index++) {
            var player = viewModel.getPlayers().get(index);
            if (player.getState() == PlayerState.HIT) {
                var exitLoop = false;
                while (!exitLoop) {
                    if (player.getFirstMove())
                        syso.println(player.getPlayerName() + ", What's your move?\n0 to stay\n1 to hit\n2 to surrender");
                    else syso.println(player.getPlayerName() + ", What's your move?\n0 to stay\n1 to hit");
                    try {
                        var move = scan.nextInt();
                        switch (move) {
                            case 0 -> {
                                player.setState(PlayerState.STAY);
                                player.setFirstMove(false);
                                exitLoop = true;
                            }
                            case 1 -> {
                                player.setState(PlayerState.HIT);
                                player.setFirstMove(false);
                                viewModel.hit(index);
                                showTable(false);
                                var hitAgain = true;
                                while (hitAgain && player.getState() != PlayerState.BUST && player.getState() != PlayerState.BLACKJACK) {
                                    syso.println(player.getPlayerName() + ", do you want to hit again?(y/n)");
                                    var response = scan.next();
                                    switch (response.toLowerCase().charAt(0)) {
                                        case 'y' -> {
                                            viewModel.hit(index);
                                            showTable(false);
                                        }
                                        case 'n' -> {
                                            hitAgain = false;
                                            player.setState(PlayerState.STAY);
                                        }
                                        default -> syso.println("Invalid input. Please try again");
                                    }
                                }
                                exitLoop = true;
                            }
                            case 2 -> {
                                if (player.getFirstMove()) {
                                    syso.println(player.getPlayerName() + " has surrendered.");
                                    player.setState(PlayerState.SURRENDER);
                                } else syso.println(player.getPlayerName() + ", you cannot surrender.");
                                exitLoop = true;
                            }
                            default -> syso.println("Invalid entry, please try again.");
                        }
                    } catch (InputMismatchException e) {
                        scan.nextLine();
                        syso.println("Invalid input, please try again.");
                    }
                }
            }
        }
        viewModel.dealerHit();
        showTable(true);
        displayResults();
    }

    private static void displayResults() {
        viewModel.getPlayers().forEach(player -> {
            switch (player.getState()) {
                case WON ->
                        syso.println(player.getPlayerName() + ", you won! Your winnings: $" + player.getBet() + "\nYour total money is now $" + player.getMoney());
                case LOST ->
                        syso.println(player.getPlayerName() + ", you lost! Total amount lost: $" + player.getBet() + "\nYour total money is now $" + player.getMoney());
                case BUST ->
                        syso.println(player.getPlayerName() + ", you went bust! Total amount lost: $" + player.getBet() + "\nYour total money is now $" + player.getMoney());
                case PUSH ->
                        syso.println(player.getPlayerName() + ", It was a push!" + "\nYour total money remains the same $" + player.getMoney());
                case BLACKJACK ->
                        syso.println(player.getPlayerName() + ", you won! You got a blackjack! your winnings: $" + (1.5 * player.getBet()) + "\nYour total money is now $" + player.getMoney());
                case SURRENDER ->
                        syso.println(player.getPlayerName() + ", you surrendered! Total amount lost: $" + (0.5 * player.getBet()) + "\nYour total money is now $" + player.getMoney());
            }
            player.setState(PlayerState.HIT);
            player.setFirstMove(true);
            player.getCards().clear();
            player.setBet(0);
        });
        if (viewModel.getDealer().getState() == PlayerState.BUST) {
            syso.println("\nDealer went bust!\n");
        }
        viewModel.getDealer().getCards().clear();
    }

    private static void playAgain() {
        while (true) {
            syso.println("Do you want to play again? (y/n)");
            var response = scan.next();
            switch (response.toLowerCase().charAt(0)) {
                case 'y' -> {
                    checkMoney();
                    syso.println("Lets place the bets!");
                    initBets();
                    viewModel.getPlayers().forEach(player -> player.getCards().addAll(viewModel.throwTwoCards()));
                    viewModel.initDealer();
                    showTable(false);
                    selectMoves();
                    viewModel.resetStack();
                }
                case 'n' -> System.exit(0);
                default -> syso.println("Invalid input. Please try again");
            }
        }
    }

    private static void checkMoney() {
        viewModel.getPlayers().forEach(player -> {
            if (player.getMoney() < 1) {
                while (true) {
                    syso.println(player.getPlayerName() + ", your current funds are: $" + player.getMoney() + "\nPlease add money to continue: $");
                    try {
                        var money = scan.nextInt();
                        if (money == 0) {
                            syso.println("Funds cannot be $0");
                        } else {
                            var playerMoney = player.getMoney() + money;
                            player.setMoney(playerMoney);
                            if (player.getMoney() < 1) syso.println("Your funds are not enough to continue.");
                            else break;
                        }
                    } catch (InputMismatchException e) {
                        scan.nextLine();
                        syso.println("Invalid input, please try again.");
                    }
                }
            }
        });
        if (viewModel.getPlayers().isEmpty()) {
            syso.println("No players left in the game. Please add at least 1 player to continue.");
            initPlayers();
        }
    }
}
