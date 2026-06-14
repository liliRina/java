package web_project.weather;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class WeatherDTO {
    private int temperature;
    private int humidity;
}