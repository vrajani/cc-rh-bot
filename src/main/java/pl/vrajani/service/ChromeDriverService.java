package pl.vrajani.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChromeDriverService {
    Logger log = LoggerFactory.getLogger(ChromeDriverService.class);

    public void openRH(WebDriver driver) {
        try {
            log.info("Opening Robinhood home page:::");
            driver.get("https://robinhood.com/login");

            String username = System.getenv("username");
            driver.findElement(By.name("username")).sendKeys(username);

            String password = System.getenv("password");
            driver.findElement(By.name("password")).sendKeys(password);

            driver.findElement(By.xpath("//span[text()='Sign In']")).click();
            Thread.sleep(5000);

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
