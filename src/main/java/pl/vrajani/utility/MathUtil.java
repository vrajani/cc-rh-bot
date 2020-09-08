package pl.vrajani.utility;

import java.text.DecimalFormat;

public class MathUtil {

    public static double getPercentAmount(double source, Double out) {
        return (source * 100)/ out;
    }

    public static double getAmount(double source, Double percent) {
        return (source * percent)/ 100;
    }

    public static String roundDecimal(double price, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(price);
    }
}
