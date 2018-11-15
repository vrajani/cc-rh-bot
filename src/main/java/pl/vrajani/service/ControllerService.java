package pl.vrajani.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.vrajani.model.*;
import pl.vrajani.service.analyse.AnalyseBuy;
import pl.vrajani.service.analyse.AnalyseSell;
import pl.vrajani.utility.ThreadWait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ControllerService {
    private static Logger LOG = LoggerFactory.getLogger(ControllerService.class);

    private static final List<String> CRYPTO = Arrays.asList("LTC","ETC");

    private WebDriver driver;
    private static final long INTERVAL_RATE = 120000;

    @Autowired
    private CryptoDataService cryptoDataService;

    @Autowired
    private ChromeDriverService chromeDriverService;

    @Autowired
    private AnalyseBuy analyseBuy;

    @Autowired
    private AnalyseSell analyseSell;

    @Autowired
    private ActionService actionService;

    @Autowired
    private StateLoadService stateLoadService;

    @Autowired
    private Map<String, CryptoCurrencyStatus> cryptoCurrencyStatusMap;
    private int count = 0;

    @Scheduled(fixedRate = INTERVAL_RATE)
    public void performCheck(){
        LOG.info("Initiating the check::::");

        synchronized (this){
            if (driver == null){
                String path = "src/main/resources/chromedriver";
                if (System.getenv("OS").equalsIgnoreCase("windows")){
                    path = path + ".exe";
                }
                System.setProperty("webdriver.chrome.driver", path);

                driver = new ChromeDriver();
                chromeDriverService.openRH(driver);
                System.setProperty("isStart","true");
                LOG.info("Opening RH first time.....");
           } else {
                LOG.info("RH is already open.");
            }
        }

        LOG.info("Logged into RH");
        checkAllCrypto();
    }

    private void checkAllCrypto() {

        CRYPTO.stream().forEach(str -> {
            try {
                LOG.info("Working with Crypto: " + str);
                CryptoHistData data = cryptoDataService.getHistoricalData(str).getBody();
                LOG.info("Current Price: " + data.getData().get(data.getData().size()-1).getClose());
                checkCryptoWithSymbol(str, data);
            } catch (Exception ex) {
                LOG.error("Exception occured::: ", ex);
            } finally {
                LOG.info("Going back to Home page !!");
                driver.findElement(By.partialLinkText("Home")).click();
            }
        });

    }

    private void sellOrBuy (String str, String action){
        driver.findElement(By.partialLinkText(str)).click();
        ThreadWait.waitFor(3000);
        LOG.info("Reached on crypto page for symbol: " + str);
        if(action.equalsIgnoreCase("buy")){
            actionService.buy(str, driver);
        } else if( action.equalsIgnoreCase("sell")){
            actionService.sell(str, driver);
        }
    }

    private void checkCryptoWithSymbol(String str, CryptoHistData cryptoHistData) throws IOException {
        CryptoCurrencyStatus currencyStatus = cryptoCurrencyStatusMap.get(str);

        LOG.info("Crypto Details: "+ currencyStatus.toString());

        ArrayList<Datum> datum = (ArrayList<Datum>) cryptoHistData.getData();
        if (analyseBuy.analyse(cryptoHistData, currencyStatus)){
            LOG.info("Buying: "+ str + " with price: "+ datum.get(datum.size()-1).getClose());
            driver.findElement(By.partialLinkText(str)).click();
            ThreadWait.waitFor(3000);
            actionService.buy(str, driver);
            currencyStatus.setShouldBuy(false);
            currencyStatus.setShouldSell(true);
            currencyStatus.setLastBuyPrice(getPercentAmount(100.25,datum.get(datum.size()-1).getClose()));
        } else if (analyseSell.analyse(cryptoHistData, currencyStatus)){
            LOG.info("Selling: "+ str + " with price: "+ datum.get(datum.size()-1).getClose());
            driver.findElement(By.partialLinkText(str)).click();
            ThreadWait.waitFor(3000);
            actionService.sell(str, driver);
            currencyStatus.setShouldBuy(true);
            currencyStatus.setShouldSell(false);
            currencyStatus.setLastSalePrice(getPercentAmount(99.75,datum.get(datum.size()-1).getClose()));
        } else {
            LOG.info("Status: No buy or sell!!");
        }

        cryptoCurrencyStatusMap.put(str, currencyStatus);
        //Finally save the new state, for just in case.
        stateLoadService.save(currencyStatus);
    }

    private Double getPercentAmount(double percent, Double currentPrice) {
        return (currentPrice * percent)/100;
    }

}
