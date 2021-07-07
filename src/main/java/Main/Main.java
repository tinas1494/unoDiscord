package Main;

import Main.Listener.MessageListener;
import Main.Listener.ReactionListener;
import Main.Manager.ServerManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws LoginException {

//        HashMap<Integer,String> map = new HashMap<>();
//        int i=0;
//        map.put(i++,"red");
//        map.put(i++,"blue");
//        map.put(i++,"yellow");
//        map.put(i++,"green");
//        map.put(i++,"gray");
//        map.put(i++,"black");
//        map.put(i++,"purple");
//        map.put(i++,"pink");
//        map.put(i,"orange");
//
//        Random rand = new Random();
//        System.out.println(Arrays.toString(map.entrySet().toArray()));
//
//        try {
//            Field field = map.getClass().getDeclaredField("table");
//            field.setAccessible(true);
//            Map.Entry[] keys = (Map.Entry[])field.get(map);
//            for(Object e : keys){
//                System.out.print(e+", ");
//            }
//            System.out.println();
//            //select 5 random and remove them from map
//            for(int j=0; j<5; j++){
//                int index = rand.nextInt(keys.length);
//                System.out.println("chosen index: "+index);
//                map.remove(index);
//                System.out.print("keys map: ");
//                for(Object e : keys){
//                    System.out.print(e+", ");
//                }
//                System.out.println("\nmap: "+Arrays.toString(map.entrySet().toArray()));
//            }
//
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }

        init(args[0]);
        load();
        Thread update = new Thread(() -> update());
        Runtime.getRuntime().addShutdownHook(update);

    }

    public static void init(String token) throws LoginException {
        JDABuilder builder =  JDABuilder.createDefault(token);
        builder.addEventListeners(new MessageListener());
        builder.addEventListeners(new ReactionListener());
       // builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setActivity(Activity.playing("~help"));
        builder.build();
    }

    public static void update(){
        ServerManager.update();
    }

    public static void load(){
        ServerManager.load();
    }

}

