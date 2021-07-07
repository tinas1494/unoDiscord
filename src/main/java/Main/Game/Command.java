package Main.Game;

public enum Command {

    help(null,"lists bot commands"),
    uno(null,"starts a game of UNO"),
    rule(" [name]","describes the specified rule"),
    clear(null,"clears all message after starting game"),
    prefix(" [prefix]","changes the prefix for the server"),
    ;

    private String description;
    private String usage;

    Command(String u, String d){
        usage = u;
        description = d;
    }

    public String getUsage(){
        if(usage == null){
            return "";
        }
        return usage;
    }

    public String getDescription(){
        return description;
    }
}
