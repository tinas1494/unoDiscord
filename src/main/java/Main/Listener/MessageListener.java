package Main.Listener;

import Main.Game.Command;
import Main.Game.Rule;
import Main.Manager.ServerManager;
import Main.Misc.Embed;
import Main.Misc.Emoji;
import Main.Game.UNO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;


public class MessageListener extends ListenerAdapter {

    //maps a textchannel to a game, allows for only one game per channel
    protected static HashMap<TextChannel,UNO> games = new HashMap<>();
    //maps a string indicating the type of message to an array of the messages of that type
    //this allows us to know what message was sent without looking at its contents
    //so we know what to do with that message
    public static HashMap<String, ArrayList<Message>> reactionMessages = new HashMap<>();
    //maps a message in dms to a specific game
    //lets us know which game the player is responding to (since they can be in multiple games)
    public static HashMap<Message, UNO> privateGames = new HashMap<>();

    /**
     * Occurs when a message is received in a server. Sends information about the event, specifically
     * the guild and channel where the message was sent, the user sending it, and the message itself.
     * @param event the event that a server message is received
     */
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        newGuildMessage(event.getGuild(), event.getAuthor(), event.getChannel(), event.getMessage());
    }

    /**
     * Handles guild messages received.
     * @param guild the guild where the message was received
     * @param user the user sending the message
     * @param tc the channel where the message was received
     * @param msg the message received
     */
    private void newGuildMessage(Guild guild, User user, TextChannel tc, Message msg){
        if(user.isBot())
            return;

        String[] message = msg.getContentRaw().split("\\s+");

        if(message.length>0){

            String cmd = message[0];
            String prefix = ServerManager.getPrefix(guild.getId());

            if(cmd.startsWith(prefix)) {

                try {

                    String command = cmd.substring(1).toUpperCase();

                    //admin commands
                    if(user.getId().equals("335591012976295939")){
                        if(command.equals("ADDRULE")){
                            StringBuilder name = new StringBuilder();
                            for(int i=1; i<message.length; i++){
                                name.append(message[i]);
                            }
                            Rule rule = Rule.valueOf(name.toString().toUpperCase());
                            LinkedList<Rule> rules = new LinkedList<>();
                            rules.add(rule);
                            games.get(tc).addRules(rules);
                        }
                    }

                    if (command.equals("UNO")) {
                        if (games.get(tc) == null) {
                            try{
                                user.openPrivateChannel().complete().sendMessage(
                                        Embed.systemMessage("You have joined the game!")
                                ).complete();
                            }catch(ErrorResponseException e){
                                tc.sendMessage(Embed.errorMessage
                                        (String.format("%s, please enable direct messages so you can join the game!",
                                                user.getAsMention()))).queue();
                                return;
                            }
                            EmbedBuilder em = createGame(user,tc);
                            tc.sendMessage(em.build()).queue(m ->{
                                addUNOReactions(m);
                                if(reactionMessages.get("UNO")==null){
                                    ArrayList<Message> temp = new ArrayList<>();
                                    temp.add(m);
                                    reactionMessages.put("UNO",temp);
                                }else{
                                    reactionMessages.get("UNO").add(m);
                                }
                                games.put(tc,new UNO(user,tc,m.getId(),em));
                            });
                        } else {
                            tc.sendMessage(Embed.errorMessage("There is already a game in this channel.")).queue();
                        }
                    }

                    if (command.equals("HELP")){
                        EmbedBuilder em = Embed.embed();
                        em.setTitle("Welcome to UNO!");
                        em.setDescription("Start a game of UNO with the command ~uno. Each UNO game has a maximum of 8 players and a creator: the only user who can start, cancel, and save games.");
                        StringJoiner help = new StringJoiner("\n");
                        Command[] commands = Command.values();
                        for(Command c : commands){
                            help.add(String.format("`%s%s%s` - %s",prefix,c.name(),c.getUsage(),c.getDescription()));
                        }
                        em.addField("Commands",help.toString(),true);
                        em.setFooter("command [argument] - description");
                        tc.sendMessage(em.build()).queue();
                    }

                    if(command.equals("CLEAR")){
                        if(games.get(tc)!=null){
                            TextChannel channel = games.get(tc).getTc();
                            List<Message> messages = channel.getHistoryAfter(games.get(tc).getMessageID(),100).complete().getRetrievedHistory();
                            tc.deleteMessages(messages).queue();
                        }
                    }

                    if(command.equals("RULE")){
                        StringBuilder name = new StringBuilder();
                        for(int i=1; i<message.length; i++){
                            name.append(message[i]);
                        }
                        Rule rule = Rule.valueOf(name.toString().toUpperCase());
                        EmbedBuilder em = Embed.embed();
                        em.setTitle(rule.getName());
                        em.setDescription(rule.getDescription());
                        tc.sendMessage(em.build()).queue();
                    }

                    if(command.equals("PREFIX")){
                        try {
                            ServerManager.setPrefix(guild.getId(), message[1]);
                            tc.sendMessage(Embed.systemMessage(String.format("The prefix has been set to %s.",message[1]))).queue();
                        }catch(ArrayIndexOutOfBoundsException e){
                            tc.sendMessage(Embed.errorMessage("Please provide a new prefix.")).queue();
                        }
                    }

                    if(command.equals("UPDATE") || command.equals("DIE")){
                        System.exit(0);
                    }

                } catch (IllegalArgumentException e) {
                }
            }
        }
    }

    private EmbedBuilder createGame(User user, TextChannel tc){
        EmbedBuilder em = Embed.embed();
        em.setTitle(String.format("Starting UNO game in channel #%s.",tc.getName()));
        em.setDescription("Current players: "+user.getAsMention());
        em.setFooter(String.format("React with %s to join, %s to add specific rules, %s to start, or %s to cancel the game.",
                Emoji.JOIN.getEmoji(),
                Emoji.DRAW.getEmoji(),
                Emoji.START.getEmoji(),
                Emoji.CANCEL.getEmoji()));
        return em;
    }

    private void addUNOReactions(Message m){
        m.addReaction(Emoji.JOIN.getEmoji()).queue();
        m.addReaction(Emoji.DRAW.getEmoji()).queue();
        m.addReaction(Emoji.START.getEmoji()).queue();
        m.addReaction(Emoji.CANCEL.getEmoji()).queue();
    }

}
