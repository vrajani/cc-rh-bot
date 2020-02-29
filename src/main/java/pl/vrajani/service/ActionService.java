package pl.vrajani.service;

import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoOrderResponse;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.MathUtil;

public class ActionService {
    private double stopLossPercent;

    private APIService apiService;

    public ActionService(APIService apiService) {
        this.apiService = apiService;
        this.stopLossPercent = Double.parseDouble("95");
    }

    String analyseBuy(Double initialPrice, Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        lastPrice = MathUtil.getAmount(lastPrice, 100.10);
        double stopLossResume = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getLastSellPrice());
        double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        double midNightPercent = MathUtil.getPercentAmount(lastPrice, midNightPrice);
        System.out.println("Buy Percent: "+ buyPercent);
        System.out.println("MidNight Percent: "+ midNightPercent);

        // Range
        System.out.println("Checking Low Range Buying....");
        double targetBuyPercent = Double.parseDouble("100") - cryptoCurrencyStatus.getProfitPercent();
        if(midNightPercent < 95) {
            targetBuyPercent = targetBuyPercent - cryptoCurrencyStatus.getProfitPercent();
        }

        if ((cryptoCurrencyStatus.getStopCounter() <= 0 && (buyPercent < targetBuyPercent || midNightPercent < 96)) ||
                (cryptoCurrencyStatus.getStopCounter() > 0 && stopLossResume < 100 - cryptoCurrencyStatus.getProfitPercent())) {
            System.out.println("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            return buy(cryptoCurrencyStatus, lastPrice);
        }
        return null;
    }

    private String buy(CryptoCurrencyStatus cryptoCurrencyStatus, Double buyPrice) {
        double v = cryptoCurrencyStatus.getBuyAmount() / buyPrice;
        CryptoOrderResponse buyCrypto = apiService.buyCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(v), String.valueOf(buyPrice));
        return buyCrypto.getId();
    }

    String analyseSell(Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        //Range
        lastPrice = MathUtil.getAmount(lastPrice, 99.90);
        double sellPercent = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getLastBuyPrice());
        System.out.println("Sell Low Percent: " + sellPercent);
        double targetSelPercent = Double.parseDouble("100") + cryptoCurrencyStatus.getProfitPercent();

        if(midNightPrice < 95) {
            targetSelPercent = targetSelPercent - (cryptoCurrencyStatus.getProfitPercent() / 2);
        }

        if (sellPercent > targetSelPercent || sellPercent < stopLossPercent) {
            System.out.println("Selling Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            return sell(cryptoCurrencyStatus, lastPrice);
        }

        return null;
    }

    private String sell(CryptoCurrencyStatus cryptoCurrencyStatus, Double sellPrice) {
        CryptoOrderResponse sellCrypto = apiService.sellCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(cryptoCurrencyStatus.getQuantity()), String.valueOf(sellPrice));
        return sellCrypto.getId();
    }
}
