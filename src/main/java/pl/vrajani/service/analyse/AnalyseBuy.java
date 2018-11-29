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

        if( cryptoCurrencyStatus.isShouldBuy()){
            List<Datum> datumList = cryptoHistData.getData();

            log.info("start Value: "+ datumList.get(0).getClose());
            log.info("Last Value: "+ datumList.get(datumList.size()-1).getClose());
            Double buyPercent = getPercent(datumList.get(datumList.size()-1).getClose(), datumList.get(0).getClose());
            log.info("Buy Percent: "+buyPercent);
            if(buyPercent < 99.03){
                return true;
            }
        }
        return false;
    }
}
