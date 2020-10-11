package pl.vrajani.utility;

import pl.vrajani.BackTest;
import pl.vrajani.model.CryptoCurrencyStatus;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Function;

public class MathUtil {

    public static double getPercentAmount(double source, Double out) {
        return (source * 100)/ out;
    }

    public static double getAmount(double source, Double percent) {
        return (source * percent)/ 100;
    }

    public static double roundDecimal(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        return Double.parseDouble(df.format(price));
    }
    public static double getMedianPercent(List<CryptoCurrencyStatus> cryptoCurrencyStatuses, Function<CryptoCurrencyStatus, Double> mapperFunction) {
        return roundDecimal(cryptoCurrencyStatuses.stream()
                .map(mapperFunction)
                .sorted()
                .skip((BackTest.TOP_K -1)/2)
                .findFirst()
                .orElse(0.0));
    }
}
