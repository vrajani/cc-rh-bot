package pl.vrajani.service.analyse;

import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistData;

@FunctionalInterface
public interface Analyser {
    boolean analyse(Double initialPrice, Double lastPrice, CryptoCurrencyStatus cryptoCurrencyStatus);
}
