package pl.vrajani.service;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.Datum;
import pl.vrajani.utility.ThreadWait;

import java.util.ArrayList;

@Service
public class ActionService {
    private static Logger LOG = LoggerFactory.getLogger(ActionService.class);

    protected void buy(CryptoCurrencyStatus currencyStatus, WebDriver driver, Double lastBuyPrice){
        driver.findElement(By.partialLinkText(currencyStatus.getSymbol().toUpperCase())).click();
        ThreadWait.waitFor(2000);

        //driver.findElement(By.xpath("//h3[text()='Sell LTC']")).click();
        driver.findElement(By.name("amount")).sendKeys("9.0");
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
        currencyStatus.setBuyTotal(currencyStatus.getBuyTotal() + 9.0);
        currencyStatus.setLastBuyPrice(lastBuyPrice);
    }

    protected void sell(CryptoCurrencyStatus currencyStatus, WebDriver driver, Double lastSellPrice){
        driver.findElement(By.partialLinkText(currencyStatus.getSymbol().toUpperCase())).click();
        ThreadWait.waitFor(2000);

        driver.findElement(By.xpath("//span[text()='Sell "+currencyStatus.getSymbol().toUpperCase()+"']")).click();
        driver.findElement(By.name("amount")).sendKeys("9.06");
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        ThreadWait.waitFor(2000);
        currencyStatus.setShouldBuy(true);
        currencyStatus.setShouldSell(false);
        currencyStatus.setSellTotal(currencyStatus.getSellTotal() + 9.06);
        currencyStatus.setLastSalePrice(lastSellPrice);
    }
}
