package pl.vrajani.service.analyse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.utility.MathUtil;

@Component
public class AnalyseBuy implements Analyser {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyseBuy.class);

    @Override
    public boolean analyse(Double initialPrice, Double lastPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        if( cryptoCurrencyStatus.isShouldBuy() && cryptoCurrencyStatus.getWaitCounter() == 0){
            Double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);
            LOG.info("Buy Percent: "+buyPercent);
            return buyPercent < 99.21;
        } else if(cryptoCurrencyStatus.getWaitCounter() > 0){
            cryptoCurrencyStatus.setWaitCounter(cryptoCurrencyStatus.getWaitCounter()-1);
        }
        return false;
    }
}
