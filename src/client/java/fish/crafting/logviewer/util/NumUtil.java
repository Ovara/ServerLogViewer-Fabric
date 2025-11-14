package fish.crafting.logviewer.util;

import java.text.NumberFormat;

public class NumUtil {

    public static String addCommas(int number) {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        return format.format(number);
    }

    public static String addCommas(long number) {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        return format.format(number);
    }

}
