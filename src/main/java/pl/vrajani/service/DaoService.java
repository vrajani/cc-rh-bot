package pl.vrajani.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.model.CryptoOrderStatusResponse;
import pl.vrajani.model.DataConfig;

import java.io.*;

public class DaoService {
    private final ObjectMapper objectMapper;
    private final AmazonS3 s3client;

    public DaoService(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
        this.s3client = AmazonS3ClientBuilder
                .defaultClient();
    }

    public DataConfig getDataConfig() throws IOException {
        if(isOnCloud()){
            S3Object object = s3client.getObject("cc-rh-config", "config.json");
            S3ObjectInputStream is = object.getObjectContent();
            return objectMapper.readValue(is, DataConfig.class);
        }
        return refresh();
    }

    private boolean isOnCloud() {
        String isAWS = System.getenv("isAWS");
        return isAWS != null && isAWS.equalsIgnoreCase("true");
    }

    public void updateConfig(DataConfig dataConfig) throws JsonProcessingException {
        if(isOnCloud()){
            s3client.putObject("cc-rh-config", "config.json",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataConfig));
        } else {
            saveStatus(dataConfig);
        }
    }

    private void saveStatus(DataConfig dataConfig){
        //Finally save the new state, for just in case.
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("src/main/resources/status/config.json"), dataConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DataConfig refresh(){
        try {
            return objectMapper.readValue(new File("src/main/resources/status/config.json"),
                    DataConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void registerTransactionReport(String transactionReports) throws IOException {
        if(isOnCloud()) {
            S3Object historicalReports = s3client.getObject("cc-rh-config", "transaction-report.txt");
            S3ObjectInputStream s3objectResponse = historicalReports.getObjectContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(s3objectResponse));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                if(!line.isEmpty()) {
                    sb.append(line).append(System.lineSeparator());
                }
            }

            s3client.putObject("cc-rh-config", "transaction-report.txt", sb.append(transactionReports).toString());
        } else {
            FileWriter writer = new FileWriter(new File("src/main/resources/transaction-report.txt"), true);
            writer.write(transactionReports);
        }
    }
}
