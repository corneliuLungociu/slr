package slr;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import slr.services.PredictionService;
import slr.services.impl.NeuralNetworkPredictionService;

@Configuration
@ComponentScan(basePackages = "slr")
public class SpringContext {

    @Bean
    public PredictionService getPredictionService() {
        NeuralNetworkPredictionService predictionService = new NeuralNetworkPredictionService();
        predictionService.initForPrediction("/neuralNetwork.txt");
        return predictionService;
    }

}
