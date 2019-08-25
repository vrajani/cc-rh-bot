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
    public void analyse(Double initialPrice, Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus, WebDriver driver) {

        Double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        Double midNightPercent = MathUtil.getPercentAmount(lastPrice, midNightPrice);
        LOG.info("Buy Percent: "+buyPercent);
        LOG.info("MidNight Percent: "+midNightPercent);

        // High Range
        if( cryptoCurrencyStatus.getHighRange().isPower() && cryptoCurrencyStatus.getHighRange().isShouldBuy()){
            LOG.info("Checking High Range Buying....");
            if (buyPercent < 90.0){
                try {
                    LOG.info("Buying High Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setHighRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getHighRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getHighRange().getBuyAmount());
                    saveStatus(cryptoCurrencyStatus);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Medium Range
        if( cryptoCurrencyStatus.getMediumRange().isPower() && cryptoCurrencyStatus.getMediumRange().isShouldBuy()){
            LOG.info("Checking Medium Range Buying....");
            if (buyPercent < 93.5 && buyPercent > 91.5){
                try {
                    LOG.info("Buying Medium Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setMediumRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getMediumRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getMediumRange().getBuyAmount());
                    saveStatus(cryptoCurrencyStatus);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Low Range
        if(cryptoCurrencyStatus.getLowRange().isPower() && cryptoCurrencyStatus.getLowRange().isShouldBuy()){
            LOG.info("Checking Low Range Buying....");

            if (buyPercent < 98 || midNightPercent < 96){
                try {
                    LOG.info("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setLowRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getLowRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getLowRange().getBuyAmount());
                    saveStatus(cryptoCurrencyStatus);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Daily Range
        if(cryptoCurrencyStatus.getDailyRange().isPower() && cryptoCurrencyStatus.getDailyRange().isShouldBuy()){
            LOG.info("Checking Daily Range Buying....");
            if (buyPercent < 98.51 && buyPercent > 97.5){
                try {
                    LOG.info("Buying Daily Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setDailyRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getDailyRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getDailyRange().getBuyAmount());
                    saveStatus(cryptoCurrencyStatus);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void saveStatus(CryptoCurrencyStatus cryptoCurrencyStatus){
        cryptoCurrencyStatusMap.put(cryptoCurrencyStatus.getSymbol().toUpperCase(), cryptoCurrencyStatus);
        //Finally save the new state, for just in case.
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/status/"+ cryptoCurrencyStatus.getSymbol().toLowerCase()+".json"), cryptoCurrencyStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
