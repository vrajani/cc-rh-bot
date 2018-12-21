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

            log.info("start Value: " + datumList.get(0).getClose());
            log.info("Last Value: " + datumList.get(datumList.size() - 1).getClose());
            Double sellPercent = getPercent(datumList.get(datumList.size() - 1).getClose(), cryptoCurrencyStatus.getLastBuyPrice());
            log.info("Sell Percent: " + sellPercent);

            if(cryptoCurrencyStatus.getSymbol().equalsIgnoreCase("etc")) {
                return sellPercent > 101.75 || (getPercent(datumList.get(datumList.size() - 1).getClose(),
                        datumList.get(0).getClose()) > 100.70 && sellPercent > 100.85);
            }
            if(sellPercent > 101.35 || (getPercent(datumList.get(datumList.size() - 1).getClose(),
                    datumList.get(0).getClose()) > 100.70 && sellPercent > 100.85)){
                return true;
            }
            if(sellPercent < 95){
                log.info("TEST::: Selling to reduce Loss at sell percent: " + sellPercent);
                cryptoCurrencyStatus.setWaitCounter(5);
                return true;
            }
        }
        return false;
    }
}
