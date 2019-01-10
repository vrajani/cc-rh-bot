package pl.vrajani.utility;

public class MathUtil {

    public static Double getPercentAmount(double source, Double out) {
        return (source * 100)/ out;
    }

    public static Double getAmount(double source, Double percent) {
        return (source * percent)/ 100;
    }

}
