package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoOrderResponse;
import pl.vrajani.model.TransactionUpdate;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.MathUtil;

import java.io.File;
import java.io.IOException;

public class ActionService {
    private static final String STOP_LOSS_PERCENT = "95";

    private APIService apiService;
    private ObjectMapper objectMapper;

    public ActionService(APIService apiService, ObjectMapper objectMapper) {
        this.apiService = apiService;
        this.objectMapper = objectMapper;
    }

    TransactionUpdate analyseBuy(Double initialPrice, Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        lastPrice = MathUtil.getAmount(lastPrice, 100.12);
        Double stopLossResume = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getLastSalePrice());
        Double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        Double midNightPercent = MathUtil.getPercentAmount(lastPrice, midNightPrice);
        System.out.println("Buy Percent: "+ buyPercent);
        System.out.println("MidNight Percent: "+ midNightPercent);

        TransactionUpdate bought = null;

        // Range
        System.out.println("Checking Low Range Buying....");
        double targetBuyPercent = Double.parseDouble("100") - cryptoCurrencyStatus.getProfitPercent();
        if(midNightPercent < 95) {
            targetBuyPercent = targetBuyPercent - cryptoCurrencyStatus.getProfitPercent();
        }
        if(cryptoCurrencyStatus.getStopCounter() <= 0 && (buyPercent < targetBuyPercent || midNightPercent < 96)){
            System.out.println("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            bought = buy(cryptoCurrencyStatus, lastPrice);
            if(midNightPercent < 96) {
                cryptoCurrencyStatus.setStopCounter(180);
            }
        } else if (cryptoCurrencyStatus.getStopCounter() > 0 && stopLossResume < Double.parseDouble(STOP_LOSS_PERCENT) - cryptoCurrencyStatus.getProfitPercent()){
            System.out.println("Buying Stop loss resume: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            bought = buy(cryptoCurrencyStatus, lastPrice);
        }
        return bought;
    }

    private TransactionUpdate buy(CryptoCurrencyStatus cryptoCurrencyStatus, Double buyPrice) {
        double v = cryptoCurrencyStatus.getBuyAmount() / buyPrice;
        CryptoOrderResponse buyCrypto = apiService.buyCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(v), String.valueOf(buyPrice));
        if(buyCrypto != null) {
            cryptoCurrencyStatus.setShouldBuy(false);
            cryptoCurrencyStatus.setLastBuyPrice(buyPrice);

            return new TransactionUpdate(cryptoCurrencyStatus.getSymbol(), cryptoCurrencyStatus, buyCrypto.getId());
        }
        return null;
    }

    TransactionUpdate analyseSell(Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        //Range
        lastPrice = MathUtil.getAmount(lastPrice, 99.88);
        TransactionUpdate sold = null;
        Double sellPercent = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getLastBuyPrice());
        System.out.println("Sell Low Percent: " + sellPercent);
        double targetSelPercent = Double.parseDouble("100") + cryptoCurrencyStatus.getProfitPercent();

        if(midNightPrice < 95) {
            targetSelPercent = targetSelPercent - (cryptoCurrencyStatus.getProfitPercent() / 2);
        }
        if (sellPercent > targetSelPercent){
            System.out.println("Selling Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            sold = sell(cryptoCurrencyStatus, lastPrice, false);
            cryptoCurrencyStatus.setStopCounter(0);
        } else if(sellPercent < Double.parseDouble(STOP_LOSS_PERCENT)) {
            System.out.println("Selling for Stop loss "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            cryptoCurrencyStatus.setStopCounter(120);
            sold = sell(cryptoCurrencyStatus, lastPrice, true);
        }

        return sold;
    }

    private TransactionUpdate sell(CryptoCurrencyStatus cryptoCurrencyStatus, Double sellPrice, boolean stopLoss) {
        double sellAmount = cryptoCurrencyStatus.getBuyAmount();
        double stopLossPercent = Double.parseDouble("100") - (3 * cryptoCurrencyStatus.getProfitPercent());

        if(stopLoss){
            sellAmount = MathUtil.getAmount(sellAmount, stopLossPercent);
            cryptoCurrencyStatus.incStopLossSell();
        } else {
            cryptoCurrencyStatus.incRegularSell();
        }
        double v = sellAmount / sellPrice;
        CryptoOrderResponse sellCrypto = apiService.sellCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(v), String.valueOf(sellPrice));
        if(sellCrypto != null) {
            cryptoCurrencyStatus.setShouldBuy(true);
            cryptoCurrencyStatus.setLastSalePrice(sellPrice);

            return new TransactionUpdate(cryptoCurrencyStatus.getSymbol(), cryptoCurrencyStatus, sellCrypto.getId());
        }

        return null;
    }
}
