package pl.vrajani.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import pl.vrajani.request.APIService;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@org.springframework.context.annotation.Configuration
public class Configuration {

    public static final List<String> CRYPTO = Arrays.asList("LTC","BTC","ETH","BCH","BSV");

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public APIService apiService() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("username", System.getenv("username"));
        properties.put("password", System.getenv("password"));
        properties.put("grant_type", "password");
        properties.put("client_id", System.getenv("client_id"));
        properties.put("account_id", System.getenv("account_id"));

        return new APIService(properties);
    }
}
