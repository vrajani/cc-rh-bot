package pl.vrajani.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.vrajani.model.ActionConfig;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.request.APIService;

import java.io.File;
import java.io.IOException;

@Service
public class ActionService {

    @Autowired
    private APIService apiService;

    @Autowired
    private ObjectMapper objectMapper;

    public Boolean buy(CryptoCurrencyStatus cryptoCurrencyStatus, Double buyPrice) {
        ActionConfig actionConfig = cryptoCurrencyStatus.getRange();
        double v = actionConfig.getBuyAmount() / buyPrice;
        Boolean buyCrypto = apiService.buyCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(v), String.valueOf(buyPrice));
        if(buyCrypto) {
            actionConfig.setShouldBuy(false);
            actionConfig.setLastBuyPrice(buyPrice);

            cryptoCurrencyStatus.setSellTotal(cryptoCurrencyStatus.getBuyTotal() + cryptoCurrencyStatus.getRange().getBuyAmount());
            cryptoCurrencyStatus.setRange(actionConfig);
            saveStatus(cryptoCurrencyStatus);
        }
        return buyCrypto;
    }

    public boolean sell(CryptoCurrencyStatus cryptoCurrencyStatus, Double sellPrice) {
        ActionConfig actionConfig = cryptoCurrencyStatus.getRange();
        double v = actionConfig.getBuyAmount() / sellPrice;
        Boolean sellCrypto = apiService.sellCrypto(cryptoCurrencyStatus.getSymbol(), String.valueOf(v), String.valueOf(sellPrice));
        if(sellCrypto) {
            actionConfig.setShouldBuy(true);
            actionConfig.setLastSalePrice(sellPrice);

            Double sellAmount = cryptoCurrencyStatus.getRange().getBuyAmount();
            cryptoCurrencyStatus.setSellTotal(cryptoCurrencyStatus.getSellTotal() + sellAmount);
            cryptoCurrencyStatus.setRange(actionConfig);
            saveStatus(cryptoCurrencyStatus);
        }

        return sellCrypto;
    }

    void saveStatus(CryptoCurrencyStatus cryptoCurrencyStatus){
        //Finally save the new state, for just in case.
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/status/"+ cryptoCurrencyStatus.getSymbol().toLowerCase()+".json"), cryptoCurrencyStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
