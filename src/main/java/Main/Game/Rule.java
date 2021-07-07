package Main.Game;

public enum Rule {

    //done
    NOACTION("No Action","There are only numerical and wild cards in the deck."),
    //test
    CAMOUFLAGE("Camouflage","You cannot see anyone’s number of cards until they only have 1 card left."),
    PERFECTION("Perfection","If you play a card whose numerical value is equal to the number of cards in your hand, you can play again."),
    //test
    NOCOMBO("No Combo","You cannot block combos."),
    //test
    CLEANFINISH("Clean Finish","You can only win if your last card is a numerical card."),
    //test
    BOMBCARD("Bomb Card","You are eliminated if you draw this card."),
    //test
    SUPERCOMBO("Super Combo","You can block a combo with any + card.","PLUS0"),
    SUDDENDEATHCARD("Sudden Death Card","You are eliminated if you are unable to play a card.","SUDDENDEATH"),
    //test
    INKCARD("Ink Card","When you play this card, every colored card on the next player’s hand turns the color of your ink card.","INK"),
    //test
    MESS("Mess","The deck consists only of action cards."),

    EQUALITYCARD("Equality Card","When you play this card, every player either draws or discards cards until everyone has 3 cards.","EQUALITY"),
    OVERLOAD("Overload","Players with more than 10 cards get eliminated."),
    //done
    RAINCARD("Rain Card","When you play this card, every other player draws 1 card.","RAIN"),
    CLONECARD("Clone Card","The card activates the same effect as the previous card.","CLONE"),
    JUSTICECARD("Justice Card","When you play this card, discard 1 card for each player with fewer cards than you.","JUSTICE"),
    THUNDERCARD("Thunder Card","When you play this card, two random players draw 1-5 cards.","THUNDER"),
    MAGNETCARD("Magnet Card","When you play this card, discard every card that has your magnet's color.","MAGNET"),
    //done
    MAXI("Maxi","Everyone starts with 9 cards."),
    //done
    MINI("Mini","Everyone starts with 4 cards."),
    //revelation: top card of deck is visible to everyone
    //blue and greeN: treated as same color
    //dancing chairs card: change order of players
    //peace card: when played, actions have no effect for next 2 turns
    ;

    private final String name;
    private final String description;
    private final String code;

    Rule(String name, String description){
        this.name = name;
        this.description = description;
        code = null;
    }

    Rule(String name, String description, String code){
        this.name = name;
        this.description = description;
        this.code = code;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getCode() { return code; }

}
