package Main.Manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class ServerManager {

    //maps a server id to the bot's prefix in that server
    private static final HashMap<String,String> servers = new HashMap<>();

    /**
     * Gets the bot's prefix in a specific server
     * @param id the id of the server
     * @return a String representing the prefix of the server
     */
    public static String getPrefix(String id){
        return servers.get(id)==null ? "~" : servers.get(id);
    }

    /**
     * Sets the prefix of a server to a new prefix
     * @param id the id of the server
     * @param prefix the new prefix to be changed to
     */
    public static void setPrefix(String id, String prefix){
        if(servers.get(id)==null){
            servers.put(id,prefix);
        }else{
            if(prefix.equals("~")){
                servers.remove(id);
            }else {
                servers.replace(id, prefix);
            }
        }
    }

    /**
     * Loads the hashmap with stored information from a text file about servers and their prefix.
     */
    public static void load() {
        try {
            File file = new File("servers.txt");
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String[] data = sc.nextLine().split(" ");
                setPrefix(data[0],data[1]);
            }
        }catch (IOException ignored){

        }
    }

    /**
     * Stores information about servers and their prefix in a text file.
     */
    public static void update(){
        try {
            File file = new File("servers.txt");
            file.createNewFile();
            FileWriter write = new FileWriter(file);
            for (String s : servers.keySet()) {
                write.append(String.format("%s %s\n", s, servers.get(s)));
            }
            write.flush();
            write.close();
        }catch(IOException ignored){
        }
    }

}
