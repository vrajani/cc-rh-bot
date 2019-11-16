package pl.vrajani.service.analyse;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.model.CryptoCurrencyStatus;

import java.io.File;
import java.io.IOException;

@FunctionalInterface
public interface Analyser {
    boolean analyse(Double initialPrice, Double lastPrice, Double midnightPrice, CryptoCurrencyStatus cryptoCurrencyStatus);
}
