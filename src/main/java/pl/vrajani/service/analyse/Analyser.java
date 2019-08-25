package pl.vrajani.service.analyse;

import org.openqa.selenium.WebDriver;
import pl.vrajani.model.CryptoCurrencyStatus;

@FunctionalInterface
public interface Analyser {
    void analyse(Double initialPrice, Double lastPrice, Double midnightPrice, CryptoCurrencyStatus cryptoCurrencyStatus, WebDriver driver);
}
