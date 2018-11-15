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

        if( cryptoCurrencyStatus.isShouldSell()){
            List<Datum> datumList = cryptoHistData.getData();

            log.info("start Value: "+ datumList.get(0).getClose());
            log.info("Last Value: "+ datumList.get(datumList.size()-1).getClose());
            Double sellPercent = getPercent(datumList.get(datumList.size()-1).getClose(), cryptoCurrencyStatus.getLastBuyPrice());
            log.info("Sell Percent: "+sellPercent);
            if(sellPercent > 101.10){
                return true;
            }
            if(getPercent(datumList.get(datumList.size()-1).getClose(), datumList.get(0).getClose()) > 100.90 &&  sellPercent> 100.98){
                return true;
            }
        }
        return false;

    }
}
