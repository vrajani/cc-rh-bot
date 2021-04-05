package pl.vrajani.service;

import pl.vrajani.BackTest;
import pl.vrajani.model.*;
import pl.vrajani.request.APIService;
import pl.vrajani.utility.MathUtil;

import java.io.IOException;
import java.util.List;

public class OrderService {
    private final APIService apiService;
    private final LimitManager limitManager;

    public OrderService(APIService apiService, DaoService daoService) {
        this.apiService = apiService;
        this.limitManager = new LimitManager(daoService);
    }

    public CryptoOrderResponse executeBuy(CryptoCurrencyStatus cryptoCurrencyStatus, List<DataPoint> dataPoints,
                                          double lastPrice, boolean shouldExecute) throws IOException, InterruptedException {
        if (doesMeetBuyCriteria(cryptoCurrencyStatus, lastPrice, dataPoints)) {
            System.out.println("Buying :: "+ cryptoCurrencyStatus.getSymbol() + " with price: "+ lastPrice);
            double quantity = cryptoCurrencyStatus.getBuyAmount() / lastPrice;
            return shouldExecute ? executeBuy(cryptoCurrencyStatus, lastPrice, quantity) :
                    BackTest.getDummyCryptoOrderResponse(lastPrice, quantity);
        }
        return null;
    }

    private boolean doesMeetBuyCriteria(CryptoCurrencyStatus cryptoCurrencyStatus, double lastPrice, List<DataPoint> dataPoints) {
        double initialPrice = Double.parseDouble(dataPoints.get(0).getClosePrice());
        double buyPercent = MathUtil.getPercentAmount(lastPrice, initialPrice);

        double tenMinAgoPrice = Double.parseDouble(dataPoints.get(dataPoints.size() - 2).getClosePrice());
        double tenMinPercent = MathUtil.getPercentAmount(lastPrice, tenMinAgoPrice);

        if(Boolean.parseBoolean(System.getenv("PRINT_INFO_LOGS"))){
            System.out.println("Current Value: " + lastPrice
                    + "\n10 Min ago Value: " + tenMinAgoPrice
                    + "\n10 Min Percent: "+ tenMinPercent
                    + "\n" + (dataPoints.size() * 5) + "Min ago Value: " + initialPrice
                    + "\nBuy Percent: "+ buyPercent);
        }

        double highPrice = dataPoints
                .stream()
                .mapToDouble(dataPoint -> Double.parseDouble(dataPoint.getHighPrice()))
                .max().getAsDouble();
        double targetBuyPercent = 100 - cryptoCurrencyStatus.getBuyPercent();


        return buyPercent < targetBuyPercent && tenMinPercent > 99.75 &&
                MathUtil.getPercentAmount(lastPrice, highPrice) < 100 - (cryptoCurrencyStatus.getProfitPercent() * 1.5);
    }

    private CryptoOrderResponse executeBuy(CryptoCurrencyStatus cryptoCurrencyStatus, double lastPrice, double quantity) throws IOException, InterruptedException {
        if(limitManager.shouldBuyMore(cryptoCurrencyStatus.getSymbol())) {
            return executeWithRetry(cryptoCurrencyStatus.getSymbol(), String.valueOf(quantity), String.valueOf(lastPrice), "buy");
        } else {
            System.out.println("Already have pending orders in stop loss more than limit!");
        }
        return  null;
    }

    CryptoOrderResponse setSellOrder(CryptoCurrencyStatus currencyStatus,
                                             CryptoOrderStatusResponse cryptoOrderStatusResponse, double price) throws InterruptedException {
        System.out.println("Setting sell order:: " + currencyStatus.getCcId() + " with price: " + price);
        return executeWithRetry(currencyStatus.getSymbol(), cryptoOrderStatusResponse.getQuantity(),
                String.valueOf(price), "sell");
    }

    private CryptoOrderResponse executeWithRetry(String symbol, String quantity, String price, String side) throws InterruptedException {
        int retryCounter = 0;
        while(retryCounter < 3) {
            try {
                if(side.equalsIgnoreCase("buy")) {
                    return apiService.buyCrypto(symbol, quantity, price);
                } else {
                    return apiService.sellCrypto(symbol, quantity, price);
                }
            } catch (Exception ex) {
                retryCounter++;
                ex.printStackTrace();
                Thread.sleep(5000);
            }
        }
        throw new RuntimeException("Failed to set sell order after all retries");
    }
}
