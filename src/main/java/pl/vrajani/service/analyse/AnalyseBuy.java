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
public class AnalyseBuy implements Analyser {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyseBuy.class);

    @Autowired
    private ActionService actionService;

    @Autowired
    private Map<String, CryptoCurrencyStatus> cryptoCurrencyStatusMap;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void analyse(Double initialPrice, Double lastPrice, CryptoCurrencyStatus cryptoCurrencyStatus, WebDriver driver) {

        Double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        LOG.info("Buy Percent: "+buyPercent);

        // High Range
        if( cryptoCurrencyStatus.getHighRange().isShouldBuy()){
            if (buyPercent < 90.0){
                try {
                    LOG.info("Buying High Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setHighRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getHighRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getHighRange().getBuyAmount());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Medium Range
        if( cryptoCurrencyStatus.getMediumRange().isShouldBuy()){
            if (buyPercent < 95.0 && buyPercent > 90.0){
                try {
                    LOG.info("Buying Medium Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setMediumRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getMediumRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getMediumRange().getBuyAmount());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Low Range
        if( cryptoCurrencyStatus.getLowRange().isShouldBuy()){
            if (buyPercent < 98.0 && buyPercent > 95.0){
                try {
                    LOG.info("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setLowRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getLowRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getLowRange().getBuyAmount());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Daily Range
        if( cryptoCurrencyStatus.getDailyRange().isShouldBuy()){
            if (buyPercent < 98.91 && buyPercent > 98.0){
                try {
                    LOG.info("Buying Daily Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setDailyRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getDailyRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getDailyRange().getBuyAmount());
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
