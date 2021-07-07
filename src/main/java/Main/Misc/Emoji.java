package Main.Misc;

public enum Emoji {

    ZERO("\u0030\uFE0F\u20E3"),
    ONE("\u0031\uFE0F\u20E3"),
    TWO("\u0032\uFE0F\u20E3"),
    THREE("\u0033\uFE0F\u20E3"),
    FOUR("\u0034\uFE0F\u20E3"),
    FIVE("\u0035\uFE0F\u20E3"),
    SIX("\u0036\uFE0F\u20E3"),
    SEVEN("\u0037\uFE0F\u20E3"),
    EIGHT("\u0038\uFE0F\u20E3"),
    NINE("\u0039\uFE0F\u20E3"),
    LINK("\uD83D\uDD17"),

    JOIN("\u23FA"),
    START("\u25B6"),
    CANCEL("\u23F9"),
    DECK("\uD83C\uDCCF"),
    SAVE("\uD83D\uDCBE"),
    CHECK("\u2705"),

    RED("\uD83D\uDD34"),
    YELLOW("\uD83D\uDFE1"),
    GREEN("\uD83D\uDFE2"),
    BLUE("\uD83D\uDD35"),
    BLACK("\u26AB"),

    DRAW("\u2795"),
    NEW("\uD83C\uDD95"),

    LEFT("\u2B05"),
    RIGHT("\u27A1"),
    UP("\u2B06"),
    DOWN("\u2B07"),

    ;

    private final String code;

    Emoji(String code) {
        this.code = code;
    }

    public String getEmoji(){
        return code;
    }

}
