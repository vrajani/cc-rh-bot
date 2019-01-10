package pl.vrajani.service.analyse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistData;
import pl.vrajani.model.Datum;
import pl.vrajani.utility.MathUtil;

import java.util.List;

@Component
public class AnalyseSell implements Analyser {
    public static Logger log = LoggerFactory.getLogger(AnalyseSell.class);

    @Override
    public boolean analyse(Double initialPrice, Double lastPrice, CryptoCurrencyStatus cryptoCurrencyStatus) {

        if( cryptoCurrencyStatus.isShouldSell()) {
            Double sellPercent = MathUtil.getPercentAmount(lastPrice, cryptoCurrencyStatus.getLastBuyPrice());
            log.info("Sell Percent: " + sellPercent);

            return sellPercent > 100.80;
        }
        return false;
    }
}
