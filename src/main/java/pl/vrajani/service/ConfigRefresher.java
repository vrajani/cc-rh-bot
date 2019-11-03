package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.vrajani.model.CryptoCurrencyStatus;

import java.io.File;
import java.io.IOException;

@Service
public class ConfigRefresher {

    @Autowired
    private ObjectMapper objectMapper;

    CryptoCurrencyStatus refresh(String str){
        try {
            return objectMapper.readValue(new File("src/main/resources/status/"+ str.toLowerCase()+".json"),
                    CryptoCurrencyStatus.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}
