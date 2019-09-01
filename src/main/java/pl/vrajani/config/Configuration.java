package pl.vrajani.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

@org.springframework.context.annotation.Configuration
public class Configuration {

    public static final List<String> CRYPTO = Arrays.asList("LTC","BTC","ETH","BCH","BSV");

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}
