package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.vrajani.config.Configuration;
import pl.vrajani.model.*;
import pl.vrajani.service.analyse.AnalyseBuy;
import pl.vrajani.service.analyse.AnalyseSell;
import pl.vrajani.utility.ThreadWait;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

@Component
public class ControllerService {
    private static Logger LOG = LoggerFactory.getLogger(ControllerService.class);
    private static final long INTERVAL_RATE = 100000;

    private WebDriver driver;

    @Autowired
    private CryptoDataService cryptoDataService;

    @Autowired
    private AnalyseBuy analyseBuy;

    @Autowired
    private AnalyseSell analyseSell;

    @Autowired
    private ActionService actionService;

    @Autowired
    private Map<String, CryptoCurrencyStatus> cryptoCurrencyStatusMap;

    @Autowired
    private ObjectMapper objectMapper;

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

                if(System.getenv("headless").equalsIgnoreCase("false")) {
                    driver = new ChromeDriver();
                } else {
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("headless");
                    options.addArguments("window-size=1400x600");
                    driver = new ChromeDriver(options);
                }
                LOG.info("Opening Robinhood home page:::");
                driver.get("https://robinhood.com/login");
                ThreadWait.waitFor(3000);

                String username = System.getenv("username");
                driver.findElement(By.name("username")).sendKeys(username);

                String password = System.getenv("password");
                driver.findElement(By.name("password")).sendKeys(password);

                driver.findElement(By.xpath("//span[text()='Sign In']")).click();
                ThreadWait.waitFor(5000);
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

        if(!isDownTime()) {
            Configuration.CRYPTO.stream().forEach(str -> {
                try {
                    LOG.info("Working with Crypto: " + str);
                    CryptoCurrencyStatus currencyStatus = cryptoCurrencyStatusMap.get(str);
                    LOG.info("Crypto Details: "+ currencyStatus.toString());

                    CryptoHistData cryptoHistData = cryptoDataService.getHistoricalData(str).getBody();
                    ArrayList<Datum> datum = (ArrayList<Datum>) cryptoHistData.getData();
                    LOG.info("25 Mins ago Value: " + datum.get(0).getClose());
                    LOG.info("Current Value: " + datum.get(datum.size() - 1).getClose());

                    Double resetValue = getPercentAmount(currencyStatus.getLastBuyPrice(), datum.get(datum.size()-1).getClose());
                    LOG.info("Reset Data::: "+ str + " with reset value::: "+ resetValue);
                    if(resetValue > 105.0 && !currencyStatus.isShouldBuy()){
                        LOG.info("Resetting::: "+ str + " with reset value::: "+ resetValue);
                        currencyStatus.setShouldBuy(true);
                    }

                    if (analyseBuy.analyse(cryptoHistData, currencyStatus)){
                        LOG.info("Buying: "+ str + " with price: "+ datum.get(datum.size()-1).getClose());
                        actionService.buy(currencyStatus, driver, datum.get(datum.size()-1).getClose());
                    } else if (analyseSell.analyse(cryptoHistData, currencyStatus)){
                        LOG.info("Selling: "+ str + " with price: "+ datum.get(datum.size()-1).getClose());
                        actionService.sell(currencyStatus, driver, datum.get(datum.size()-1).getClose());
                    } else {
                        LOG.info("Status: No buy or sell!!");
                    }

                    cryptoCurrencyStatusMap.put(str, currencyStatus);
                    //Finally save the new state, for just in case.
                    objectMapper.writeValue(new File("src/main/resources/status/"+ str.toLowerCase()+".json"), currencyStatus);
                } catch (Exception ex) {
                    LOG.error("Exception occured::: ", ex);
                } finally {
                    LOG.info("Going back to Home page !!");
                    driver.findElement(By.linkText("Home")).click();
                }
            });
        }

    }

    private boolean isDownTime() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

        return (currentHour == 16 && currentMinute > 25 )|| (currentHour == 17 && currentMinute < 5 );
    }

    private Double getPercentAmount(double source, Double out) {
        return (source * 100)/ out;
    }

}
