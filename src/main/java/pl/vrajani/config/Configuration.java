package pl.vrajani.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import pl.vrajani.model.CryptoCurrencyStatus;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.context.annotation.Configuration
public class Configuration {

    public static final List<String> CRYPTO = Arrays.asList("LTC","BTC","ETH","BCH");

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public Map<String, CryptoCurrencyStatus> cryptoCurrencyStatusMap(ObjectMapper objectMapper) throws Exception{
        Map<String, CryptoCurrencyStatus> cryptoCurrencyMap = new HashMap<>();
        CRYPTO.stream().forEach(str -> {
            try {
                cryptoCurrencyMap.put(str,objectMapper.readValue(new File("src/main/resources/status/"+ str.toLowerCase()+".json"),
                        CryptoCurrencyStatus.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return cryptoCurrencyMap;
    }
}
