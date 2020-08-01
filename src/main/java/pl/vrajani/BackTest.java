package pl.vrajani;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.model.*;
import pl.vrajani.service.ActionService;
import pl.vrajani.service.ControllerService;
import pl.vrajani.utility.MathUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BackTest {


    public static void main(String[] args) throws IOException {
        new BackTest().execute();
    }

    private void execute() throws IOException {
        List<Double> profitPercentRange = getProfitPercentRange();
        List<Double> buyPercentRange = getProfitPercentRange();
        List<CryptoCurrencyStatus> results = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String token = System.getenv("token");
        CryptoCurrencyStatus testConfig = getTestConfig(objectMapper);
        String symbol = testConfig.getSymbol();
        CryptoHistPrice cryptoHistPriceBySymbol = Application.getApiService(token).getCryptoHistPriceBySymbol(symbol, "week", "5minute");

        StringBuilder result = new StringBuilder();
        for (Double percent: profitPercentRange) {
            for (Double buyPercent : buyPercentRange) {
                testConfig = getTestConfig(objectMapper);
                testConfig.setProfitPercent(percent);
                testConfig.setBuyPercent(buyPercent);
                CryptoCurrencyStatus resultStatus = runTest(cryptoHistPriceBySymbol, testConfig);
                if (resultStatus != null && resultStatus.getRegularSell() != 0) {
                    results.add(resultStatus);
                }
            }
        }

        results.stream().sorted(Comparator.comparingDouble(CryptoCurrencyStatus::getProfit).reversed()).limit(10).forEach(cryptoCurrencyStatus -> {
            result.append(cryptoCurrencyStatus.getBuyPercent()).append(ReportGenerator.SEPARATOR);
            result.append(cryptoCurrencyStatus.getProfitPercent()).append(ReportGenerator.SEPARATOR);
            ReportGenerator.getReportData(result, cryptoCurrencyStatus);
        });

        System.out.println(result.toString());
    }

    private List<Double> getProfitPercentRange() {
        List<Double> profitPercent = new ArrayList<>();
        double currentPercent = 0.2;
        while(currentPercent <= 1.0) {
            profitPercent.add(currentPercent);
            currentPercent += 0.05;
        }
        while(currentPercent < 9.0) {
            profitPercent.add(currentPercent);
            currentPercent += 0.5;
        }
        return profitPercent;
    }

    private List<Double> getStopLossRange() {
        List<Double> stopLossRange = new ArrayList<>();
        double currentPercent = 2.5;
        while(currentPercent <= 7.0) {
            stopLossRange.add(currentPercent);
            currentPercent += 0.5;
        }
        return stopLossRange;
    }

    private CryptoCurrencyStatus runTest(CryptoHistPrice cryptoHistPriceBySymbol, CryptoCurrencyStatus testConfig) {
        ActionService actionService = new ActionService(null);
        ControllerService  controllerService = new ControllerService(null);
        List<DataPoint> dataPoints = cryptoHistPriceBySymbol.getDataPoints();
        int i = 0;
        for (int j = 0; j < dataPoints.size() - 1; i++) {
            j = i + 6;
            double highPrice = Double.parseDouble(dataPoints.subList(j < 288 ? 0 : i, j).stream().max(Comparator.comparingDouble(dataPoint -> Double.parseDouble(dataPoint.getHighPrice()))).get().getHighPrice());


            System.out.println("Testing for Starting time: " + dataPoints.get(j).toString());
            if (testConfig.isShouldBuy()) {
                CryptoOrderResponse cryptoOrderResponse = actionService.executeBuy(testConfig, dataPoints.subList(i,j), Double.parseDouble(dataPoints.get(j).getClosePrice()), highPrice, false);
                if(cryptoOrderResponse != null) {
                    System.out.println("Buy Order Executed: " + cryptoOrderResponse.toString());
                    testConfig = controllerService.processFilledOrder(testConfig, getDummyCryptoOrderStatusResponse(cryptoOrderResponse), false);
                }
            } else {
                DataPoint currentDataPoint = dataPoints.get(j);
                double targetPrice = MathUtil.getAmount(testConfig.getLastBuyPrice(), 100 + testConfig.getProfitPercent());
                double highPriceOfCurrentDataPoint = MathUtil.getAmount(Double.parseDouble(currentDataPoint.getHighPrice()), 99.75);
                double lowPriceOfCurrentDataPoint = MathUtil.getAmount(Double.parseDouble(currentDataPoint.getLowPrice()), 100.25);

                boolean sell = lowPriceOfCurrentDataPoint < targetPrice && targetPrice < highPriceOfCurrentDataPoint;
                if ( sell) {
                    System.out.println("Sell Order Executed: " + currentDataPoint.toString() + " with targetPrice = " + targetPrice);

                    CryptoOrderStatusResponse cryptoOrderStatusResponse = new CryptoOrderStatusResponse();
                    cryptoOrderStatusResponse.setPrice(String.valueOf(targetPrice));
                    cryptoOrderStatusResponse.setSide("sell");
                    testConfig = controllerService.processFilledOrder(testConfig, cryptoOrderStatusResponse, false);
                }
            }
        }
        System.out.println("Processed iteration count: "+i);
        return testConfig;
    }

    private CryptoOrderStatusResponse getDummyCryptoOrderStatusResponse(CryptoOrderResponse cryptoOrderResponse) {
        CryptoOrderStatusResponse cryptoOrderStatusResponse = new CryptoOrderStatusResponse();
        cryptoOrderStatusResponse.setId(cryptoOrderResponse.getId());
        cryptoOrderStatusResponse.setQuantity(cryptoOrderResponse.getQuantity());
        cryptoOrderStatusResponse.setSide(cryptoOrderResponse.getSide());
        cryptoOrderStatusResponse.setPrice(cryptoOrderResponse.getPrice());
        return cryptoOrderStatusResponse;
    }

    private CryptoCurrencyStatus getTestConfig(ObjectMapper objectMapper) throws IOException {
        try {
            return objectMapper.readValue(new File("src/main/resources/backtest/test.json"),
                    CryptoCurrencyStatus.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
