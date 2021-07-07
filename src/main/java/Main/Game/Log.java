package Main.Game;

import net.dv8tion.jda.api.entities.User;

public class Log {

    private User user;
    private String log;

    public Log(User user, String log){
        this.user = user;
        this.log = log;
    }

    public User getUser(){
        return user;
    }

    public String getLog(){
        return log;
    }


}
