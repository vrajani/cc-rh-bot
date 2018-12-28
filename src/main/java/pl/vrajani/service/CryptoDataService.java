package pl.vrajani.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import pl.vrajani.model.CryptoHistData;

@Controller
public class CryptoDataService {

    public ResponseEntity<CryptoHistData> getHistoricalData(String symbol){
        RestTemplate restTemplate = new RestTemplate();
        return  restTemplate.getForEntity("https://min-api.cryptocompare.com/data/histominute?fsym="+symbol
                        +"&tsym=USD&limit=25&aggregate=1", CryptoHistData.class);
    }

    public ResponseEntity<Object> getCurrentPrice (String symbol){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity("https://min-api.cryptocompare.com/data/pricehistorical?fsym="+symbol
                +"ETH&tsyms=USD", Object.class);
    }
}
