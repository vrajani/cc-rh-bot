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

        System.out.println("Checking Low Range Buying....");
        double targetBuyPercent = Double.parseDouble("100") - cryptoCurrencyStatus.getProfitPercent();
        if(midNightPercent < 95) {
            targetBuyPercent = targetBuyPercent - cryptoCurrencyStatus.getProfitPercent();
        }

        Double buyAmount = cryptoCurrencyStatus.getBuyAmount();
        if(cryptoCurrencyStatus.getStopCounter() > 0){
            buyAmount /= 2;
        }

        if ((cryptoCurrencyStatus.getStopCounter() <= 0 && buyPercent < targetBuyPercent) ||
                (cryptoCurrencyStatus.getStopCounter() > 0 && stopLossResume < 100 - cryptoCurrencyStatus.getProfitPercent())) {
            System.out.println("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            double quantity = buyAmount / lastPrice;
            CryptoOrderResponse buyCrypto = apiService.buyCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(quantity), String.valueOf(lastPrice));
            return buyCrypto.getId();
        }
        return null;
    }

    String analyseSell(Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        lastPrice = MathUtil.getAmount(lastPrice, 99.90);
        double sellPercent = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getLastBuyPrice());
        double targetSelPercent = Double.parseDouble("100") + cryptoCurrencyStatus.getProfitPercent();
        System.out.println("Sell Low Percent: " + sellPercent);

        if(midNightPrice < 95) {
            targetSelPercent = targetSelPercent - (cryptoCurrencyStatus.getProfitPercent() / 2);
        }
        stopLossPercent = Double.parseDouble("100") - (cryptoCurrencyStatus.getProfitPercent() * 3);
        if (sellPercent > targetSelPercent || sellPercent < stopLossPercent) {
            System.out.println("Selling Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            CryptoOrderResponse sellCrypto = apiService.sellCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(cryptoCurrencyStatus.getQuantity()), String.valueOf(lastPrice));
            return sellCrypto.getId();
        }

        return null;
    }
}
