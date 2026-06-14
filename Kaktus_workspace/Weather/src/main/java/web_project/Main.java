package web_project;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import web_project.weather.WeatherDTO;

@SpringBootApplication
@EnableScheduling
@EnableKafka
public class Main {

    @Autowired
    private KafkaTemplate<String, WeatherDTO> kafkaTemplate;
    private final String TOPIC = "weather";

    @Scheduled(fixedDelay = 10000)
    public void sendWeather() {
        WeatherDTO weatherDTO = new WeatherDTO();
        kafkaTemplate.send(TOPIC, weatherDTO);
        System.out.println("Погода отправлена: " + weatherDTO);
    }
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}