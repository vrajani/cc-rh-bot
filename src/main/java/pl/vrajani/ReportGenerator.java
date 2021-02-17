package pl.vrajani;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.DataConfig;
import pl.vrajani.model.StopLossConfigBase;
import pl.vrajani.service.DaoService;
import pl.vrajani.utility.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator implements RequestHandler<Object, String> {
    public final static String SEPARATOR = ",";

    @Override
    public String handleRequest(Object input, Context context) {
        DaoService daoService = new DaoService();

        try {
            StringBuilder stringBuilder = new StringBuilder();

            StopLossConfigBase stopLossConfig = daoService.getStopLossConfig();
            Map<String, Double> stopLossProfits = stopLossConfig.getProfits();
            if(!stopLossProfits.isEmpty()) {
                stopLossProfits.forEach((k , v) -> {
                    if(v > 0.0){
                        CryptoCurrencyStatus cryptoCurrencyStatus = new CryptoCurrencyStatus();
                        cryptoCurrencyStatus.setCcId(k);
                        cryptoCurrencyStatus.setProfit(v);
                        cryptoCurrencyStatus.setRegularSell(0);
                        getReportData(stringBuilder, cryptoCurrencyStatus);
                    }
                });
            }

            List<CryptoCurrencyStatus> updatedStatuses = new ArrayList<>();
            DataConfig dataConfig = daoService.getMainConfig();
            dataConfig.getCryptoCurrencyStatuses()
                    .forEach(cryptoCurrencyStatus -> {
                        if(cryptoCurrencyStatus.getRegularSell() != 0) {
                            getReportData(stringBuilder, cryptoCurrencyStatus);
                        }
                        updatedStatuses.add(resetCryptoCurrencyStatus(cryptoCurrencyStatus));
                    });

            if(stringBuilder.length() > 0) {
                dataConfig.setCryptoCurrencyStatuses(updatedStatuses);
                daoService.updateMainConfig(dataConfig);
                daoService.registerTransactionReport(stringBuilder.toString());

                stopLossConfig.setProfits(new HashMap<>());
                daoService.updateStoplossConfig(stopLossConfig);
            }
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
