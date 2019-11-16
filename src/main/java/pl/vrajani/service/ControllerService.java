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
import pl.vrajani.config.Configuration;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistData;
import pl.vrajani.model.CryptoHistPrice;
import pl.vrajani.model.Datum;
import pl.vrajani.request.APIService;
import pl.vrajani.service.analyse.AnalyseBuy;
import pl.vrajani.service.analyse.AnalyseSell;
import pl.vrajani.utility.MathUtil;
import pl.vrajani.utility.ThreadWait;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ControllerService {
    private static Logger LOG = LoggerFactory.getLogger(ControllerService.class);
    private static final long INTERVAL_RATE = 120000;
    private WebDriver driver;

    @Autowired
    private CryptoDataService cryptoDataService;

    @Autowired
    private APIService apiService;

    @Autowired
    private AnalyseBuy analyseBuy;

    @Autowired
    private AnalyseSell analyseSell;

    @Autowired
    private ConfigRefresher configRefresher;

    @Scheduled(fixedRate = INTERVAL_RATE)
    public void performCheck(){
        LOG.info("Initiating the check::::");

//        synchronized (this){
//            if (driver == null){
//                String path = "src/main/resources/chromedriver";
//                if (System.getenv("OS").equalsIgnoreCase("windows")){
//                    path = path + ".exe";
//                }
//                System.setProperty("webdriver.chrome.driver", path);
//
//                if(System.getenv("headless").equalsIgnoreCase("false")) {
//                    driver = new ChromeDriver();
//                } else {
//                    ChromeOptions options = new ChromeOptions();
//                    options.addArguments("headless");
//                    options.addArguments("window-size=1400x600");
//                    driver = new ChromeDriver(options);
//                }
//                LOG.info("Opening Robinhood home page:::");
//                driver.get("https://robinhood.com/login");
//                ThreadWait.waitFor(3000);
//
//                String username = System.getenv("username");
//                driver.findElement(By.name("username")).sendKeys(username);
//
//                String password = System.getenv("password");
//                driver.findElement(By.name("password")).sendKeys(password);
//
//                driver.findElement(By.xpath("//span[text()='Sign In']")).click();
//                ThreadWait.waitFor(10000);
//                System.setProperty("isStart","true");
//                LOG.info("Opening RH first time.....");
//           } else {
//                LOG.info("RH is already open.");
//            }
//        }

        checkAllCrypto();
    }

    private void checkAllCrypto() {

        if(!isDownTime()) {
            Configuration.CRYPTO.stream().forEach(str -> {
                try {
                    LOG.info("Working with Crypto: " + str);
                    CryptoCurrencyStatus currencyStatus = configRefresher.refresh(str);
                    LOG.info("Crypto Details: "+ currencyStatus.toString());

//                    CryptoHistData crypto50mHistData = cryptoDataService.getHistoricalData(str, "50", "1").getBody();
//                    Double initialPrice = MathUtil.getAmount(crypto50mHistData.getData().get(0).getClose(), 99.50);
                    CryptoHistPrice cryptoHistHourData = apiService.getCryptoHistPriceBySymbol(str, "hour", "5minute");
                    Double initialPrice = Double.valueOf(cryptoHistHourData.getDataPoints().get(0).getClosePrice());


//                    CryptoHistData cryptoHistData = cryptoDataService.getHistoricalData(str, "48", "30").getBody();
//                    Double midNightPrice = MathUtil.getAmount(cryptoHistData.getData().get(0).getClose(), 99.40);
                    CryptoHistPrice cryptoHistDayData = apiService.getCryptoHistPriceBySymbol(str, "day", "hour");
                    Double midNightPrice = Double.valueOf(cryptoHistDayData.getDataPoints().get(0).getClosePrice());

                    //Double lastPrice = MathUtil.getAmount(cryptoDataService.getCurrentPrice(str), 99.50);
                    Double lastPrice = Double.valueOf(apiService.getCryptoPriceBySymbol(str).getMarkPrice());

                    LOG.info("50 Mins avg Value: " + initialPrice);
                    LOG.info("Current Value: " +lastPrice);

                    boolean bought = analyseBuy.analyse(initialPrice, lastPrice, midNightPrice, currencyStatus, driver);
                    if(!bought) {
                        analyseSell.analyse(initialPrice, lastPrice, midNightPrice, currencyStatus, driver);
                    }
                } catch (Exception ex) {
                    LOG.error("Exception occured::: ", ex);
                } finally {
//                    LOG.info("Going back to Home page !!");
//                    driver.findElement(By.linkText("Home")).click();
                    ThreadWait.waitFor(4000);
                }
            });
        }

    }

    private boolean isDownTime() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

        return (currentHour == 16 && currentMinute > 25 )|| (currentHour == 17 && currentMinute < 5 );
    }

}
