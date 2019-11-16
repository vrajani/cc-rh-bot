package pl.vrajani.service.analyse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.service.ActionService;
import pl.vrajani.utility.MathUtil;

@Component
public class AnalyseSell implements Analyser {
    public static Logger log = LoggerFactory.getLogger(AnalyseSell.class);

    @Autowired
    private ActionService actionService;

    @Override
    public boolean analyse(Double initialPrice, Double lastPrice, Double migNightPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        //Range
        boolean sold = false;
        if(cryptoCurrencyStatus.getRange().isPower() && !cryptoCurrencyStatus.getRange().isShouldBuy()){
            lastPrice = MathUtil.getAmount(lastPrice, 99.50);
            Double sellPercent = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getRange().getLastBuyPrice());
            log.info("Sell Low Percent: " + sellPercent);
            if (sellPercent > Double.valueOf("100") + cryptoCurrencyStatus.getRange().getProfitPercent() ){
                log.info("Selling Low Range: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
                sold = actionService.sell(cryptoCurrencyStatus, lastPrice);
            } else if( sellPercent < Double.valueOf("90")){
                log.info("Down by over 10%, continue to buy: "+ cryptoCurrencyStatus.getSymbol());
                cryptoCurrencyStatus.getRange().setShouldBuy(true);
            }
        }

        return sold;
    }
}
