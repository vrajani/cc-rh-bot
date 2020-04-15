package pl.vrajani;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.InputStream;
import java.io.OutputStream;

public class ConfigUpdater implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        DaoService daoService = new DaoService(objectMapper);
//
//
//        try {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            JSONParser parser = new JSONParser();
//            JSONObject event = (JSONObject)parser.parse(reader);
//
//            HttpMethod httpMethod = HttpMethod.valueOf(event.get("httpMethod"));
//            if(httpMethod.equals(HttpMethod.GET)) {
//                // Retrieve data
//                String path = event.get("path");
//                daoService.getDataConfig()
//            } else if( httpMethod.equals(HttpMethod.POST)) {
//
//            }
//
//            if (event.get("body") != null) {
//                JSONObject body = (JSONObject) parser.parse((String) event.get("body"));
//                CryptoStatusBase statusBase = objectMapper.reader().readValue(body);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//        System.out.println("Done!");
    }
}
