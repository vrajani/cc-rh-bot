package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.vrajani.model.CryptoCurrencyStatus;

import java.io.File;
import java.io.IOException;

@Service
public class StateLoadService {

    @Autowired
    private ObjectMapper objectMapper;

    public CryptoCurrencyStatus readState(String symbol) throws IOException {
            return objectMapper.readValue(new File("src/main/resources/status/"+ symbol.toLowerCase()+".json"),
                    CryptoCurrencyStatus.class);
    }

    public void save(CryptoCurrencyStatus currencyStatus) throws IOException {
        objectMapper.writeValue(new File("src/main/resources/status/"+ currencyStatus.getSymbol().toLowerCase()+".json"), currencyStatus);
    }
}