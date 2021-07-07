package Main.Game;

import Main.Misc.Embed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;

public class Player {

    private User user;
    private ArrayList<Card> cards;
    private EmbedBuilder embed;
    private boolean draw;

    public Player(User user){
        this.user = user;
        cards = new ArrayList<>();
        draw = false;
    }

    public User getUser(){
        return user;
    }

    public ArrayList<Card> getCards(){
        return cards;
    }

    public EmbedBuilder getEmbed(){
        return embed;
    }

    public boolean getDraw(){
        return draw;
    }

    public void setEmbedBuilder(EmbedBuilder em){
        embed = em;
    }

    public void setDraw(){
        draw = !draw;
    }
}
