package pl.vrajani.service;

import pl.vrajani.model.*;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.MathUtil;

import java.util.List;
import java.util.Optional;

public class ActionService {
    private APIService apiService;

    public ActionService(APIService apiService) {
        this.apiService = apiService;
    }

    String executeBuyIfPriceDown(CryptoCurrencyStatus cryptoCurrencyStatus) {
        String symbol = cryptoCurrencyStatus.getSymbol();
        CryptoHistPrice cryptoDayData = apiService.getCryptoHistPriceBySymbol(symbol, "day", "5minute");
        double lastPrice = MathUtil.getAmount(Double.parseDouble(apiService.getCryptoPriceBySymbol(symbol).getMarkPrice()) , 100.10);

        List<DataPoint> dataPoints = cryptoDayData.getDataPoints();
        return Optional.ofNullable(executeBuy(cryptoCurrencyStatus, dataPoints.subList(dataPoints.size() - 6, dataPoints.size()), lastPrice, true)).map(CryptoOrderResponse::getId).orElse(null);
    }

    public CryptoOrderResponse executeBuy(CryptoCurrencyStatus cryptoCurrencyStatus, List<DataPoint> dataPoints, double lastPrice, boolean shouldExecute) {
        double initialPrice = Double.parseDouble(dataPoints.get(0).getClosePrice());
        double tenMinAgoPrice = Double.parseDouble(dataPoints.get(dataPoints.size() - 2).getClosePrice());

        System.out.println("30 Min ago Value: " + initialPrice);
        System.out.println("10 Min ago Value: " + tenMinAgoPrice);
        System.out.println("Current Value: " + lastPrice);

        double stopLossResume = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getLastSellPrice());
        double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        double tenMinPercent = MathUtil.getPercentAmount(lastPrice, tenMinAgoPrice);

        System.out.println("Buy Percent: "+ buyPercent);
        System.out.println("10 Min Percent: "+ tenMinPercent);

        double profitPercent = cryptoCurrencyStatus.getProfitPercent();
        double buyAmount = cryptoCurrencyStatus.getBuyAmount();

        double targetBuyPercent = Double.parseDouble("100") - profitPercent;
        if ((cryptoCurrencyStatus.getStopCounter() <= 0 && buyPercent < targetBuyPercent && tenMinPercent > 99.0) ||
                (cryptoCurrencyStatus.getStopCounter() > 0 && stopLossResume < 100 - profitPercent)) {
            System.out.println("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            double quantity = buyAmount / lastPrice;
            if(shouldExecute) {
                return execute(cryptoCurrencyStatus, lastPrice, quantity);
            } else {
                return getDummyCryptoOrderResponse(lastPrice, quantity);
            }
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

    private boolean isVolatile(double midNightPercent) {
        return midNightPercent < 93 || midNightPercent > 108;
    }

    String executeStopLossOrderIfPriceDown(CryptoCurrencyStatus cryptoCurrencyStatus, CryptoOrderStatusResponse cryptoOrderStatusResponse) {
        String symbol = cryptoCurrencyStatus.getSymbol();
        double lastPrice = MathUtil.getAmount(
                Double.parseDouble(apiService.getCryptoPriceBySymbol(symbol).getMarkPrice()),
                99.90);
        double sellPercent = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getLastBuyPrice());

        double stopLossPercent = Double.parseDouble("100") - (cryptoCurrencyStatus.getProfitPercent() * Double.parseDouble(System.getenv("stop_loss_factor")));

        System.out.println("Sell Low Percent: " + sellPercent);
        if(sellPercent < stopLossPercent) {
            System.out.println("Cancelling order to stop loss, symbol: " + symbol + " with last price: " + lastPrice);
            apiService.cancelOrder(symbol, cryptoOrderStatusResponse.getCancelUrl());
            CryptoOrderResponse stopLossSellOrder = apiService.sellCrypto(symbol, cryptoOrderStatusResponse.getQuantity(), String.valueOf(lastPrice));
            return stopLossSellOrder.getId();
        }
        return null;
    }
}
