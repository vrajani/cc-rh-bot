package pl.vrajani.utility;

public class MathUtil {

    public static double getPercentAmount(double source, Double out) {
        return (source * 100)/ out;
    }

    public static double getAmount(double source, Double percent) {
        return (source * percent)/ 100;
    }

}
