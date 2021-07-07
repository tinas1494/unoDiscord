package Main.Game;

import Main.Listener.ReactionListener;
import Main.Misc.Embed;
import Main.Misc.Emoji;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class UNO {

    //this is the user that initiated the game with the uno command
    //only the creator can do specific things such as adding rules and ending the game
    private final User host;
    private final TextChannel tc; //the channel associated with this specific game (cannot have multiple games per channel)
    private final LinkedList<Player> players; //list of all players in the game
    private static LinkedList<Rule> rules; //list of all rules that exist in the game
    private static Card topCard; //this is the card on top of the deck that all future cards are played upon
    private final AtomicInteger turn; //the index corresponding to whose turn it is in the list of players
    private boolean reverse; //indicates if the game order is normal or reversed
    //keeps track of the current combo of the game
    //this also serves as a boolean checking whether the player is forced to draw or not
    private static int combo;
    private final ArrayList<Card> deck; //the deck containing all the cards, if empty will be exchanged with the discard pile
    private final ArrayList<Card> discard; //cards are placed into discard pile after being played
    private final String messageID; //messageId linked to the embed of game info
    private EmbedBuilder embed; //the embed storing all the game info in the server's channel
    private final Random rand; //used to randomize variables of the game (ex: the card drawn, the player that goes first)
    private boolean started; //indicates if the game has started
    private final ArrayList<Log> history; //keeps track of the players' actions (playing a card, drawing, skipping turn, etc)

    /**
     * Constructor for creating an {@link UNO} game
     * @param user creator/host of the game
     * @param tc the {@link TextChannel} where the game is being hosted
     * @param id the message ID of the embed with game info
     * @param em the embed consisting of the game's info
     */
    public UNO(User user, TextChannel tc, String id, EmbedBuilder em){
        host = user;
        this.tc = tc;
        turn = new AtomicInteger(0); //when creating a game, there is only 1 player (the host)
        reverse = false;
        combo = 0;
        players = new LinkedList<>();
        players.add(new Player(user)); //we add the host as a player
        rules = new LinkedList<>();
        deck = new ArrayList<>();
        discard = new ArrayList<>();
        messageID = id;
        embed = em;
        rand = new Random();
        started = false;
        history = new ArrayList<>();
    }

    ///// get and set methods

    public User getHost(){
        return host;
    }

    public TextChannel getTc(){
        return tc;
    }

    public LinkedList<Player> getPlayers(){
        return players;
    }

    public Card getTopCard(){
        return topCard;
    }

    public EmbedBuilder getEmbed(){
        return embed;
    }

    public String getMessageID(){
        return messageID;
    }

    public boolean getStarted(){
        return started;
    }

    public ArrayList<Log> getHistory(){
        return history;
    }

    public void setEmbedBuilder(EmbedBuilder em){
        embed = em;
    }

    public int getCombo(){
        return combo;
    }

    private void setReverse(){
        reverse = !reverse;
    }

    public void addCombo(int i){
        combo+=i;
    }

    ///// get and set methods

    /**
     * Returns a comma separated list of players in the game as mentions
     * @return a string representing the list of players in the game
     */
    public String listPlayers(){
        StringJoiner res = new StringJoiner(", ");
        for(Player p : players){
            res.add(p.getUser().getAsMention());
        }
        return res.toString();
    }

    /**
     * Returns a comma separated list of rules in the game
     * @return a list of rules in the game
     */
    public String listRules(){
        try {
            StringJoiner res = new StringJoiner(", ");
            for (Rule r : rules) {
                res.add(r.getName());
            }
            return res.toString();
        }
        catch(NullPointerException e){
            return "None";
        }
    }

    /**
     * Checks if the game consists of a specified {@link Rule}
     * @param rule the {@link Rule} to be checked
     * @return {@code true} if the game has the {@link Rule}, {@code false} otherwise
     */
    public boolean hasRule(String rule){
        return rules.contains(Rule.valueOf(rule));
    }

    /**
     * Returns a new line separated list of players (as mentions) and their number of cards.
     * If it is the player's turn, an up or down emoji will be displayed representing the order of turns.
     * @return string with the player's info
     */
    public String playerInfo(){
        StringJoiner res = new StringJoiner("\n");
        Player current = getPlayer(getTurn());

        for(Player p : players){
            String hand = p.getCards().size()==1 ? "UNO" :
                    rules.contains(Rule.CAMOUFLAGE) ? "[?]" : String.format("[%d]",p.getCards().size());
            String status = p.equals(current) ? reverse ? Emoji.UP.getEmoji() : Emoji.DOWN.getEmoji() : "";
            res.add(String.format("%s %s %s", p.getUser().getAsMention(),hand,status));
        }

        return res.toString();
    }

    /**
     * Checks if the {@link User} is a {@link Player} in the game
     * @param user the {@link User} to be checked
     * @return {@code true} if the {@link Player} is in the game, {@code false} otherwise
     */
    public boolean hasPlayer(User user){
        for(Player p : players){
            if(p.getUser().equals(user))
                return true;
        }
        return false;
    }

    /**
     * Gets the {@link Player} corresponding to the given {@link User}
     * @param user the {@link User} to be found
     * @return the {@link Player} corresponding to the given {@link User}, {@code null} if the {@link Player} does not exist
     */
    public Player getPlayer(User user){
        for(Player p : players){
            if(p.getUser().equals(user)){
                return p;
            }
        }
        return null;
    }

    /**
     * Gets the {@link User} in the list of players whose {@link #turn} it is.
     * If the turn (aka index) is out of bounds, we set it to the last index (if order is reversed)
     * or the 0 index (if the order is not reversed)
     * @return the {@link User} whose turn it is
     */
    public User getTurn(){
        try{
            return players.get(turn.get()).getUser();
        }catch(IndexOutOfBoundsException e){
            if(reverse){
                turn.set(players.size()-1);
            }else{
                turn.set(0);
            }
            return getTurn();
        }
    }

    /**
     * Adds a given list of rules to the game
     * @param list the list of rules to be added
     */
    public void addRules(LinkedList<Rule> list){
        rules = new LinkedList<>();
        rules.addAll(list);
    }

    /**
     * Loads the game by setting things up. We choose a random player to go first, whether the turn order is normal or
     * reversed, load the cards, deal the cards, set up players' embeds, then sends the first player's
     * embed in dms. We also acknowledge that the game has started.
     * @param start the number of cards each player starts with
     */
    public void loadGame(int start){
        start = 20; //for now

        turn.set(rand.nextInt(players.size()));
        if(rand.nextInt(2)==0) reverse = true;
        loadCards();

        int index = rand.nextInt(deck.size());
        topCard = deck.get(index);
        discard.add(deck.remove(index));
        history.add(new Log(null,String.format("The game starts with the card %s.",topCard)));

        for(Player p : players){
            draw(p,start);
            EmbedBuilder em = Embed.embed();
            em.setTitle(String.format("UNO game in channel #%s of server %s.",
                    tc.getName(),
                    tc.getGuild().getName()));
            em.setFooter(String.format("React with %s then reply with index of card to be played, or %s to draw a card then either play a card or react with %s to skip your turn.",
                    Emoji.START.getEmoji(),
                    Emoji.DRAW.getEmoji(),
                    Emoji.CANCEL.getEmoji()));
            p.setEmbedBuilder(em);
        }
        ReactionListener.updatePlayer(this,getPlayer(getTurn()).getUser(),true);
        started = true;
    }

    /**
     * Loads the cards in the original deck.
     * Cards are of 4 colors: red, yellow, blue, green, while wild cards are black.
     * Cards have values 0-9 and nonwild actions (skip, reverse, +2, etc); each card is duplicated except 0's.
     * Wild cards are duplicated 4 times. When played, their color is instead set to the player's chosen color.
     * Other special cards are added depending on house rules.
     */
    private void loadCards(){

        if(rules.contains(Rule.MESS)){
            for(Cards c : Cards.values()){
                if(c.getAction()){
                    int num = c.getColor().equals("BLACK") ? c.getValue().equals("BOMB") ? 1 : 4 : 2;
                    for(int i=0; i<num ;i++){
                        deck.add(new Card(c));
                    }
                }
            }
            return;
        }

        for(Cards c : Cards.values()){
            if(c.getOriginal()){
                int num = c.getColor().equals("BLACK") ? 4 : c.getValue().equals("0") ? 1 : 2;
                if(!c.getAction() || !rules.contains(Rule.NOACTION)){
                    for(int i=0; i<num; i++){
                        deck.add(new Card(c));
                    }
                }
            }else{
                break;
            }
        }

        String[] colors = {"RED","YELLOW","GREEN","BLUE"};

        if(!rules.contains(Rule.NOACTION)) {

            if (rules.contains(Rule.RAINCARD)) {
                for (String c : colors) {
                    deck.add(new Card(Cards.valueOf(c + "RAIN")));
                    deck.add(new Card(Cards.valueOf(c + "RAIN")));
                }
            }

            if (rules.contains(Rule.BOMBCARD)) {
                deck.add(new Card(Cards.valueOf("BOMB")));
            }

            if (rules.contains(Rule.SUPERCOMBO)) {
                for (String c : colors) {
                    deck.add(new Card(Cards.valueOf(c + "PLUS0")));
                    deck.add(new Card(Cards.valueOf(c + "PLUS0")));
                }
            }

            if (rules.contains(Rule.INKCARD)) {
                for (String c : colors) {
                    deck.add(new Card(Cards.valueOf(c + "INK")));
                    deck.add(new Card(Cards.valueOf(c + "INK")));
                }
            }

        }
    }

    /**
     * Returns an embed listing the player's cards and game info
     * @param em the embed to edit
     * @param cards the cards to be traversed and listed on the embed
     * @return new embed with updated info and the player's cards
     */
    public static EmbedBuilder listCards(EmbedBuilder em, ArrayList<Card> cards){
        HashMap<String,StringJoiner> colors = new HashMap<>();
        colors.put("RED",new StringJoiner("\n"));
        colors.put("YELLOW",new StringJoiner("\n"));
        colors.put("GREEN",new StringJoiner("\n"));
        colors.put("BLUE",new StringJoiner("\n"));
        colors.put("BLACK",new StringJoiner("\n"));

        AtomicInteger count = new AtomicInteger(1);
        for(Card c : cards){
            if(c.getDrawn()){
                colors.get(c.getColor()).add(String.format("[%d] %s %s", count.getAndIncrement(), c.getValue(), Emoji.NEW.getEmoji()));
                c.setDrawn();
            }else {
                colors.get(c.getColor()).add(String.format("[%d] %s", count.getAndIncrement(), c.getValue()));
            }
        }
        em.addField(Emoji.RED.getEmoji(),colors.get("RED").toString(),true);
        em.addField(Emoji.YELLOW.getEmoji(),colors.get("YELLOW").toString(),true);
        em.addField(Emoji.GREEN.getEmoji(),colors.get("GREEN").toString(),true);
        em.addField(Emoji.BLUE.getEmoji(),colors.get("BLUE").toString(),true);
        em.addField(Emoji.BLACK.getEmoji(),colors.get("BLACK").toString(),true);
        em.addBlankField(true);
        return em;
    }

    /**
     * A player draws a specified {@code amount} of cards.
     * This differs from {@link #draw(Player)} in that this method is for forced draws (such as from
     * {@link Rule#RAINCARD}, {@link Rule#THUNDERCARD}, {@link Rule#NOCOMBO}, etc. There is no {@link Log}
     * that records these draws.
     * @param player the {@link Player} drawing the cards
     * @param num the number of cards to draw
     */
    public void draw(Player player, int num){
        if(rules.contains(Rule.OVERLOAD) && player.getCards().size()+num>10){
            eliminate(player);
            history.add(new Log(player.getUser(),"has over 10 cards and is eliminated."));
        }
        for(int i=0; i<num; i++) {
            int c = rand.nextInt(deck.size());
            if(deck.get(c).getValue().equals("BOMB")){
                eliminate(player);
                history.add(new Log(player.getUser(),"drew the BOMB card and is eliminated."));
                return;
            }
            player.getCards().add(deck.remove(c));
        }
    }

    /**
     * A player draws 1 or {@link #combo} random card(s), depending on if there is a combo or not, and sets all drawn cards
     * as drawn. We remove it from the {@link #deck} and add it to the player's hand and make a history {@link Log} recording
     * that the {@link Player} drew.
     * @param player the {@link Player} drawing the card(s)
     */
    public void draw(Player player){
        int num = combo==0 ? 1 : combo;
        ArrayList<Card> cards = player.getCards();
        for(int i=0; i<num; i++) {
            int c = rand.nextInt(deck.size());
            if(deck.get(c).getValue().equals("BOMB")){
                eliminate(player);
                history.add(new Log(player.getUser(),"drew the BOMB card and is eliminated."));
                return;
            }
            deck.get(c).setDrawn();
            cards.add(deck.remove(c));
        }
        if(num>1){
            combo = 0;
            nextTurn();
        }
        history.add(new Log(player.getUser(),String.format("drew %d card(s).",num)));
        updateGame();
    }

    /**
     * {@link Player} plays a {@link Card} if it is playable. Cards, when played, have special effects depending on the rules
     * and type of {@link Card} it is.
     * @param player the {@link Player} playing the {@link Card}
     * @param card the {@link Card} to be played
     */
    public void play(Player player, Card card){
        player.getCards().remove(card);
        discard.add(topCard);
        topCard = card;
        if(card.getValue().equals("SKIP")){
            if(reverse){
                turn.decrementAndGet();
            }else {
                turn.incrementAndGet();
            }
            try {
                history.add(new Log(player.getUser(), String.format("played the card %s. %s's turn was skipped.", card.toString(), players.get(turn.get()).getUser().getAsMention())));
                nextTurn();
                return;
            }catch(IndexOutOfBoundsException e){
                if(reverse){
                    turn.set(players.size()-1);
                }else{
                    turn.set(0);
                }
                history.add(new Log(player.getUser(), String.format("played the card %s. %s's turn was skipped.", card.toString(), players.get(turn.get()).getUser().getAsMention())));
                nextTurn();
                return;
            }
        }
        if(card.getValue().equals("REVERSE")){
            setReverse();
            history.add(new Log(player.getUser(), String.format("played the card %s. The order of play has been reversed!", card.toString())));
            nextTurn();
            return;
        }
        if(card.getValue().startsWith("+")){
            if(rules.contains(Rule.NOCOMBO)){
                Player next = players.get(turn.incrementAndGet());
                int draw = Integer.parseInt(card.getValue().substring(1));
                draw(next,draw);
                history.add(new Log(player.getUser(),
                        String.format("played the card %s. %s drew %d card(s).",
                                card.toString(),
                                next.getUser().getAsMention(),
                                draw)));
                nextTurn();
                return;
            }
            addCombo(Integer.parseInt(card.getValue().substring(1)));
            history.add(new Log(player.getUser(), String.format("played the card %s. The combo is now %d.", card.toString(),combo)));
            nextTurn();
            return;
        }
        if(card.getValue().equals("RAIN")){
            for(Player p : players){
                if(!p.equals(players.get(turn.get()))){
                    draw(p,1);
                }
            }
            history.add(new Log(player.getUser(), String.format("played the card %s. Everyone else drew 1 card.", card.toString())));
            nextTurn();
            return;
        }
        if(card.getValue().equals("WILD") || card.getValue().equals("+4")){
            AtomicReference<String> msgID = new AtomicReference<>();
            EmbedBuilder em = Embed.embed();
            em.setDescription("React with the color of your choice");
            player.getUser().openPrivateChannel().complete().sendMessage(em.build()).queue(m -> {
                m.addReaction(Emoji.RED.getEmoji()).queue();
                m.addReaction(Emoji.YELLOW.getEmoji()).queue();
                m.addReaction(Emoji.GREEN.getEmoji()).queue();
                m.addReaction(Emoji.BLUE.getEmoji()).queue();
                msgID.set(m.getId());
            });
            EventWaiter waiter = new EventWaiter();
            tc.getJDA().addEventListener(waiter);
            waiter.waitForEvent(PrivateMessageReactionAddEvent.class,
                    (event) -> getTurn().equals(event.getUser()) && (
                            event.getReactionEmote().getEmoji().equals(Emoji.RED.getEmoji())
                            || event.getReactionEmote().getEmoji().equals(Emoji.YELLOW.getEmoji())
                            || event.getReactionEmote().getEmoji().equals(Emoji.GREEN.getEmoji())
                            || event.getReactionEmote().getEmoji().equals(Emoji.BLUE.getEmoji())),
                    (event) -> {
                        String emoji = event.getReactionEmote().getEmoji();
                        if(emoji.equals(Emoji.RED.getEmoji())) card.setColor("RED");
                        if(emoji.equals(Emoji.YELLOW.getEmoji())) card.setColor("YELLOW");
                        if(emoji.equals(Emoji.GREEN.getEmoji())) card.setColor("GREEN");
                        if(emoji.equals(Emoji.BLUE.getEmoji())) card.setColor("BLUE");
                        history.add(new Log(player.getUser(), String.format("played the card %s.", card.toString())));
                        event.getChannel().deleteMessageById(String.valueOf(msgID)).complete();
                        nextTurn();
                    });
        }
        if(!card.getColor().equals("BLACK")){
            history.add(new Log(player.getUser(), String.format("played the card %s.", card.toString())));
            nextTurn();
        }
    }

    /**
     * Eliminates a player from the game by removing them from the list of players and transferring
     * their cards to the discard pile.
     * @param p the player to be eliminated
     */
    public void eliminate(Player p){
        players.remove(p);
        for(Card c : p.getCards()){
            discard.add(c);
        }
    }

    /**
     * Returns a string representing the {@link #history} of the game. Only the last 5 events are displayed.
     * If the user is {@code null}, the {@link Log} is a system message and so no users are mentioned.
     * @return a string representing the {@link #history} (or last 5 events) of the game
     */
    public String displayHistory(){
        StringJoiner res = new StringJoiner("\n");
        if(history.size()>5){
            for(int i=history.size()-5; i<history.size(); i++){
                Log log = history.get(i);
                try {
                    res.add(String.format("%s %s", log.getUser().getAsMention(), log.getLog()));
                }catch(NullPointerException e){
                    res.add(log.getLog());
                }
            }
        }else{
            for (Log log : history) {
                try {
                    res.add(String.format("%s %s", log.getUser().getAsMention(), log.getLog()));
                }catch(NullPointerException e){
                    res.add(log.getLog());
                }
            }
        }
        return res.toString();
    }

    /**
     * Checks if a {@link Card} is playable.
     * A {@link Card} is deemed playable if there is no combo (otherwise you are forced to draw),
     * if the {@link Card} is black, or if the card and the {@link #topCard}'s color or value match.
     * Special house rules (such as {@link Rule#CLEANFINISH} may affect if a {@link Card} is considered playable.
     * @param card the {@link Card} to be played
     * @return {@code true} if the {@link Card} is playable as described, {@code false} otherwise
     */
    public static boolean playable(Player p, Card card){
        PrivateChannel pc = p.getUser().openPrivateChannel().complete();
        if(combo != 0) {
            if(rules.contains(Rule.SUPERCOMBO)){
                if(!card.getValue().startsWith("+")){
                    pc.sendMessage(Embed.errorMessage(String.format("There is a combo of %s. You must draw this amount or play a draw card.",combo))).queue();
                    return false;
                }else{
                    if(topCard.getValue().equals("+4")){
                        if(!card.getValue().equals("+4") && !topCard.getColor().equals(card.getColor())){
                            pc.sendMessage(Embed.errorMessage("You must place a draw card of the same color or value.")).queue();
                            return false;
                        }
                    }else{
                        if(!topCard.getColor().equals(card.getColor()) && !topCard.getValue().equals(card.getValue())){
                            pc.sendMessage(Embed.errorMessage("You must place a draw card of the same color or value.")).queue();
                            return false;
                        }
                    }
                    return true;
                }
            }else{
                if(!topCard.getValue().equals(card.getValue())){
                    pc.sendMessage(Embed.errorMessage("Card values must match.")).queue();
                    return false;
                }
                return true;
            }
        }
        if(rules.contains(Rule.CLEANFINISH) && p.getCards().size()==1){
            if(card.getValue().length()!=1){
                pc.sendMessage(Embed.errorMessage("You can only win if your last card is a numerical card.")).queue();
                return false;
            }else{
                if(topCard.getColor().equals(card.getColor()) ||
                        topCard.getValue().equals(card.getValue())){
                    return true;
                }else{
                    pc.sendMessage("Card colors or values must match.").queue();
                    return false;
                }
            }
        }
        return topCard.getColor().equals(card.getColor()) ||
                topCard.getValue().equals(card.getValue()) ||
                card.getColor().equals("BLACK");
    }

    /**
     * On progressing to the next turn, we update {@link #turn} and automatically dm
     * the next player indicating it is their turn. We also update the game {@link #embed} in the server.
     */
    public void nextTurn(){
        if(reverse){
            turn.decrementAndGet();
        }else {
            turn.incrementAndGet();
        }
        try{
            players.get(turn.get());
        }catch(IndexOutOfBoundsException e){
            if(reverse){
                turn.set(players.size()-1);
            }else{
                turn.set(0);
            }
        }
        if (topCard.getValue().equals("INK")) {
            players.get(turn.get()).getCards().forEach(c -> {
                if (!c.getColor().equals("BLACK")) {
                    c.setColor(topCard.getColor());
                }
            });
        }
        ReactionListener.updatePlayer(this,getTurn(),true);
        updateGame();
    }

    /**
     * We update the game {@link #embed} with new game info and updates the server channel with this info.
     */
    public void updateGame(){
        embed.clearFields();
        embed.addField("Top Card",topCard.toString(),true);
        embed.addField("Combo",String.valueOf(getCombo()),true);
        embed.addField("Rules",listRules(),true);
        embed.addField("Players",playerInfo(),true);
        embed.addField("History",displayHistory(),false);
        tc.editMessageById(messageID,embed.build()).queue();
    }

}
