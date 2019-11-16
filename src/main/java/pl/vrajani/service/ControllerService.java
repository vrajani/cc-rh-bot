package pl.vrajani.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.vrajani.config.Configuration;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistPrice;
import pl.vrajani.request.APIService;
import pl.vrajani.service.analyse.AnalyseBuy;
import pl.vrajani.service.analyse.AnalyseSell;
import pl.vrajani.utility.ThreadWait;

import java.util.Calendar;

@Component
public class ControllerService {
    private static Logger LOG = LoggerFactory.getLogger(ControllerService.class);
    private static final long INTERVAL_RATE = 120000;

    @Autowired
    private APIService apiService;

    @Autowired
    private AnalyseBuy analyseBuy;

    @Autowired
    private AnalyseSell analyseSell;

    @Autowired
    private ConfigRefresher configRefresher;

    @Scheduled(fixedRate = INTERVAL_RATE)
    public void checkAllCrypto() {
        LOG.info("Initiating the check::::");

        if(!isDownTime()) {
            Configuration.CRYPTO.stream().forEach(str -> {
                try {
                    LOG.info("Working with Crypto: " + str);
                    CryptoCurrencyStatus currencyStatus = configRefresher.refresh(str);
                    LOG.info("Crypto Details: "+ currencyStatus.toString());

                    CryptoHistPrice cryptoHistHourData = apiService.getCryptoHistPriceBySymbol(str, "hour", "5minute");
                    Double initialPrice = Double.valueOf(cryptoHistHourData.getDataPoints().get(0).getClosePrice());

                    CryptoHistPrice cryptoHistDayData = apiService.getCryptoHistPriceBySymbol(str, "day", "hour");
                    Double midNightPrice = Double.valueOf(cryptoHistDayData.getDataPoints().get(0).getClosePrice());

                    Double lastPrice = Double.valueOf(apiService.getCryptoPriceBySymbol(str).getMarkPrice());

                    LOG.info("Hour ago Value: " + initialPrice);
                    LOG.info("Current Value: " +lastPrice);

                    boolean bought = analyseBuy.analyse(initialPrice, lastPrice, midNightPrice, currencyStatus);
                    if(!bought) {
                        analyseSell.analyse(initialPrice, lastPrice, midNightPrice, currencyStatus);
                    }
                } catch (Exception ex) {
                    LOG.error("Exception occured::: ", ex);
                } finally {
                    ThreadWait.waitFor(4000);
                }
            });
        } else {
            LOG.info("It is DownTime. Waiting...");
        }
    }

    private boolean isDownTime() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

        return (currentHour == 16 && currentMinute > 25 )|| (currentHour == 17 && currentMinute < 5 );
    }

}
