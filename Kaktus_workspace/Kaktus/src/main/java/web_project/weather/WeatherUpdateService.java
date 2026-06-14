package web_project.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherUpdateService {
    private WeatherDTO curWeather = new WeatherDTO();
    private final ApplicationEventPublisher eventPublisher;

    @KafkaListener(topics = "weather", groupId = "cactus-group")
    public void changeWaterLoss(WeatherDTO weather) {
        curWeather = weather;
        System.out.println("Weather: " + weather);
        eventPublisher.publishEvent(new WeatherUpdatedEvent(this, weather));
    }
    public WeatherDTO getWeather(){
        return curWeather;
    }

    public class WeatherUpdatedEvent extends ApplicationEvent {
        private final WeatherDTO weather;

        public WeatherUpdatedEvent(Object source, WeatherDTO weather) {
            super(source);
            this.weather = weather;
        }
        public WeatherDTO getWeather() { return weather; }
    }

}
