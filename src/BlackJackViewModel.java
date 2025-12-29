import java.util.ArrayList;

public class BlackJackViewModel {
    private final StackOfCards stackOfCards;
    private final int BLACKJACK = 21;
    private final ArrayList<Card> cardStack;
    private final ArrayList<Player> players = new ArrayList<>();
    private int nextCardIndex = 51;
    private final Dealer dealer = new Dealer();
    private int numPlayers = 0;

    public BlackJackViewModel() {
        stackOfCards = new StackOfCards();
        cardStack = stackOfCards.getCardList();
    }

    public Dealer getDealer() {
        return dealer;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public void initDealer() {
        var indexLimit = nextCardIndex - 2;
        while (nextCardIndex > indexLimit) {
            dealer.getCards().add(cardStack.get(nextCardIndex));
            nextCardIndex--;
        }
    }

    public void dealerHit() {
        while (dealer.getTotal() < 17) {
            dealer.getCards().add(cardStack.get(nextCardIndex));
            dealer.setTotal();
            nextCardIndex--;
        }
        switch (dealer.getState()) {
            case BLACKJACK -> players.forEach(player -> {
                if (player.getState() == PlayerState.BLACKJACK) {
                    player.setState(PlayerState.PUSH);
                }

            });
            case BUST -> players.forEach(player -> {
                if (player.getState() == PlayerState.BUST) {
                    player.setState(PlayerState.PUSH);
                } else if (player.getState() == PlayerState.STAY) {
                    player.setState(PlayerState.WON);
                }
            });
            case DONE -> players.forEach(player -> {
                if (player.getState() == PlayerState.STAY) {
                    if ((BLACKJACK - player.getTotal()) < (BLACKJACK - dealer.getTotal())) {
                        player.setState(PlayerState.WON);
                    } else player.setState(PlayerState.LOST);
                }
            });
        }
        setPlayerRewards();
    }

    private void setPlayerRewards() {
        players.forEach(player -> {
            var money = player.getMoney();
            switch (player.getState()) {
                case BUST, LOST -> {
                    money -= player.getBet();
                    player.setMoney(money);
                }
                case WON -> {
                    money += player.getBet();
                    player.setMoney(money);
                }
                case BLACKJACK -> {
                    money -= (float) (1.5 * player.getBet());
                    player.setMoney(money);
                }
                case SURRENDER -> {
                    money -= (float) (0.5 * player.getBet());
                    player.setMoney(money);
                }
            }
        });
    }

    public void resetStack() {
        stackOfCards.shuffleAndReset();
        nextCardIndex = cardStack.size() - 1;
    }

    public ArrayList<Card> throwTwoCards() {
        var indexLimit = nextCardIndex - 2;
        var twoCardList = new ArrayList<Card>();
        while (nextCardIndex > indexLimit) {
            twoCardList.add(cardStack.get(nextCardIndex));
            nextCardIndex--;
        }
        return twoCardList;
    }

    public void hit(int playerNum) {
        var player = players.get(playerNum);
        player.getCards().add(cardStack.get(nextCardIndex));
        player.setTotal();
        nextCardIndex--;
    }
}
