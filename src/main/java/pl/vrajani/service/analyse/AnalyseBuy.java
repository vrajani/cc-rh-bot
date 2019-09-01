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
import java.util.Map;

@Component
public class AnalyseBuy implements Analyser {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyseBuy.class);

    @Autowired
    private ActionService actionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public CryptoCurrencyStatus analyse(Double initialPrice, Double avgPrice, Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus, WebDriver driver) {

        Double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        Double midNightPercent = MathUtil.getPercentAmount(lastPrice, midNightPrice);
        Double avgBuyPercent = MathUtil.getPercentAmount(lastPrice, avgPrice);
        LOG.info("Buy Percent: "+buyPercent);
        LOG.info("Average Buy Percent: "+avgBuyPercent);
        LOG.info("MidNight Percent: "+midNightPercent);

        // High Range
        if( cryptoCurrencyStatus.getHighRange().isPower() && cryptoCurrencyStatus.getHighRange().isShouldBuy()){
            LOG.info("Checking High Range Buying....");
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
        if( cryptoCurrencyStatus.getMediumRange().isPower() && cryptoCurrencyStatus.getMediumRange().isShouldBuy()){
            LOG.info("Checking Medium Range Buying....");
            if (buyPercent < 93.5 && buyPercent > 91.5){
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
        if(cryptoCurrencyStatus.getLowRange().isPower() && cryptoCurrencyStatus.getLowRange().isShouldBuy()){
            LOG.info("Checking Low Range Buying....");

            if (buyPercent < 98.1 || midNightPercent < 96 || avgBuyPercent < 98.5){
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
        if(cryptoCurrencyStatus.getDailyRange().isPower() && cryptoCurrencyStatus.getDailyRange().isShouldBuy()){
            LOG.info("Checking Daily Range Buying....");
            if (buyPercent < 98.51 && buyPercent > 97.5){
                try {
                    LOG.info("Buying Daily Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setDailyRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getDailyRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getDailyRange().getBuyAmount());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cryptoCurrencyStatus;
    }
}
