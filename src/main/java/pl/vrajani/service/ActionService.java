package pl.vrajani.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;
import pl.vrajani.utility.ThreadWait;

@Service
public class ActionService {

    protected void buy(String symbol, WebDriver driver){
        ThreadWait.waitFor(2000);
        //driver.findElement(By.xpath("//h3[text()='Sell LTC']")).click();
        driver.findElement(By.name("amount")).sendKeys("7.0");
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
    }

    protected void sell(String symbol, WebDriver driver){
        ThreadWait.waitFor(2000);//
        driver.findElement(By.xpath("//span[text()='Sell "+symbol+"']")).click();
        driver.findElement(By.name("amount")).sendKeys("7.00");
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        ThreadWait.waitFor(2000);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
    }
}
