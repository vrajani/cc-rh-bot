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
import pl.vrajani.model.Datum;
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
    private AnalyseBuy analyseBuy;

    @Autowired
    private AnalyseSell analyseSell;

    @Autowired
    private ConfigRefresher configRefresher;

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
                ThreadWait.waitFor(10000);
                System.setProperty("isStart","true");
                LOG.info("Opening RH first time.....");
           } else {
                LOG.info("RH is already open.");
            }
        }

        checkAllCrypto();
    }

    private void checkAllCrypto() {

        if(!isDownTime()) {
            Configuration.CRYPTO.stream().forEach(str -> {
                try {
                    LOG.info("Working with Crypto: " + str);
                    CryptoCurrencyStatus currencyStatus = configRefresher.refresh(str);
                    LOG.info("Crypto Details: "+ currencyStatus.toString());

                    CryptoHistData crypto50mHistData = cryptoDataService.getHistoricalData(str, "50", "1").getBody();
                    Double midNightPrice = getMidNightPrice(str);

                    Double initialPrice = MathUtil.getAmount(crypto50mHistData.getData().get(0).getClose(), 99.50);
                    Double avgPrice = getESTAvgPrice(crypto50mHistData);
                    Double lastPrice = MathUtil.getAmount(crypto50mHistData.getData().get(49).getClose(), 99.50);
                    LOG.info("50 Mins avg Value: " + initialPrice);
                    LOG.info("Current Value: " +lastPrice);

                    currencyStatus = analyseBuy.analyse(initialPrice, avgPrice, lastPrice, midNightPrice, currencyStatus, driver);
                    currencyStatus = analyseSell.analyse(initialPrice, avgPrice, lastPrice, midNightPrice, currencyStatus, driver);
                    configRefresher.saveStatus(currencyStatus);
                } catch (Exception ex) {
                    LOG.error("Exception occured::: ", ex);
                } finally {
                    LOG.info("Going back to Home page !!");
                    driver.findElement(By.linkText("Home")).click();
                    ThreadWait.waitFor(4000);
                }
            });
        }

    }

    private Double getMidNightPrice(String str) {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int limit = currentHour * 2;
        if(currentMinute > 30){
            limit += 1;
        }
        CryptoHistData cryptoHistData = cryptoDataService.getHistoricalData(str, String.valueOf(limit), "30").getBody();

        return MathUtil.getAmount(cryptoHistData.getData().get(0).getClose(), 99.40);
    }

    private Double getESTAvgPrice(CryptoHistData cryptoHistData) {
        List<Datum> datumList = cryptoHistData.getData();
        AtomicReference<Double> totalValue = new AtomicReference<>(0D);
        AtomicInteger count = new AtomicInteger(0);
        datumList.parallelStream().forEach(datum -> {
            totalValue.updateAndGet(v -> v + datum.getClose());
            count.getAndIncrement();
        });


        double initialPrice = totalValue.get()/count.doubleValue();
        return MathUtil.getAmount(initialPrice, 99.50);
    }

    private boolean isDownTime() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);

        return (currentHour == 16 && currentMinute > 25 )|| (currentHour == 17 && currentMinute < 5 );
    }

}
