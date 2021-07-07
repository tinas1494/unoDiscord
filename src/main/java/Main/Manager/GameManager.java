package Main.Manager;

import Main.Game.Player;
import Main.Game.UNO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class GameManager {

    private static final HashMap<Player, ArrayList<UNO>> games  = new HashMap<>();

    public static void load(){
        try{
            File file = new File("games.txt");
            Scanner sc = new Scanner(file);
            while(sc.hasNextLine()){
                String[] data = sc.nextLine().split(" ");
            }
        }catch(IOException ignored){

        }
    }

    public static void update(){
        try {
            File file = new File("games.txt");
            file.createNewFile();
            FileWriter write = new FileWriter(file);
            for (Player p : games.keySet()) {
                for(UNO u : games.get(p)){
                    //written in format [id] [
                    write.append(String.format("%s",p.getUser().getId()));
                }
            }
            write.flush();
            write.close();
        }catch(IOException ignored){
        }
    }

}
