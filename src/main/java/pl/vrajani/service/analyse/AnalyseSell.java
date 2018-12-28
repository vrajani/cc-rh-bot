package pl.vrajani.service.analyse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistData;
import pl.vrajani.model.Datum;

import java.util.List;

@Component
public class AnalyseSell implements Analyser {
    public static Logger log = LoggerFactory.getLogger(AnalyseSell.class);

    @Override
    public boolean analyse(CryptoHistData cryptoHistData, CryptoCurrencyStatus cryptoCurrencyStatus) {

        if( cryptoCurrencyStatus.isShouldSell()) {
            List<Datum> datumList = cryptoHistData.getData();

            Double sellPercent = getPercent(datumList.get(datumList.size() - 1).getClose(), cryptoCurrencyStatus.getLastBuyPrice());
            log.info("Sell Percent: " + sellPercent);

            return sellPercent > 101.25;
//            if(sellPercent < 85){
//                log.info("TEST::: Selling to reduce Loss at sell percent: " + sellPercent);
//                cryptoCurrencyStatus.setWaitCounter(5);
//                return true;
//            }
        }
        return false;
    }
}
