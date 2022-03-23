package strd.lib;

//TODO MMUCHA: externalize
public class Constants {
    private Constants() {}

    public static final int PRODUCT_ID = 0x0fd9;

    //if text is multiline, lets split it, trimming each line, again, no point in printing invisible.
    //we're limiting multiline output to 10 lines. There is not point printing more lines, unless trying
    //to make this code fail.
    public static final int MAX_NUMBER_OF_TEXT_LINES = 10;

    //anything below this is unreadable.
    public static final int MAX_TEXT_LINE_LENGTH = 10;

    public static final int MAX_X_MARGIN = 10;
    public static final int MAX_Y_MARGIN = 10;
}
