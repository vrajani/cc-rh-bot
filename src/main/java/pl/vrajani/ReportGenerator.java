package pl.vrajani;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.DataConfig;
import pl.vrajani.service.DaoService;
import pl.vrajani.utility.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportGenerator implements RequestHandler<Object, String> {
    private final static String SEPARATOR = ",";

    @Override
    public String handleRequest(Object input, Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        DaoService daoService = new DaoService(objectMapper);

        try {
            StringBuilder stringBuilder = new StringBuilder();
            List<CryptoCurrencyStatus> updatedStatuses = new ArrayList<>();
            DataConfig dataConfig = daoService.getDataConfig();
            dataConfig.getCryptoCurrencyStatuses()
                .forEach(cryptoCurrencyStatus -> {
                if(cryptoCurrencyStatus.getStopLossSell() != 0 || cryptoCurrencyStatus.getRegularSell() != 0) {
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
                .append(cryptoCurrencyStatus.getSymbol())
                .append(SEPARATOR)
                .append(cryptoCurrencyStatus.getProfit())
                .append(SEPARATOR)
                .append(cryptoCurrencyStatus.getRegularSell())
                .append(SEPARATOR)
                .append(cryptoCurrencyStatus.getStopLossSell())
                .append(System.lineSeparator());
    }

    private CryptoCurrencyStatus resetCryptoCurrencyStatus(CryptoCurrencyStatus cryptoCurrencyStatus) {
        cryptoCurrencyStatus.setProfit(0);
        cryptoCurrencyStatus.setRegularSell(0);
        cryptoCurrencyStatus.setStopLossSell(0);
        return cryptoCurrencyStatus;
    }
}
