package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.Datum;
import pl.vrajani.utility.MathUtil;
import pl.vrajani.utility.ThreadWait;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ActionService {
    private static Logger LOG = LoggerFactory.getLogger(ActionService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Map<String, CryptoCurrencyStatus> cryptoCurrencyStatusMap;

    protected void buy(CryptoCurrencyStatus currencyStatus, WebDriver driver, Double buyPrice) throws IOException {
        driver.findElement(By.partialLinkText(currencyStatus.getSymbol().toUpperCase())).click();
        ThreadWait.waitFor(2000);

        //driver.findElement(By.xpath("//h3[text()='Sell LTC']")).click();
        driver.findElement(By.name("amount")).sendKeys("4.0");
        ThreadWait.waitFor(2000);
        try {
            driver.findElement(By.xpath("//button[@type='submit']")).click();
            ThreadWait.waitFor(2000);
            driver.findElement(By.xpath("//button[@type='submit']")).click();
        } catch (NoSuchElementException exception) {
            LOG.error("Not enough Cash!!!!!");
            return;
        }
        ThreadWait.waitFor(2000);
        currencyStatus.setShouldBuy(false);
        currencyStatus.setShouldSell(true);
        currencyStatus.setBuyTotal(currencyStatus.getBuyTotal() + 4.0);
        currencyStatus.setLastBuyPrice(MathUtil.getAmount(buyPrice, 99.65));

        cryptoCurrencyStatusMap.put(currencyStatus.getSymbol().toUpperCase(), currencyStatus);
        //Finally save the new state, for just in case.
        objectMapper.writeValue(new File("src/main/resources/status/"+ currencyStatus.getSymbol().toLowerCase()+".json"), currencyStatus);
    }

    protected void sell(CryptoCurrencyStatus currencyStatus, WebDriver driver, Double sellPrice) throws IOException {
        driver.findElement(By.partialLinkText(currencyStatus.getSymbol().toUpperCase())).click();
        ThreadWait.waitFor(2000);

        driver.findElement(By.xpath("//span[text()='Sell "+currencyStatus.getSymbol().toUpperCase()+"']")).click();
        driver.findElement(By.name("amount")).sendKeys("4.03");
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        ThreadWait.waitFor(2000);
        currencyStatus.setShouldBuy(true);
        currencyStatus.setShouldSell(false);
        currencyStatus.setSellTotal(currencyStatus.getSellTotal() + 4.03);
        currencyStatus.setLastSalePrice(MathUtil.getAmount(sellPrice, 99.25));
        //Finally save the new state, for just in case.
        cryptoCurrencyStatusMap.put(currencyStatus.getSymbol().toUpperCase(), currencyStatus);

        objectMapper.writeValue(new File("src/main/resources/status/"+ currencyStatus.getSymbol().toLowerCase()+".json"), currencyStatus);

    }
}
