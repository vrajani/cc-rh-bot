package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.vrajani.model.ActionConfig;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.utility.MathUtil;
import pl.vrajani.utility.ThreadWait;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ActionService {
    private static Logger LOG = LoggerFactory.getLogger(ActionService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Map<String, CryptoCurrencyStatus> cryptoCurrencyStatusMap;

    public static void main(String[] args) throws IOException {
        ActionService actionService = new ActionService();
        actionService.objectMapper = new ObjectMapper();

        CryptoCurrencyStatus currencyStatus1 = new CryptoCurrencyStatus();
        currencyStatus1.setHighRange(new ActionConfig());
        currencyStatus1.setMediumRange(new ActionConfig());
        currencyStatus1.setLowRange(new ActionConfig());
        ActionConfig dailyConfig = new ActionConfig();
        dailyConfig.setLastBuyPrice(31.708729650000006);
        dailyConfig.setLastSalePrice(32.480108375);
        dailyConfig.setShouldBuy(true);
        dailyConfig.setProfitPercent(1.0);
        dailyConfig.setBuyAmount(5.0);
        currencyStatus1.setDailyRange(dailyConfig);
        currencyStatus1.setSymbol("LTC");
        currencyStatus1.setBuyTotal(0.0);
        currencyStatus1.setSellTotal(0.0);
        actionService.objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/status/ltc.json"), currencyStatus1);
    }

    public ActionConfig buy(String symbol, WebDriver driver, Double buyPrice, ActionConfig actionConfig) throws IOException {
        driver.findElement(By.partialLinkText(symbol.toUpperCase())).click();
        ThreadWait.waitFor(2000);

        //driver.findElement(By.xpath("//h3[text()='Sell LTC']")).click();
        driver.findElement(By.name("amount")).sendKeys(actionConfig.getBuyAmount().toString());
        ThreadWait.waitFor(2000);
        try {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            ThreadWait.waitFor(2000);
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (NoSuchElementException exception) {
            LOG.error("Not enough Cash!!!!!");
            return actionConfig;
        }
        ThreadWait.waitFor(2000);
        actionConfig.setShouldBuy(false);
        actionConfig.setLastBuyPrice(MathUtil.getAmount(buyPrice, 99.65));
        return actionConfig;
    }

    public ActionConfig sell(String symbol, WebDriver driver, Double sellPrice, ActionConfig actionConfig) throws IOException {
        driver.findElement(By.partialLinkText(symbol.toUpperCase())).click();
        ThreadWait.waitFor(2000);

        driver.findElement(By.xpath("//span[text()='Sell "+symbol.toUpperCase()+"']")).click();
        Double sellPriceToEnter = ((100 + actionConfig.getProfitPercent()) * actionConfig.getBuyAmount())/100;
        driver.findElement(By.name("amount")).sendKeys(sellPriceToEnter.toString());
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        ThreadWait.waitFor(2000);
        actionConfig.setShouldBuy(true);
        actionConfig.setLastSalePrice(MathUtil.getAmount(sellPrice, 99.25));
        return actionConfig;
    }
}
