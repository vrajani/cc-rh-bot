package pl.vrajani;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.request.APIService;
import pl.vrajani.service.ActionService;
import pl.vrajani.service.ControllerService;
import pl.vrajani.service.DaoService;

import java.io.IOException;
import java.util.HashMap;

public class Application implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {

        ObjectMapper objectMapper = new ObjectMapper();
        APIService apiService = apiService();
        ActionService actionService = new ActionService(apiService, objectMapper);
        DaoService daoService = new DaoService(objectMapper);
        ControllerService controllerService = new ControllerService(apiService, actionService, daoService);
        try {
            controllerService.checkAllCrypto();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Completed Execution";
    }

    public static APIService apiService() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("username", System.getenv("username"));
        properties.put("password", System.getenv("password"));
        properties.put("grant_type", "password");
        properties.put("client_id", System.getenv("client_id"));
        properties.put("account_id", System.getenv("account_id"));
        properties.put("accountId", System.getenv("accountId"));

        return new APIService(properties);
    }
}
