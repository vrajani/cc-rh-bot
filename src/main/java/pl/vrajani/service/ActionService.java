package pl.vrajani.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.utility.ThreadWait;

@Service
public class ActionService {

    protected void buy(CryptoCurrencyStatus currencyStatus, WebDriver driver){
        driver.findElement(By.partialLinkText(currencyStatus.getSymbol().toUpperCase())).click();
        ThreadWait.waitFor(2000);

        //driver.findElement(By.xpath("//h3[text()='Sell LTC']")).click();
        driver.findElement(By.name("amount")).sendKeys("6.0");
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        ThreadWait.waitFor(2000);
        currencyStatus.setShouldBuy(false);
        currencyStatus.setShouldSell(true);
        currencyStatus.setBuyTotal(currencyStatus.getBuyTotal() + 6.0);
    }

    protected void sell(CryptoCurrencyStatus currencyStatus, WebDriver driver){
        driver.findElement(By.partialLinkText(currencyStatus.getSymbol().toUpperCase())).click();
        ThreadWait.waitFor(2000);

        driver.findElement(By.xpath("//span[text()='Sell "+currencyStatus.getSymbol().toUpperCase()+"']")).click();
        driver.findElement(By.name("amount")).sendKeys("6.03");
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        ThreadWait.waitFor(2000);
        currencyStatus.setShouldBuy(true);
        currencyStatus.setShouldSell(false);
        currencyStatus.setSellTotal(currencyStatus.getSellTotal() + 6.03);

    }
}
