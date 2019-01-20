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

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class AnalyseSell implements Analyser {
    public static Logger log = LoggerFactory.getLogger(AnalyseSell.class);

    @Autowired
    private ActionService actionService;

    @Autowired
    private Map<String, CryptoCurrencyStatus> cryptoCurrencyStatusMap;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void analyse(Double initialPrice, Double lastPrice, CryptoCurrencyStatus cryptoCurrencyStatus, WebDriver driver) {

        Double sellPercent = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getDailyRange().getLastBuyPrice());
        log.info("Sell Percent: " + sellPercent);

        // High Range
        if( !cryptoCurrencyStatus.getHighRange().isShouldBuy()){
            if (sellPercent > 110.0){
                try {
                    log.info("Selling High Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setHighRange(actionService.sell(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getHighRange()));
                    Double sellAmount = ((cryptoCurrencyStatus.getHighRange().getProfitPercent() + 100) * cryptoCurrencyStatus.getHighRange().getBuyAmount())/100;
                    cryptoCurrencyStatus.setSellTotal(cryptoCurrencyStatus.getSellTotal()+sellAmount);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Medium Range
        if( !cryptoCurrencyStatus.getMediumRange().isShouldBuy()){
            if (sellPercent > 105.0 && sellPercent < 110.0){
                try {
                    log.info("Selling Medium Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setMediumRange(actionService.sell(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getMediumRange()));
                    Double sellAmount = ((cryptoCurrencyStatus.getHighRange().getProfitPercent() + 100) * cryptoCurrencyStatus.getHighRange().getBuyAmount())/100;
                    cryptoCurrencyStatus.setSellTotal(cryptoCurrencyStatus.getSellTotal()+sellAmount);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Low Range
        if( !cryptoCurrencyStatus.getLowRange().isShouldBuy()){
            if (sellPercent > 102.0 && sellPercent < 105.0){
                try {
                    log.info("Selling Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setLowRange(actionService.sell(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getLowRange()));
                    Double sellAmount = ((cryptoCurrencyStatus.getHighRange().getProfitPercent() + 100) * cryptoCurrencyStatus.getHighRange().getBuyAmount())/100;
                    cryptoCurrencyStatus.setSellTotal(cryptoCurrencyStatus.getSellTotal()+sellAmount);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Daily Range
        if( !cryptoCurrencyStatus.getDailyRange().isShouldBuy()){
            if (sellPercent > 101.0 && sellPercent < 102.0){
                try {
                    log.info("Selling Daily Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setDailyRange(actionService.sell(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getDailyRange()));
                    Double sellAmount = ((cryptoCurrencyStatus.getHighRange().getProfitPercent() + 100) * cryptoCurrencyStatus.getHighRange().getBuyAmount())/100;
                    cryptoCurrencyStatus.setSellTotal(cryptoCurrencyStatus.getSellTotal()+sellAmount);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        cryptoCurrencyStatusMap.put(cryptoCurrencyStatus.getSymbol().toUpperCase(), cryptoCurrencyStatus);
        //Finally save the new state, for just in case.
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/status/"+ cryptoCurrencyStatus.getSymbol().toLowerCase()+".json"), cryptoCurrencyStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
