package Main.Misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.util.Random;

public class Embed {

    public static EmbedBuilder embed(){

        EmbedBuilder em = new EmbedBuilder();
        Random rand = new Random();
        Color col = new Color(rand.nextInt(256),rand.nextInt(256),rand.nextInt(256));
        em.setColor(col);

        return em;
    }

    public static MessageEmbed errorMessage(String message){
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.RED);
        em.setDescription(message);
        return em.build();
    }

    public static MessageEmbed systemMessage(String message){
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setDescription(message);
        return em.build();
    }

}
