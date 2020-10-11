package pl.vrajani.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.model.DataConfig;
import pl.vrajani.model.StopLossConfigBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DaoService {
    private final static String AWS_BUCKET = "cc-rh-config";
    private final static String CONFIG_FILE = "config.json";
    private final static String STOP_LOSS_CONFIG_FILE = "stop-loss-config.json";
    private final static String TRANSACTION_REPORT_FILE = "transaction-report.txt";
    private final ObjectMapper objectMapper;
    private final AmazonS3 s3client;

    public DaoService(){
        this.objectMapper = new ObjectMapper();
        this.s3client = AmazonS3ClientBuilder
                .defaultClient();
    }

    public DataConfig getMainConfig() throws IOException {
        return objectMapper.readValue(getS3ObjectInputStream(CONFIG_FILE), DataConfig.class);
    }

    public void updateMainConfig(DataConfig dataConfig) throws JsonProcessingException {
        updateS3Config(CONFIG_FILE, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataConfig));
    }

    public StopLossConfigBase getStopLossConfig() throws IOException {
        return objectMapper.readValue(getS3ObjectInputStream(STOP_LOSS_CONFIG_FILE), StopLossConfigBase.class);
    }

    public void updateStoplossConfig(StopLossConfigBase stopLossConfigBase) throws JsonProcessingException {
        updateS3Config(STOP_LOSS_CONFIG_FILE, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(stopLossConfigBase));
    }

    public void registerTransactionReport(String transactionReports) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getS3ObjectInputStream(TRANSACTION_REPORT_FILE)));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            if(!line.isEmpty()) {
                sb.append(line).append(System.lineSeparator());
            }
        }

        updateS3Config(TRANSACTION_REPORT_FILE, sb.append(transactionReports).toString());
    }

    private S3ObjectInputStream getS3ObjectInputStream(String configFile) {
        S3Object object = s3client.getObject(AWS_BUCKET, configFile);
        return object.getObjectContent();
    }

    private void updateS3Config(String stopLossConfigFile, String s) {
        s3client.putObject(AWS_BUCKET, stopLossConfigFile, s);
    }
}
