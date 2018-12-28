package pl.vrajani.service.analyse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistData;
import pl.vrajani.model.Datum;

import java.util.List;

@Component
public class AnalyseBuy implements Analyser {
    public static Logger log = LoggerFactory.getLogger(AnalyseBuy.class);

    @Override
    public boolean analyse(CryptoHistData cryptoHistData, CryptoCurrencyStatus cryptoCurrencyStatus) {

        if( cryptoCurrencyStatus.isShouldBuy() && cryptoCurrencyStatus.getWaitCounter() == 0){
            List<Datum> datumList = cryptoHistData.getData();

            Double buyPercent = getPercent(datumList.get(datumList.size()-1).getClose(), datumList.get(0).getClose());
            log.info("Buy Percent: "+buyPercent);
            return buyPercent < 98.46;
        } else if(cryptoCurrencyStatus.getWaitCounter() > 0){
            cryptoCurrencyStatus.setWaitCounter(cryptoCurrencyStatus.getWaitCounter()-1);
        }
        return false;
    }
}
