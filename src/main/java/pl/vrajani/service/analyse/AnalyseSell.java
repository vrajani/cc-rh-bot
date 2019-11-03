package pl.vrajani.service.analyse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.service.ActionService;
import pl.vrajani.utility.MathUtil;

import java.io.IOException;

@Component
public class AnalyseSell implements Analyser {
    public static Logger log = LoggerFactory.getLogger(AnalyseSell.class);

    @Autowired
    private ActionService actionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean analyse(Double initialPrice, Double avgPrice, Double lastPrice, Double migNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus, WebDriver driver) {

        //Range
        boolean sold = false;
        if(cryptoCurrencyStatus.getRange().isPower() && !cryptoCurrencyStatus.getRange().isShouldBuy()){
            Double sellPercent = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getRange().getLastBuyPrice());
            log.info("Sell Low Percent: " + sellPercent);
            if (sellPercent > Double.valueOf("100") + cryptoCurrencyStatus.getRange().getProfitPercent() ){
                try {
                    log.info("Selling Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setRange(actionService.sell(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getRange()));
                    Double sellAmount = cryptoCurrencyStatus.getRange().getBuyAmount();
                    cryptoCurrencyStatus.setSellTotal(cryptoCurrencyStatus.getSellTotal()+sellAmount);
                    sold = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if( sellPercent < Double.valueOf("90")){
                log.info("Down by over 10%, continue to buy: "+ cryptoCurrencyStatus.getSymbol());
                cryptoCurrencyStatus.getRange().setShouldBuy(true);
            }
        }
        saveStatus(cryptoCurrencyStatus, objectMapper);

        return sold;
    }
}
