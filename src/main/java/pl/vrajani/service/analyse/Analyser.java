package pl.vrajani.service.analyse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.WebDriver;
import pl.vrajani.model.CryptoCurrencyStatus;

import java.io.File;
import java.io.IOException;

@FunctionalInterface
public interface Analyser {
    boolean analyse(Double initialPrice, Double avgPrice, Double lastPrice, Double midnightPrice, CryptoCurrencyStatus cryptoCurrencyStatus, WebDriver driver);

    default void saveStatus(CryptoCurrencyStatus cryptoCurrencyStatus, ObjectMapper objectMapper){
        //Finally save the new state, for just in case.
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/status/"+ cryptoCurrencyStatus.getSymbol().toLowerCase()+".json"), cryptoCurrencyStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
