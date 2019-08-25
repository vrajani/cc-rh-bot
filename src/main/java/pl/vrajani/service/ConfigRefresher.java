package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.vrajani.config.Configuration;
import pl.vrajani.model.CryptoCurrencyStatus;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigRefresher {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Map<String, CryptoCurrencyStatus> cryptoCurrencyStatusMap;

    void refresh(){
        Map<String, CryptoCurrencyStatus> cryptoCurrencyMap = new HashMap<>();
        Configuration.CRYPTO.stream().forEach(str -> {
            try {
                cryptoCurrencyMap.put(str,objectMapper.readValue(new File("src/main/resources/status/"+ str.toLowerCase()+".json"),
                        CryptoCurrencyStatus.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.cryptoCurrencyStatusMap = cryptoCurrencyMap;
    }

}
