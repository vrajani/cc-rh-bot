package pl.vrajani.service;

import pl.vrajani.BackTest;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistPrice;
import pl.vrajani.model.CryptoOrderResponse;
import pl.vrajani.model.DataPoint;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.MathUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ActionService {
    private final APIService apiService;

    public ActionService(APIService apiService) {
        this.apiService = apiService;
    }

    String executeBuyIfPriceDown(CryptoCurrencyStatus cryptoCurrencyStatus) {
        String symbol = cryptoCurrencyStatus.getSymbol();
        CryptoHistPrice cryptoDayData = apiService.getCryptoHistPriceBySymbol(symbol, "day", "5minute");
        double lastPrice = Double.parseDouble(apiService.getCryptoPriceBySymbol(symbol).getMarkPrice());

        List<DataPoint> dataPoints = cryptoDayData.getDataPoints();
        int duration = Integer.parseInt(System.getenv("duration"));
        final List<DataPoint> subDataPoints = dataPoints.subList(dataPoints.size() - duration, dataPoints.size());
        double highPrice = Double.parseDouble(subDataPoints
                .stream()
                .max(Comparator.comparingDouble(dataPoint -> Double.parseDouble(dataPoint.getHighPrice())))
                .get()
                .getHighPrice());
        return Optional.ofNullable(executeBuy(cryptoCurrencyStatus, subDataPoints, lastPrice, highPrice, true))
                .map(CryptoOrderResponse::getId).orElse(null);
    }

    public CryptoOrderResponse executeBuy(CryptoCurrencyStatus cryptoCurrencyStatus, List<DataPoint> dataPoints,
                                          double lastPrice, double highPrice, boolean shouldExecute) {
        double initialPrice = Double.parseDouble(dataPoints.get(0).getClosePrice());
        double tenMinAgoPrice = Double.parseDouble(dataPoints.get(dataPoints.size() - 2).getClosePrice());
        double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        double tenMinPercent = MathUtil.getPercentAmount(lastPrice, tenMinAgoPrice);

        System.out.println("Current Value: " + lastPrice
                         + "\n10 Min ago Value: " + tenMinAgoPrice
                         + "\n10 Min Percent: "+ tenMinPercent
                         + "\n" + (dataPoints.size() * 5) + "Min ago Value: " + initialPrice
                         + "\nBuy Percent: "+ buyPercent);

        double targetBuyPercent = 100 - cryptoCurrencyStatus.getBuyPercent();
        if (buyPercent < targetBuyPercent && tenMinPercent > 99.75 &&
                MathUtil.getPercentAmount(lastPrice, highPrice) < 100 - (cryptoCurrencyStatus.getProfitPercent() * 1.5)) {
            System.out.println("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            double quantity = cryptoCurrencyStatus.getBuyAmount() / lastPrice;
            return shouldExecute ? execute(cryptoCurrencyStatus, lastPrice, quantity) :
                    BackTest.getDummyCryptoOrderResponse(lastPrice, quantity);
        }
        return null;
    }

    private CryptoOrderResponse execute(CryptoCurrencyStatus cryptoCurrencyStatus, double lastPrice, double quantity) {
        return apiService.buyCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(quantity), String.valueOf(lastPrice));
    }
}
