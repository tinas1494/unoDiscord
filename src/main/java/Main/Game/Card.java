package Main.Game;

import Main.Misc.Emoji;

public class Card {

    private final Cards card;
    private String color;
    private String value;
    private boolean drawn;

    public Card(Cards card){
        this.card = card;
        color = card.getColor();
        value = card.getValue();
        drawn = false;
    }

    ///get and set methods

    public String getColor(){
        return color;
    }

    public String getValue(){
        return value;
    }

    public boolean getDrawn() { return drawn; }

    public boolean getAction() { return card.getAction(); }

    public boolean getOriginal() { return card.getOriginal(); }

    public void setDrawn(){
        drawn = !drawn;
    }

    public void setColor(String color){
        this.color = color;
    }

    ///get and set methods

    public String toString(){
        if(color.equals("BLACK")) return value;
        return String.format("%s %s", Emoji.valueOf(color).getEmoji(),value);
    }

}
