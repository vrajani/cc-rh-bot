package pl.vrajani.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.service.StateLoadService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.context.annotation.Configuration
public class Configuration {

    private static final List<String> CRYPTO = Arrays.asList(new String[]{"LTC","ETC"});

    @Autowired
    private StateLoadService stateLoadService;

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public Map<String, CryptoCurrencyStatus> cryptoCurrencyStatusMap() throws Exception{
        Map<String, CryptoCurrencyStatus> cryptoCurrencyMap = new HashMap<>();
        CRYPTO.parallelStream().forEach(str -> {
            try {
                cryptoCurrencyMap.put(str,stateLoadService.readState(str.toLowerCase()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return cryptoCurrencyMap;
    }
}
