package web_project.weather;

import lombok.*;

import java.util.Random;

@AllArgsConstructor
@Setter
@Getter
@ToString
public class WeatherDTO {
    private Random random = new Random();
    public WeatherDTO(){
        this.temperature = random.nextInt(0, 31);
        this.humidity = random.nextInt(0, 100);
    }
    private int temperature;
    private int humidity;
}
