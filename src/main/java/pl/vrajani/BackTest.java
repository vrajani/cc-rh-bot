package pl.vrajani;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.model.*;
import pl.vrajani.service.ActionService;
import pl.vrajani.service.ControllerService;
import pl.vrajani.utility.MathUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackTest {

    private double stopLossFactor;

    public static void main(String[] args) throws IOException {
        new BackTest().execute();
    }

    private void execute() throws IOException {
        List<Double> profitPercent = getProfitPercentRange();
        List<Double> stopLossRange = getStopLossRange();

        ObjectMapper objectMapper = new ObjectMapper();
        String token = System.getenv("token");
        CryptoCurrencyStatus testConfig = getTestConfig(objectMapper);
        String symbol = testConfig.getSymbol();
        CryptoHistPrice cryptoHistPriceBySymbol = ControllerService.apiService(token).getCryptoHistPriceBySymbol(symbol, "week", "5minute");

        StringBuilder result = new StringBuilder();
        for (Double percent: profitPercent) {
            for(double stopLoss: stopLossRange) {
                this.stopLossFactor = stopLoss;
                testConfig = getTestConfig(objectMapper);
                testConfig.setProfitPercent(percent);
                CryptoCurrencyStatus resultStatus = runTest(cryptoHistPriceBySymbol, testConfig);
                if (resultStatus != null) {
                    result.append(percent).append(ReportGenerator.SEPARATOR);
                    result.append(stopLoss).append(ReportGenerator.SEPARATOR);
                    ReportGenerator.getReportData(result, resultStatus);
                }
            }
        }
        System.out.println(result.toString());
    }

    private List<Double> getProfitPercentRange() {
        List<Double> profitPercent = new ArrayList<>();
        double currentPercent = Double.parseDouble("0.10");
        while(currentPercent < 1.0D) {
            currentPercent += 0.05D;
            profitPercent.add(currentPercent);
        }
        return profitPercent;
    }

    private List<Double> getStopLossRange() {
        List<Double> stopLossRange = new ArrayList<>();
        double currentPercent = Double.parseDouble("4.0");
        while(currentPercent <= 7.0) {
            currentPercent += 0.5;
            stopLossRange.add(currentPercent);
        }
        return stopLossRange;
//        return Collections.singletonList(0.21);
    }

    private CryptoCurrencyStatus runTest(CryptoHistPrice cryptoHistPriceBySymbol, CryptoCurrencyStatus testConfig) {
        ActionService actionService = new ActionService(null);
        ControllerService  controllerService = new ControllerService(null);
        List<DataPoint> dataPoints = cryptoHistPriceBySymbol.getDataPoints();
        int i = 0;
        for (int j = 0; j < dataPoints.size() - 1; i++) {
            j = i + 6;
            System.out.println("Testing for Starting time: " + dataPoints.get(j).toString());
            if (testConfig.isShouldBuy()) {
                CryptoOrderResponse cryptoOrderResponse = actionService.executeBuy(testConfig, dataPoints.subList(i,j), Double.parseDouble(dataPoints.get(j).getClosePrice()), false);
                if(cryptoOrderResponse != null) {
                    System.out.println("Buy Order Executed: " + cryptoOrderResponse.toString());
                    testConfig = controllerService.processFilledOrder(testConfig, getDummyCryptoOrderStatusResponse(cryptoOrderResponse), false);
                }
            } else {
                DataPoint currentDataPoint = dataPoints.get(j);
                double targetPrice = MathUtil.getAmount(testConfig.getLastBuyPrice(), 100 + testConfig.getProfitPercent());
                double targetStopLossPrice = MathUtil.getAmount(testConfig.getLastBuyPrice(), 100 - (testConfig.getProfitPercent() * this.stopLossFactor));
                double highPriceOfCurrentDataPoint = MathUtil.getAmount(Double.parseDouble(currentDataPoint.getHighPrice()), 99.90);
                double lowPriceOfCurrentDataPoint = MathUtil.getAmount(Double.parseDouble(currentDataPoint.getLowPrice()), 100.1);

                boolean stoploss = highPriceOfCurrentDataPoint < targetStopLossPrice;
                boolean sell = lowPriceOfCurrentDataPoint < targetPrice && targetPrice < highPriceOfCurrentDataPoint;
                if ( sell || stoploss) {
                    double sellPrice = stoploss ? targetStopLossPrice : targetPrice;
                    String msg = stoploss?  "Sell stop loss Order Executed: " : "Sell Order Executed: ";
                    System.out.println(msg + currentDataPoint.toString() + " with targetPrice = " + sellPrice);

                    CryptoOrderStatusResponse cryptoOrderStatusResponse = new CryptoOrderStatusResponse();
                    cryptoOrderStatusResponse.setPrice(String.valueOf(sellPrice));
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
