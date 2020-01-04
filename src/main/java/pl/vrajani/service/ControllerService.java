package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.vrajani.model.CryptoConfig;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistPrice;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ControllerService {
    private static Logger LOG = LoggerFactory.getLogger(ControllerService.class);
    private static final long INTERVAL_RATE = 60000;

    @Autowired
    private APIService apiService;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedRate = INTERVAL_RATE)
    public void checkAllCrypto() {
        LOG.info("Initiating the check::::");

        if(!TimeUtil.isDownTime()) {
            List<String> active = Arrays.asList(System.getenv("active").split(","));
            final AtomicBoolean[] updated = {new AtomicBoolean(false)};
            CryptoConfig cryptoConfig = refresh();
            List<CryptoCurrencyStatus> updatedStatuses = new ArrayList<>();
            Objects.requireNonNull(cryptoConfig).getCryptoCurrencyStatuses().stream()
                    .filter(currencyStatus -> active.contains(currencyStatus.getSymbol()))
                    .forEach(currencyStatus -> {
                        try {
                            String str = currencyStatus.getSymbol();
                            LOG.info("Working with Crypto: " + str);
                            LOG.info("Crypto Details: "+ currencyStatus.toString());

                            CryptoHistPrice cryptoHistHourData = apiService.getCryptoHistPriceBySymbol(str, "day", "5minute");
                            Double initialPrice = Double.valueOf(cryptoHistHourData.getDataPoints().get(cryptoHistHourData.getDataPoints().size()-18).getClosePrice());

                            CryptoHistPrice cryptoHistDayData = apiService.getCryptoHistPriceBySymbol(str, "day", "hour");
                            Double midNightPrice = Double.valueOf(cryptoHistDayData.getDataPoints().get(0).getClosePrice());

                            Double lastPrice = Double.valueOf(apiService.getCryptoPriceBySymbol(str).getMarkPrice());

                            LOG.info("1.5 Hour ago Value: " + initialPrice);
                            LOG.info("Current Value: " +lastPrice);

                            boolean bought = actionService.analyseBuy(initialPrice, lastPrice, midNightPrice, currencyStatus);
                            boolean sold = false;
                            if(!bought) {
                                sold = actionService.analyseSell(lastPrice, currencyStatus);
                            }
                            updated[0].set(bought || sold);
                            if(currencyStatus.getStopCounter() > 0){
                                updated[0].set(true);
                                currencyStatus.setStopCounter(currencyStatus.getStopCounter()-1);
                                actionService.saveStatus(currencyStatus);
                            }
                        } catch (Exception ex) {
                            LOG.error("Exception occured::: ", ex);
                        }
                    });
        } else {
            LOG.info("It is DownTime. Waiting...");
        }
    }

    private CryptoConfig refresh(){
        try {
            return objectMapper.readValue(new File("src/main/resources/status/config.json"),
                    CryptoConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
