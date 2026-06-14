package web_project.exceptions;

public class GetWaterLossEx extends RuntimeException{
    public GetWaterLossEx(String message){
        super("Ошибка при получении значения потери влажности: " + message);
    }
}
