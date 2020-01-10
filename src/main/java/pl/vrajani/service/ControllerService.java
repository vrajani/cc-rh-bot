package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.vrajani.config.Configuration;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistPrice;
import pl.vrajani.model.CryptoOrderResponse;
import pl.vrajani.model.CryptoOrderStatusResponse;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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

    @Autowired
    private HashMap<String, CryptoOrderResponse> orderStatus;

    @Scheduled(fixedRate = INTERVAL_RATE)
    public void checkAllCrypto() {
        LOG.info("Initiating the check::::");

        if(!TimeUtil.isDownTime()) {
            for (String str : Configuration.CRYPTO) {
                if (orderStatus.containsKey(str)) {
                    CryptoOrderResponse previousOrder = orderStatus.get(str);
                    CryptoOrderStatusResponse cryptoOrderStatusResponse = apiService.executeCryptoOrderStatus(previousOrder.getId());
                    if(!"filled".equalsIgnoreCase(cryptoOrderStatusResponse.getState())) {
                        LOG.info("Skipping crypto as there is a pending order: {} with order Id: {}", str, previousOrder.getId());
                        continue;
                    }
                }
                try {
                    LOG.info("Working with Crypto: " + str);
                    CryptoCurrencyStatus currencyStatus = refresh(str);
                    LOG.info("Crypto Details: " + currencyStatus.toString());

                    CryptoHistPrice cryptoHistHourData = apiService.getCryptoHistPriceBySymbol(str, "day", "5minute");
                    Double initialPrice = Double.valueOf(cryptoHistHourData.getDataPoints().get(cryptoHistHourData.getDataPoints().size() - 18).getClosePrice());

                    CryptoHistPrice cryptoHistDayData = apiService.getCryptoHistPriceBySymbol(str, "day", "hour");
                    Double midNightPrice = Double.valueOf(cryptoHistDayData.getDataPoints().get(0).getClosePrice());

                    Double lastPrice = Double.valueOf(apiService.getCryptoPriceBySymbol(str).getMarkPrice());

                    LOG.info("1.5 Hour ago Value: " + initialPrice);
                    LOG.info("Current Value: " + lastPrice);

                    CryptoOrderResponse orderResponse = actionService.analyseBuy(initialPrice, lastPrice, midNightPrice, currencyStatus);
                    if (orderResponse == null) {
                        orderResponse = actionService.analyseSell(lastPrice, currencyStatus);
                    }

                    if (orderResponse != null) {
                        orderStatus.put(str, orderResponse);
                    }
                    if (currencyStatus.getStopCounter() > 0) {
                        currencyStatus.setStopCounter(currencyStatus.getStopCounter() - 1);
                        actionService.saveStatus(currencyStatus);
                    }


                } catch (Exception ex) {
                    LOG.error("Exception occured::: ", ex);
                }
            }
        } else {
            LOG.info("It is DownTime. Waiting...");
        }
    }

    private CryptoCurrencyStatus refresh(String str){
        try {
            return objectMapper.readValue(new File("src/main/resources/status/"+ str.toLowerCase()+".json"),
                    CryptoCurrencyStatus.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
