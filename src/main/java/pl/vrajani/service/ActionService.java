package pl.vrajani.service;

import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistPrice;
import pl.vrajani.model.CryptoOrderResponse;
import pl.vrajani.model.CryptoOrderStatusResponse;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.MathUtil;

public class ActionService {
    private APIService apiService;

    public ActionService(APIService apiService) {
        this.apiService = apiService;
    }

    String executeBuyIfPriceDown(CryptoCurrencyStatus cryptoCurrencyStatus) {
        String symbol = cryptoCurrencyStatus.getSymbol();
        CryptoHistPrice cryptoHistHourData = apiService.getCryptoHistPriceBySymbol(symbol, "day", "5minute");
        double initialPrice = Double.parseDouble(cryptoHistHourData.getDataPoints().get(cryptoHistHourData.getDataPoints().size() - 9).getClosePrice());
        double dayAgoPrice = Double.parseDouble(cryptoHistHourData.getDataPoints().get(0).getClosePrice());
        double lastPrice = MathUtil.getAmount(Double.parseDouble(apiService.getCryptoPriceBySymbol(symbol).getMarkPrice()) , 100.10);
        double tenMinAgoPrice = Double.parseDouble(cryptoHistHourData.getDataPoints().get(cryptoHistHourData.getDataPoints().size() - 2).getClosePrice());

        System.out.println("1 Day ago Value: " + dayAgoPrice);
        System.out.println("45 Min ago Value: " + initialPrice);
        System.out.println("10 Min ago Value: " + tenMinAgoPrice);
        System.out.println("Current Value: " + lastPrice);

        double stopLossResume = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getLastSellPrice());
        double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        double midNightPercent = MathUtil.getPercentAmount(lastPrice, dayAgoPrice);
        double tenMinPercent = MathUtil.getPercentAmount(lastPrice, tenMinAgoPrice);

        System.out.println("Buy Percent: "+ buyPercent);
        System.out.println("MidNight Percent: "+ midNightPercent);
        System.out.println("10 Min Percent: "+ tenMinPercent);

        double profitPercent = cryptoCurrencyStatus.getProfitPercent();
        double buyAmount = cryptoCurrencyStatus.getBuyAmount();
        if(isVolatile(midNightPercent)) {
            System.out.println("Going Volatile Mode....");
            profitPercent *= 2;
            buyAmount /= 2;
        }

        if(cryptoCurrencyStatus.getStopCounter() > 0){
            buyAmount /= 2;
        }

        double targetBuyPercent = Double.parseDouble("100") - profitPercent;
        if ((cryptoCurrencyStatus.getStopCounter() <= 0 && buyPercent < targetBuyPercent && tenMinPercent > 99.0) ||
                (cryptoCurrencyStatus.getStopCounter() > 0 && stopLossResume < 100 - profitPercent)) {
            System.out.println("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            double quantity = buyAmount / lastPrice;
            CryptoOrderResponse buyCrypto = apiService.buyCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(quantity), String.valueOf(lastPrice));
            return buyCrypto.getId();
        }
        return null;
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

        double stopLossPercent = Double.parseDouble("100") - (cryptoCurrencyStatus.getProfitPercent() * 3.5);

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
