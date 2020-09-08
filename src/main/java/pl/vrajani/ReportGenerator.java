package pl.vrajani;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoStatusBase;
import pl.vrajani.model.DataConfig;
import pl.vrajani.service.DaoService;
import pl.vrajani.utility.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pl.vrajani.BackTest.TOP_K;

public class ReportGenerator implements RequestHandler<Object, String> {
    public final static String SEPARATOR = ",";

    @Override
    public String handleRequest(Object input, Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        DaoService daoService = new DaoService(objectMapper);

        try {
            StringBuilder stringBuilder = new StringBuilder();
            List<CryptoCurrencyStatus> updatedStatuses = new ArrayList<>();
            DataConfig dataConfig = daoService.getDataConfig();
            BackTest backTest = new BackTest(dataConfig.getToken());
            dataConfig.getCryptoCurrencyStatuses()
                .forEach(cryptoCurrencyStatus -> {
                if(cryptoCurrencyStatus.getRegularSell() != 0) {
                    try {
                        List<CryptoCurrencyStatus> cryptoCurrencyStatuses = backTest.processCrypto(cryptoCurrencyStatus.getSymbol());
                        double medianProfitPercent = cryptoCurrencyStatuses.stream().map(CryptoStatusBase::getProfitPercent).sorted().collect(Collectors.toList()).get(TOP_K / 2);
                        double medianBuyPercent = cryptoCurrencyStatuses.stream().map(CryptoStatusBase::getBuyPercent).sorted().collect(Collectors.toList()).get(TOP_K / 2);
                        cryptoCurrencyStatus.setBuyPercent(medianBuyPercent);
                        cryptoCurrencyStatus.setProfitPercent(medianProfitPercent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getReportData(stringBuilder, cryptoCurrencyStatus);
                    updatedStatuses.add(resetCryptoCurrencyStatus(cryptoCurrencyStatus));
                } else {
                    updatedStatuses.add(cryptoCurrencyStatus);
                }



                });

            if(!updatedStatuses.isEmpty()) {
                daoService.registerTransactionReport(stringBuilder.toString());
            }
            dataConfig.setCryptoCurrencyStatuses(updatedStatuses);
            daoService.updateConfig(dataConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Completed Execution";
    }

    public static void getReportData(StringBuilder stringBuilder, CryptoCurrencyStatus cryptoCurrencyStatus) {
        stringBuilder.append(TimeUtil.getCurrentTime())
                .append(SEPARATOR)
                .append(cryptoCurrencyStatus.getCcId())
                .append(SEPARATOR)
                .append(cryptoCurrencyStatus.getProfit())
                .append(SEPARATOR)
                .append(cryptoCurrencyStatus.getRegularSell())
                .append(System.lineSeparator());
    }

    private CryptoCurrencyStatus resetCryptoCurrencyStatus(CryptoCurrencyStatus cryptoCurrencyStatus) {
        cryptoCurrencyStatus.setProfit(0);
        cryptoCurrencyStatus.setRegularSell(0);
        return cryptoCurrencyStatus;
    }
}
