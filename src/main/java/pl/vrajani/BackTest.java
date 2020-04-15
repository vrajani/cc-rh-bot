package pl.vrajani;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.model.CryptoCurrencyStatus;
import pl.vrajani.model.CryptoHistPrice;
import pl.vrajani.model.CryptoOrderResponse;
import pl.vrajani.model.CryptoOrderStatusResponse;
import pl.vrajani.model.DataPoint;
import pl.vrajani.service.ActionService;
import pl.vrajani.service.ControllerService;
import pl.vrajani.utility.MathUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BackTest {

    private final double stopLossFactor;

    public BackTest(double stopLossFactor) {
        this.stopLossFactor = stopLossFactor;
    }

    public static void main(String[] args) throws IOException {
        new BackTest(3.4).execute();
    }

    private void execute() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String token = "";
        CryptoCurrencyStatus testConfig = getTestConfig(objectMapper);
        String symbol = testConfig.getSymbol();
        CryptoHistPrice cryptoHistPriceBySymbol = ControllerService.apiService(token).getCryptoHistPriceBySymbol(symbol, "week", "5minute");

        CryptoCurrencyStatus resultStatus = runTest(cryptoHistPriceBySymbol, testConfig);
        StringBuilder result = new StringBuilder();
        if (resultStatus != null) {
            ReportGenerator.getReportData(result, resultStatus);
        }
        System.out.println(result.toString());
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
                double targetPrice = MathUtil.getAmount(testConfig.getLastBuyPrice(), Double.parseDouble("100") + testConfig.getProfitPercent());
                double targetStopLossPrice = MathUtil.getAmount(testConfig.getLastBuyPrice(), Double.parseDouble("100") - (testConfig.getProfitPercent() * this.stopLossFactor));
                double highPriceOfCurrentDataPoint = Double.parseDouble(currentDataPoint.getHighPrice());

                boolean stoploss = highPriceOfCurrentDataPoint < targetStopLossPrice;
                boolean sell = Double.parseDouble(currentDataPoint.getLowPrice()) < targetPrice && targetPrice < highPriceOfCurrentDataPoint;
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
