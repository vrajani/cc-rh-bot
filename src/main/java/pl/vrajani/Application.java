package pl.vrajani;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.vrajani.service.ControllerService;
import pl.vrajani.service.DaoService;

import java.io.IOException;

public class Application implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        DaoService daoService = new DaoService(objectMapper);

        ControllerService controllerService = new ControllerService(daoService, objectMapper);
        try {
            controllerService.checkAllCrypto();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Completed Execution";
    }
}
