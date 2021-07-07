package Main.Listener;

import Main.Game.Log;
import Main.Game.Rule;
import Main.Misc.Embed;
import Main.Misc.Emoji;
import Main.Game.Player;
import Main.Game.UNO;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class ReactionListener extends ListenerAdapter {

    /**
     * Occurs when a reaction is added on a message. Sends information about the event, specifically
     * the user reacting, the guild and channel where the reaction occured, the message id of the message,
     * and the reaction added.
     * @param event the event that a reaction is added
     */
    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if(event.isFromGuild()){
            messageReactionHandler(event.getGuild(), event.getUser(), event.getChannel(), event.getMessageId(), event.getReactionEmote().getEmoji());
        }else{
            messageReactionHandler(null, event.getUser(),event.getChannel(),event.getMessageId(),event.getReactionEmote().getEmoji());
        }
    }

    /**
     * Handles users' reactions to messages
     * @param guild guild where the reaction occurred
     * @param user the user reacting
     * @param mc the channel where the reaction occurred
     * @param msgID the message ID of the message being reacted to
     * @param emoji the reaction emoji reacted
     */
    private void messageReactionHandler(Guild guild, User user, MessageChannel mc, String msgID, String emoji){
        //get message
        Message msg = mc.retrieveMessageById(msgID).complete();

        //does not respond to bot reactions and only responds if the message itself was from this bot
        if(user.isBot())
            return;
        if(!msg.getAuthor().equals(mc.getJDA().getSelfUser()))
            return;

        //gets the "type" of message so we know what to do with it
        AtomicReference<String> type = new AtomicReference<>("");

        MessageListener.reactionMessages.forEach((key, value) -> {
            for (Message m : value) {
                if (m.equals(msg))
                    type.set(key);
            }
        });

        //the message is the embed sent to server channels about the game info
        if(type.get().equals("UNO")){
            UNO game = MessageListener.games.get(mc);
            //adds player to game
            //max 8 players and does not add if already exists player in the game
            if(emoji.equals(Emoji.JOIN.getEmoji())){
                if(game.getPlayers().size()<8 && !game.hasPlayer(user)) {
                    try{
                        user.openPrivateChannel().complete().sendMessage(
                                Embed.systemMessage("You have joined the game!")
                        ).complete();
                    }catch(ErrorResponseException e){
                        mc.sendMessage(Embed.errorMessage(
                                String.format("%s, please enable direct messages so you can join the game!",
                                        user.getAsMention()))).queue();
                        return;
                    }
                    game.getPlayers().add(new Player(user));
                    //update embed with new players
                    EmbedBuilder temp = game.getEmbed();
                    temp.setDescription("Players: " + game.listPlayers());
                    game.setEmbedBuilder(temp);
                    mc.editMessageById(game.getMessageID(), temp.build()).queue();
                }
            }
            //to add specific rules to the game
            //sends list of rules and waits for a valid user response, then starts the game
            //valid response is a space separate list of indices corresponding to the rules the user wishes to add
            //deletes the msg embed with list of rules
            if(emoji.equals(Emoji.DRAW.getEmoji())) {
                if (user.equals(game.getHost())) {
                    final String[] tempId = {null};
                    mc.sendMessage(chooseRules().build()).queue(m -> tempId[0] = m.getId());
                    EventWaiter waiter = new EventWaiter();
                    mc.getJDA().addEventListener(waiter);
                    waiter.waitForEvent(GuildMessageReceivedEvent.class,
                            (event) -> convertRules(event.getMessage()) != null,
                            (event) -> {
                                game.addRules(convertRules(event.getMessage()));
                                mc.deleteMessageById(tempId[0]).queue();
                                startGame(game);
                            }, 60, TimeUnit.SECONDS, null);
                }
            }
            //starts the game
            //only host of game can start the game
            if(emoji.equals(Emoji.START.getEmoji())){
                if(user.equals(game.getHost())) {
                    startGame(game);
                }
            }
            //cancels the game
            //if game already started, waits 5s for user to confirm cancelling the game by reacting with a check
            //only host of game can cancel the game
            if(emoji.equals(Emoji.CANCEL.getEmoji())){
                if(user.equals(game.getHost())) {
                    if(game.getStarted()){
                        EventWaiter waiter = new EventWaiter();
                        guild.getJDA().addEventListener(waiter);
                        waiter.waitForEvent(MessageReactionAddEvent.class,
                                (event) -> user.equals(event.getUser()) && event.getReactionEmote().getEmoji().equals(Emoji.CHECK.getEmoji()),
                                (event) -> cancelGame(game),
                                5, TimeUnit.SECONDS, null);
                    }else{
                        cancelGame(game);
                    }
                }
            }
            //sends the deck of the player to their dms
            //only if the user reacting is a player
            //embed has info such as the top card, combo, rules, list of players and the # of cards they have
            if(emoji.equals(Emoji.DECK.getEmoji())){
                if(game.hasPlayer(user)){
                    updatePlayer(game,user,true);
                }
            }
            //then we remove the reaction
            ((TextChannel)mc).removeReactionById(msgID, emoji, user).queue();
        }

        //this is the message sent to players' dms about their deck
        if(type.get().equals("DECK")){
            UNO game = MessageListener.privateGames.get(msg);
            //only considers if it is the user reacting's turn in game
            if(user.equals(game.getTurn())){
                Player p = game.getPlayer(user);
                //player is trying to play a card
                //waits 30s for a response (the index of the card user wants to play)
                if (emoji.equals(Emoji.START.getEmoji())) {
                    EventWaiter waiter = new EventWaiter();
                    mc.getJDA().addEventListener(waiter);
                    waiter.waitForEvent(PrivateMessageReceivedEvent.class,
                            //checks if the card is playable, if it even exists
                            (event) -> event.getAuthor().equals(game.getTurn()) && UNO.playable(p,
                                    p.getCards().get(Integer.parseInt(event.getMessage().getContentRaw())-1)),
                            //plays the card
                            //deletes the message and removes it from the list of messages to be listened to
                            (event) -> {
                                game.play(p,p.getCards().get(
                                    Integer.parseInt(event.getMessage().getContentRaw())-1));
                                MessageListener.reactionMessages.get("DECK").remove(msg);
                                MessageListener.privateGames.remove(msg);
                                mc.deleteMessageById(msgID).queue();
                                //maybe updatePlayer() here but dont think necessary
                                },
                            30, TimeUnit.SECONDS, null);
                }
                //player wants to draw a card
                //if there is no combo, the player draws 1 card and gets to choose to play a card or skip
                //if there is a combo, the player draws that amount and their turn is skipped
                //player draws and we indicate that the player has drawn
                //we remove the message from the list of messages to be listened to
                //delete the message and update the player's embed with updated info
                if(emoji.equals(Emoji.DRAW.getEmoji())) {
                    boolean noCombo = game.getCombo()==0;
                    game.draw(p);
                    if(noCombo) {
                        p.setDraw();
                        updatePlayer(game,user,false);
                    }
                    MessageListener.reactionMessages.get("DECK").remove(msg);
                    MessageListener.privateGames.remove(msg);
                    mc.deleteMessageById(msgID).queue();
                }
                //player wants to skip his turn after drawing
                //we add a log to history indicating the player skipped their turn
                //we reset the player's draw boolean and go next turn
                if(emoji.equals(Emoji.CANCEL.getEmoji())){
                    if(p.getDraw()) {
                        game.getHistory().add(new Log(user,"skipped their turn."));
                        game.nextTurn();
                        p.setDraw();
                    }
                }
            }
        }
    }

    /**
     * Starts a game of UNO where each player starts with 7 cards.
     * @param game the game to be started
     */
    private void startGame(UNO game){
        if(game.hasRule("MAXI")){
            startGame(game,9);
        }else if(game.hasRule("MINI")){
            startGame(game,4);
        }else {
            startGame(game, 7);
        }
    }

    /**
     * Starts the game of uno by loading the game (dealing cards to players, setting top card and first player, etc).
     * Sets the embed for the game in server channel
     * Clears previous reactions (before starting game) to new reactions (game in progress)
     * @param game the game to be started
     * @param start the number of starting cards
     */
    private void startGame(UNO game, int start){
        MessageChannel mc = game.getTc();
        Message msg = mc.retrieveMessageById(game.getMessageID()).complete();
        game.loadGame(start);
        EmbedBuilder em = game.getEmbed();
        em.setTitle(String.format("Ongoing UNO game in channel #%s...",game.getTc().getName()));
        em.setDescription("");
        em.addField("Top Card",game.getTopCard().toString(),true);
        em.addField("Combo",String.valueOf(game.getCombo()),true);
        em.addField("Rules",game.listRules(),true);
        em.addField("Players",game.playerInfo(),true);
        em.addField("History",game.displayHistory(),false);
        em.setFooter(String.format("React with %s to view your deck, %s to save the game, or %s then %s to cancel the game.",
                Emoji.DECK.getEmoji(),
                Emoji.SAVE.getEmoji(),
                Emoji.CANCEL.getEmoji(),
                Emoji.CHECK.getEmoji()));
        game.setEmbedBuilder(em);
        mc.editMessageById(game.getMessageID(), em.build()).queue();
        msg.clearReactions().complete();
        addStartReactions(msg);
    }

    /**
     * Updates a specific player's with updated game info through dm
     * @param game the game with info to be updated
     * @param user the player who we want to update with new info
     * @param deck whether we want deck reactions (true) or draw reactions (false)
     */
    public static void updatePlayer(UNO game, User user, boolean deck){
        Player p = game.getPlayer(user);
        EmbedBuilder em = p.getEmbed();
        em.clearFields();
        em.addField("Top Card",game.getTopCard().toString(),true);
        em.addField("Combo",String.valueOf(game.getCombo()),true);
        em.addField("Rules",game.listRules(),true);
        EmbedBuilder finalEmbed = UNO.listCards(em,p.getCards());
        finalEmbed.addField("Players",game.playerInfo(),true);
        p.setEmbedBuilder(finalEmbed);
        user.openPrivateChannel().complete().sendMessage(
                finalEmbed.build()
        ).queue(msg -> {
            if(deck) {
                addDeckReactions(msg, game);
            }else{
                addDrawReactions(msg,game);
            }
        //    MessageListener.privateGames.put(msg,game);
        });
    }

    /**
     * Converts the user response for rules they want in the game into a list of actual rules.
     * User response is a space separated list of indices corresponding to the rules.
     * @param m the message to be converted
     * @return a list of {@link Rule}s to be added to the game
     */
    private LinkedList<Rule> convertRules(Message m){
        String[] message = m.getContentRaw().split("\\s+");
        Rule[] rules = Rule.values();
        LinkedList<Rule> res = new LinkedList<>();
        for(String s : message){
            try{
                int temp = Integer.parseInt(s);
                    res.add(rules[temp-1]);
            //invalid responses such as not an integer or a number not on the list of rules
            }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
                return null;
            }
        }
        return res;
    }

    /**
     * Returns the embed storing list of rules for players to choose
     * @return embed storing list of rules for players to choose
     */
    private EmbedBuilder chooseRules(){
        EmbedBuilder em = Embed.embed();
        em.setTitle("Rules");
        em.setFooter("Reply with the index/indices of rule(s) to be added.");
        StringJoiner rules = new StringJoiner("\n");
        AtomicInteger count = new AtomicInteger(1);
        for(Rule r : Rule.values()){
            rules.add(String.format("[%d] %s",count.getAndIncrement(),r.getName()));
        }
        em.addField("[index] Rule Name",rules.toString(),true);
        return em;
    }

    /**
     * Cancels the game and changes the game info embed accordingly, and removes the message from list of messages
     * to be listened to
     * @param game the game to be cancelled
     */
    private void cancelGame(UNO game){
        MessageChannel mc = game.getTc();
        Message msg = mc.retrieveMessageById(game.getMessageID()).complete();
        EmbedBuilder em = game.getEmbed();
        em.clear();
        em.setTitle(String.format("Cancelled UNO game in channel #%s.",mc.getName()));
        mc.editMessageById(game.getMessageID(), em.build()).queue();
        MessageListener.games.remove(mc);
        MessageListener.reactionMessages.get("UNO").remove(msg);
    }

    /**
     * Adds specific reactions to embed when game has started
     * @param msg the message to be reacted to
     */
    private void addStartReactions(Message msg){
        msg.addReaction(Emoji.DECK.getEmoji()).queue();
        msg.addReaction(Emoji.SAVE.getEmoji()).queue();
        msg.addReaction(Emoji.CANCEL.getEmoji()).queue();
        msg.addReaction(Emoji.CHECK.getEmoji()).queue();
    }

    /**
     * Adds specific reactions to embed when player drew a card
     * @param msg the message to be reacted to
     * @param game the game to be linked to the message
     */
    private static void addDrawReactions(Message msg, UNO game){
        msg.addReaction(Emoji.START.getEmoji()).queue();
        msg.addReaction(Emoji.CANCEL.getEmoji()).queue();
        if(MessageListener.reactionMessages.get("DECK")==null){
            ArrayList<Message> tempMsg = new ArrayList<>();
            tempMsg.add(msg);
            MessageListener.reactionMessages.put("DECK",tempMsg);
        }else{
            MessageListener.reactionMessages.get("DECK").add(msg);
        }
        MessageListener.privateGames.put(msg,game);
    }

    /**
     * Adds specific reactions when it is the player's turn
     * @param msg the message to be reacted to
     * @param game the game to be linked to the message
     */
    private static void addDeckReactions(Message msg, UNO game){
        msg.addReaction(Emoji.START.getEmoji()).queue();
        msg.addReaction(Emoji.DRAW.getEmoji()).queue();
        if(MessageListener.reactionMessages.get("DECK")==null){
            ArrayList<Message> tempMsg = new ArrayList<>();
            tempMsg.add(msg);
            MessageListener.reactionMessages.put("DECK",tempMsg);
        }else{
            MessageListener.reactionMessages.get("DECK").add(msg);
        }
        MessageListener.privateGames.put(msg,game);
    }

}
