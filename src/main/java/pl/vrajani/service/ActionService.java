package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.vrajani.model.ActionConfig;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.MathUtil;
import pl.vrajani.utility.TimeUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ActionService {
    private static final Logger LOG = LoggerFactory.getLogger(ActionService.class);

    private APIService apiService;
    private ObjectMapper objectMapper;
    private BufferedWriter fileWriter;

    public ActionService(APIService apiService, ObjectMapper objectMapper) throws IOException {
        this.apiService = apiService;
        this.objectMapper = objectMapper;
        this.fileWriter = new BufferedWriter(new FileWriter(new File("src/main/resources/status/action-log-bckup.txt"), true));
    }

    boolean analyseBuy(Double initialPrice, Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        lastPrice = MathUtil.getAmount(lastPrice, 100.25);
        Double stopLossResume = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getRange().getLastSalePrice());
        Double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        Double midNightPercent = MathUtil.getPercentAmount(lastPrice, midNightPrice);
        LOG.info("Buy Percent: "+buyPercent);
        LOG.info("MidNight Percent: "+midNightPercent);

        boolean bought = false;

        // Range
        if(cryptoCurrencyStatus.getRange().isPower() && cryptoCurrencyStatus.getRange().isShouldBuy()){
            LOG.info("Checking Low Range Buying....");
            if(cryptoCurrencyStatus.getStopCounter() <= 0 && (buyPercent < 98.64 || midNightPercent < 96)){
                LOG.info("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                bought = buy(cryptoCurrencyStatus, lastPrice);
                if(midNightPercent < 97.5) {
                    cryptoCurrencyStatus.setStopCounter(180);
                }
            } else if (cryptoCurrencyStatus.getStopCounter() > 0 && stopLossResume < 98.5){
                LOG.info("Buying Stop loss resume: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                bought = buy(cryptoCurrencyStatus, lastPrice);
            }
        }
        return bought;
    }

    private Boolean buy(CryptoCurrencyStatus cryptoCurrencyStatus, Double buyPrice) {
        ActionConfig actionConfig = cryptoCurrencyStatus.getRange();
        double v = actionConfig.getBuyAmount() / buyPrice;
        Boolean buyCrypto = apiService.buyCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(v), String.valueOf(buyPrice));
        if(buyCrypto) {
            actionConfig.setShouldBuy(false);
            actionConfig.setLastBuyPrice(buyPrice);

            cryptoCurrencyStatus.setSellTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getRange().getBuyAmount());
            cryptoCurrencyStatus.setRange(actionConfig);
            saveStatus(cryptoCurrencyStatus);
            logAction(cryptoCurrencyStatus.getSymbol(), "buy", buyPrice, v);
        }
        return buyCrypto;
    }

    boolean analyseSell(Double lastPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        //Range
        boolean sold = false;
        if(cryptoCurrencyStatus.getRange().isPower() && !cryptoCurrencyStatus.getRange().isShouldBuy()){
            lastPrice = MathUtil.getAmount(lastPrice, 99.75);
            Double sellPercent = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getRange().getLastBuyPrice());
            LOG.info("Sell Low Percent: " + sellPercent);
            if (sellPercent > Double.valueOf("100") + cryptoCurrencyStatus.getRange().getProfitPercent() ){
                LOG.info("Selling Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                sold = sell(cryptoCurrencyStatus, lastPrice);
                cryptoCurrencyStatus.setStopCounter(0);
            } else if(sellPercent < Double.valueOf("95")) {
                LOG.info("Selling for Stop loss "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                cryptoCurrencyStatus.setStopCounter(120);
                sold = sell(cryptoCurrencyStatus, lastPrice);
            }
        }

        return sold;
    }

    private boolean sell(CryptoCurrencyStatus cryptoCurrencyStatus, Double sellPrice) {
        ActionConfig actionConfig = cryptoCurrencyStatus.getRange();
        double v = actionConfig.getBuyAmount() / sellPrice;
        Boolean sellCrypto = apiService.sellCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(v), String.valueOf(sellPrice));
        if(sellCrypto) {
            actionConfig.setShouldBuy(true);
            actionConfig.setLastSalePrice(sellPrice);

            Double sellAmount = cryptoCurrencyStatus.getRange().getBuyAmount();
            cryptoCurrencyStatus.setSellTotal(cryptoCurrencyStatus.getSellTotal() + sellAmount);
            cryptoCurrencyStatus.setRange(actionConfig);
            saveStatus(cryptoCurrencyStatus);
            logAction(cryptoCurrencyStatus.getSymbol(), "sell", sellPrice, v);
        }

        return sellCrypto;
    }

    private void logAction(String symbol, String type, Double sellAmount, double quantity) {
        String actionLog = null;
        try {
            actionLog = TimeUtil.getCurrentTime() + "," + symbol + "," + type + "," + sellAmount + "," + quantity;
            fileWriter.write(actionLog);
            fileWriter.newLine();
        } catch (IOException e) {
            System.out.println("Error logging the action: " + actionLog);
        }finally {
            try {
                fileWriter.flush();
            } catch (IOException e) {
                System.out.println("Error flushing stream!!!");
            }
        }
    }

    void saveStatus(CryptoCurrencyStatus cryptoCurrencyStatus){
        //Finally save the new state, for just in case.
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/status/"+ cryptoCurrencyStatus.getSymbol().toLowerCase()+".json"), cryptoCurrencyStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
