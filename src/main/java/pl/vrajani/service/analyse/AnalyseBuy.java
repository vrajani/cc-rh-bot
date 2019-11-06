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
public class AnalyseBuy implements Analyser {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyseBuy.class);

    @Autowired
    private ActionService actionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean analyse(Double initialPrice, Double avgPrice, Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus, WebDriver driver) {

        Double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        Double midNightPercent = MathUtil.getPercentAmount(lastPrice, midNightPrice);
        Double avgBuyPercent = MathUtil.getPercentAmount(lastPrice, avgPrice);
        LOG.info("Buy Percent: "+buyPercent);
        LOG.info("Average Buy Percent: "+avgBuyPercent);
        LOG.info("MidNight Percent: "+midNightPercent);

        boolean bought = false;

        // Range
        if(cryptoCurrencyStatus.getRange().isPower() && cryptoCurrencyStatus.getRange().isShouldBuy()){
            LOG.info("Checking Low Range Buying....");

            if (buyPercent < 98.4 || avgBuyPercent < 98.7 || midNightPercent < 97.0){
                try {
                    LOG.info("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                    cryptoCurrencyStatus.setRange(actionService.buy(cryptoCurrencyStatus.getSymbol(), driver, lastPrice, cryptoCurrencyStatus.getRange()));
                    cryptoCurrencyStatus.setBuyTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getRange().getBuyAmount());
                    bought = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        saveStatus(cryptoCurrencyStatus, objectMapper);
        return bought;
    }
}
