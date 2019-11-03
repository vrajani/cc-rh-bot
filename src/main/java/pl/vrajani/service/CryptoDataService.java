package pl.vrajani.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import pl.vrajani.model.CryptoHistData;
import pl.vrajani.model.LatestPrice;

@Controller
public class CryptoDataService {

    public ResponseEntity<CryptoHistData> getHistoricalData(String symbol, String limit, String aggregate){
        RestTemplate restTemplate = new RestTemplate();
        return  restTemplate.getForEntity("https://min-api.cryptocompare.com/data/histominute?fsym="+symbol
                        +"&tsym=USD&limit="+ limit +"&aggregate="+aggregate , CryptoHistData.class);
    }

    public Double getCurrentPrice (String symbol){
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity("https://min-api.cryptocompare.com/data/price?fsym="+symbol
                +"&tsyms=USD", LatestPrice.class).getBody().getUSD();
    }
}
