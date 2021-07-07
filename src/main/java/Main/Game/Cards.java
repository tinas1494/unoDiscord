package Main.Game;

import Main.Misc.Emoji;

public enum Cards {

    RED0("RED","0",false,true),
    RED1("RED","1",false,true),
    RED2("RED","2",false,true),
    RED3("RED","3",false,true),
    RED4("RED","4",false,true),
    RED5("RED","5",false,true),
    RED6("RED","6",false,true),
    RED7("RED","7",false,true),
    RED8("RED","8",false,true),
    RED9("RED","9",false,true),
    YELLOW0("YELLOW","0",false,true),
    YELLOW1("YELLOW","1",false,true),
    YELLOW2("YELLOW","2",false,true),
    YELLOW3("YELLOW","3",false,true),
    YELLOW4("YELLOW","4",false,true),
    YELLOW5("YELLOW","5",false,true),
    YELLOW6("YELLOW","6",false,true),
    YELLOW7("YELLOW","7",false,true),
    YELLOW8("YELLOW","8",false,true),
    YELLOW9("YELLOW","9",false,true),
    GREEN0("GREEN","0",false,true),
    GREEN1("GREEN","1",false,true),
    GREEN2("GREEN","2",false,true),
    GREEN3("GREEN","3",false,true),
    GREEN4("GREEN","4",false,true),
    GREEN5("GREEN","5",false,true),
    GREEN6("GREEN","6",false,true),
    GREEN7("GREEN","7",false,true),
    GREEN8("GREEN","8",false,true),
    GREEN9("GREEN","9",false,true),
    BLUE0("BLUE","0",false,true),
    BLUE1("BLUE","1",false,true),
    BLUE2("BLUE","2",false,true),
    BLUE3("BLUE","3",false,true),
    BLUE4("BLUE","4",false,true),
    BLUE5("BLUE","5",false,true),
    BLUE6("BLUE","6",false,true),
    BLUE7("BLUE","7",false,true),
    BLUE8("BLUE","8",false,true),
    BLUE9("BLUE","9",false,true),
    REDPLUS2("RED","+2",true,true),
    YELLOWPLUS2("YELLOW","+2",true,true),
    GREENPLUS2("GREEN","+2",true,true),
    BLUEPLUS2("BLUE","+2",true,true),
    REDREVERSE("RED","REVERSE",true,true),
    YELLOWREVERSE("YELLOW","REVERSE",true,true),
    GREENREVERSE("GREEN","REVERSE",true,true),
    BLUEREVERSE("BLUE","REVERSE",true,true),
    REDSKIP("RED","SKIP",true,true),
    YELLOWSKIP("YELLOW","SKIP",true,true),
    GREENSKIP("GREEN","SKIP",true,true),
    BLUESKIP("BLUE","SKIP",true,true),
    WILD("WILD",false,true),
    PLUS4("+4",true,true),
    REDRAIN("RED","RAIN",true,false),
    YELLOWRAIN("YELLOW","RAIN",true,false),
    GREENRAIN("GREEN","RAIN",true,false),
    BLUERAIN("BLUE","RAIN",true,false),
    BOMB("BLACK","BOMB",true,false),
    REDPLUS0("RED","+0",true,false),
    YELLOWPLUS0("YELLOW","+0",true,false),
    GREENPLUS0("GREEN","+0",true,false),
    BLUEPLUS0("BLUE","+0",true,false),
    REDINK("RED","INK",true,false),
    YELLOWINK("YELLOW","INK",true,false),
    GREENINK("GREEN","INK",true,false),
    BLUEINK("BLUE","INK",true,false),
    ;

    private final String color;
    private final String value;
    private final boolean action;
    private final boolean original;

    Cards(String col, String val, boolean act, boolean orig){
        color = col;
        value = val;
        action = act;
        original = orig;
    }

    Cards(String val, boolean act, boolean orig){
        color = "BLACK";
        value = val;
        action = act;
        original = orig;
    }

    public String getColor(){
        return color;
    }

    public String getValue(){
        return value;
    }

    public boolean getAction() { return action; }

    public boolean getOriginal() { return original; }

    public String toString(){
        if(color.equals("BLACK")) return value;
        return String.format("%s %s", Emoji.valueOf(color).getEmoji(),value);
    }

}
