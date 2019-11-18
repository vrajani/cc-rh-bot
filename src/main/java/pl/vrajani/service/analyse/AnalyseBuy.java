package pl.vrajani.service.analyse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.service.ActionService;
import pl.vrajani.utility.MathUtil;

@Component
public class AnalyseBuy implements Analyser {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyseBuy.class);

    @Autowired
    private ActionService actionService;

    @Override
    public boolean analyse(Double initialPrice, Double lastPrice, Double midNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        Double stopLossResume = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getRange().getLastSalePrice());
        Double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
        Double midNightPercent = MathUtil.getPercentAmount(lastPrice, midNightPrice);
        LOG.info("Buy Percent: "+buyPercent);
        LOG.info("MidNight Percent: "+midNightPercent);

        boolean bought = false;

        // Range
        if(cryptoCurrencyStatus.getRange().isPower() && cryptoCurrencyStatus.getRange().isShouldBuy()){
            LOG.info("Checking Low Range Buying....");
            if(cryptoCurrencyStatus.getStopCounter() <= 0 && (buyPercent < 98.4 || midNightPercent < 97.0)){
                LOG.info("Buying Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                bought = actionService.buy(cryptoCurrencyStatus, lastPrice);
            } else if (cryptoCurrencyStatus.getStopCounter() > 0 && stopLossResume < 98.5){
                LOG.info("Buying Stop loss resume: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                bought = actionService.buy(cryptoCurrencyStatus, lastPrice);
            }
        }
        return bought;
    }
}
