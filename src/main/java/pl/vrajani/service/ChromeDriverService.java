package pl.vrajani.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.vrajani.utility.ThreadWait;

@Service
public class ChromeDriverService {
    Logger log = LoggerFactory.getLogger(ChromeDriverService.class);

    public void openRH(WebDriver driver) {
        try {
            log.info("Opening Robinhood home page:::");
            driver.get("https://robinhood.com/login");
            ThreadWait.waitFor(3000);

            String username = System.getenv("username");
            driver.findElement(By.name("username")).sendKeys(username);

            String password = System.getenv("password");
            driver.findElement(By.name("password")).sendKeys(password);

            driver.findElement(By.xpath("//span[text()='Sign In']")).click();
            ThreadWait.waitFor(5000);

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
