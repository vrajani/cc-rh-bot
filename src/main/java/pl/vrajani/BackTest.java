package pl.vrajani;

import org.apache.commons.lang3.time.StopWatch;
import pl.vrajani.model.*;
import pl.vrajani.request.APIService;
import pl.vrajani.service.ActionService;
import pl.vrajani.service.ControllerService;
import pl.vrajani.utility.MathUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BackTest {
    private static final List<String> CRYPTOS = Arrays.asList("LTC","BTC", "ETH", "BCH", "BSV");
    public static final int TOP_K = 20;
    private final APIService apiService;

    public static void main(String[] args) {
        String token = System.getenv("token");
        new BackTest(token).execute();
    }

    public BackTest(String token){
        this.apiService = Application.getApiService(token);
    }

    private void execute() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Map<String, List<CryptoCurrencyStatus>> statusByCrypto = CRYPTOS.stream().collect(Collectors.toMap(Function.identity(), crypto -> {
            try {
                return processCrypto(crypto);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }));
        stopWatch.stop();
        statusByCrypto.keySet().forEach(crypto -> printResults(statusByCrypto.get(crypto)));
        System.out.println("Time taken - " + stopWatch.getTime());
    }

    private void printResults(List<CryptoCurrencyStatus> cryptoCurrencyStatuses) {
        System.out.println("################################################################################################");
        StringBuilder result = new StringBuilder();
        double totalBuyPercent = 0.0;
        double totalProfitPercent = 0.0;
        double totalGain = 0.0;
        for (CryptoCurrencyStatus cryptoCurrencyStatus : cryptoCurrencyStatuses) {
            result.append(cryptoCurrencyStatus.getBuyPercent()).append(ReportGenerator.SEPARATOR);
            result.append(cryptoCurrencyStatus.getProfitPercent()).append(ReportGenerator.SEPARATOR);
            ReportGenerator.getReportData(result, cryptoCurrencyStatus);
            totalBuyPercent += cryptoCurrencyStatus.getBuyPercent();
            totalProfitPercent += cryptoCurrencyStatus.getProfitPercent();
            totalGain += cryptoCurrencyStatus.getProfit();
        }
        double medianProfitPercent = cryptoCurrencyStatuses.stream().map(CryptoStatusBase::getProfitPercent).sorted().collect(Collectors.toList()).get(TOP_K / 2);
        double medianBuyPercent = cryptoCurrencyStatuses.stream().map(CryptoStatusBase::getBuyPercent).sorted().collect(Collectors.toList()).get(TOP_K / 2);


        System.out.println(result.toString());

        System.out.println("Avg Buy Percent - " + (totalBuyPercent/ TOP_K));
        System.out.println("Avg Sell Percent - " + (totalProfitPercent/ TOP_K));
        System.out.println("Avg Gain - " + (totalGain/ TOP_K));
        System.out.println("Median sell percent - " + MathUtil.roundDecimal(medianProfitPercent, "0.00"));
        System.out.println("Median buy percent - " + MathUtil.roundDecimal(medianBuyPercent, "0.00"));
    }

    public List<CryptoCurrencyStatus> processCrypto(String crypto) throws InterruptedException {
        List<Double> profitPercentRange = getPercentRange();
        List<Double> buyPercentRange = getPercentRange();
        List<CryptoCurrencyStatus> results = new ArrayList<>();

        CryptoCurrencyStatus testConfig;
        CryptoHistPrice cryptoHistPriceBySymbol = apiService.getCryptoHistPriceBySymbol(crypto, "week", "5minute");

        for (Double percent: profitPercentRange) {
            for (Double buyPercent : buyPercentRange) {
                testConfig = getTestConfig(crypto);
                testConfig.setProfitPercent(percent);
                testConfig.setBuyPercent(buyPercent);
                CryptoCurrencyStatus resultStatus = runTest(cryptoHistPriceBySymbol, testConfig);
                if (resultStatus != null && resultStatus.getRegularSell() != 0) {
                    results.add(resultStatus);
                }
            }
        }
        return results.stream().sorted(Comparator.comparingDouble(CryptoCurrencyStatus::getProfit).reversed()).limit(TOP_K).collect(Collectors.toList());
    }

    private List<Double> getPercentRange() {
        List<Double> profitPercent = new ArrayList<>();
        double currentPercent = 0.2;
        while(currentPercent <= 1.5) {
            profitPercent.add(currentPercent);
            currentPercent += 0.05;
        }
        return profitPercent;
    }

    private CryptoCurrencyStatus runTest(CryptoHistPrice cryptoHistPriceBySymbol, CryptoCurrencyStatus testConfig) throws InterruptedException {
        ActionService actionService = new ActionService(null);
        ControllerService  controllerService = new ControllerService(null);
        List<DataPoint> dataPoints = cryptoHistPriceBySymbol.getDataPoints();
        int i = 0;
        for (int j = 0; j < dataPoints.size() - 1; i++) {
            j = i + 6;
            double highPrice = Double.parseDouble(dataPoints.subList(j < 288 ? 0 : i, j).stream().max(Comparator.comparingDouble(dataPoint -> Double.parseDouble(dataPoint.getHighPrice()))).get().getHighPrice());
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

    private CryptoCurrencyStatus getTestConfig(String symbol) {
        CryptoCurrencyStatus status = new CryptoCurrencyStatus();
        status.setSymbol(symbol);
        status.setCcId(symbol);
        status.setRegularSell(0);
        status.setQuantity(0);
        status.setProfit(0);
        status.setLastBuyPrice(0);
        status.setProfitPercent(0.5);
        status.setBuyPercent(0.5);
        status.setShouldBuy(true);
        status.setBuyAmount(100);
        status.setWaitInMinutes(500);
        return status;
    }

    public static CryptoOrderResponse getDummyCryptoOrderResponse(double lastPrice, double quantity) {
        CryptoOrderResponse dummyResponse = new CryptoOrderResponse();
        dummyResponse.setSide("buy");
        dummyResponse.setPrice(String.valueOf(lastPrice));
        dummyResponse.setQuantity(String.valueOf(quantity));
        dummyResponse.setState("filled");
        dummyResponse.setId("DUMMY_RESPONSE_ID");
        return dummyResponse;
    }
}
