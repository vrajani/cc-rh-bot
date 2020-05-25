package pl.vrajani.service;

import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistPrice;
import pl.vrajani.model.CryptoOrderResponse;
import pl.vrajani.model.DataPoint;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.MathUtil;

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
        return Optional.ofNullable(executeBuy(cryptoCurrencyStatus, dataPoints.subList(dataPoints.size() - duration, dataPoints.size()), lastPrice, true)).map(CryptoOrderResponse::getId).orElse(null);
    }

    public CryptoOrderResponse executeBuy(CryptoCurrencyStatus cryptoCurrencyStatus, List<DataPoint> dataPoints, double lastPrice, boolean shouldExecute) {
        double initialPrice = Double.parseDouble(dataPoints.get(0).getClosePrice());
        double tenMinAgoPrice = Double.parseDouble(dataPoints.get(dataPoints.size() - 2).getClosePrice());
        double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        double tenMinPercent = MathUtil.getPercentAmount(lastPrice, tenMinAgoPrice);

        System.out.println("Current Value: " + lastPrice
                         + "\n10 Min ago Value: " + tenMinAgoPrice
                         + "\n10 Min Percent: "+ tenMinPercent
                         + "\n30 Min ago Value: " + initialPrice
                         + "\nBuy Percent: "+ buyPercent);

        double targetBuyPercent = 100 - cryptoCurrencyStatus.getBuyPercent();
        if ((buyPercent < targetBuyPercent && tenMinPercent > 99.45)) {
            System.out.println("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            double quantity = cryptoCurrencyStatus.getBuyAmount() / lastPrice;
            return shouldExecute ? execute(cryptoCurrencyStatus, lastPrice, quantity) :
                    getDummyCryptoOrderResponse(lastPrice, quantity);
        }
        return null;
    }

    private CryptoOrderResponse execute(CryptoCurrencyStatus cryptoCurrencyStatus, double lastPrice, double quantity) {
        return apiService.buyCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(quantity), String.valueOf(lastPrice));
    }

    public static CryptoOrderResponse getDummyCryptoOrderResponse(double lastPrice, double quantity) {
        CryptoOrderResponse dummyResponse = new CryptoOrderResponse();
        dummyResponse.setSide("buy");
        dummyResponse.setPrice(String.valueOf(lastPrice));
        dummyResponse.setQuantity(String.valueOf(quantity));
        dummyResponse.setState("filled");
        dummyResponse.setId("DUMMY_RESPONSE_ID");
        return dummyResponse;
    }
}
