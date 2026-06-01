package car.leasing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import car.leasing.cars.CarsDB;
import car.leasing.cars.Garage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestGarage {
    static ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Mock
    Scanner mockScanner;
    @Mock
    CarsDB mockCarsDB;
    @InjectMocks
    MainHandler handler;
    @InjectMocks
    Garage garage;

    @BeforeAll
    static void setUp() {
        System.setOut(new PrintStream(baos));
    }

    @Test
    @DisplayName("Добавление автомобиля с некорректным VIN")
    public void testAddCarInvalidVIN(){
        when(mockScanner.nextLine())
                .thenReturn("VIN123")
                .thenReturn("1234567890qwertyut")
                .thenReturn("1234567890qwerty-")
                .thenReturn("0");
        boolean returnAns = garage.addCar();
        String ansAddCar = baos.toString();
        assertEquals(3, countOccurrences(ansAddCar, "VIN должен состоять из 17 знаков: цифры и латинские буквы"));
        assertEquals(false, returnAns);
    }
    @Test
    @DisplayName("Добавление автомобиля с некорректным годом")
    public void testAddCarinvalidYear(){
        when(mockScanner.nextLine())
                .thenReturn("1234567890qwertyu")
                .thenReturn("BMV")
                .thenReturn("BMVx5")
                .thenReturn("1999")
                .thenReturn("2050")
                .thenReturn("234t")
                .thenReturn("0");
        boolean returnAns = garage.addCar();
        String ansAddCar = baos.toString();
        assertEquals(3, countOccurrences(ansAddCar, "Год должен быть в диапазоне от 2000 до 2026"));
        assertEquals(false, returnAns);
    }
    @Test
    @DisplayName("Добавление автомобиля с некорректной ценой")
    public void testAddCarInvalidPrice(){
        when(mockScanner.nextLine())
                .thenReturn("1234567890qwertyu")
                .thenReturn("BMV")
                .thenReturn("BMVx5")
                .thenReturn("2001")
                .thenReturn("-20")
                .thenReturn("sdfsdf")
                .thenReturn("0");
        boolean returnAns = garage.addCar();
        String ansAddCar = baos.toString();
        assertEquals(2, countOccurrences(ansAddCar, "Стоимость должна быть положительным числом"));
        assertEquals(false, returnAns);
    }
    @Test
    @DisplayName("Добавление автомобиля с корректными параметрами")
    public void testAddCar(){
        when(mockCarsDB.getCarByVIN(any())).thenReturn(null);
        when(mockCarsDB.saveNewCar(any())).thenReturn(true);
        when(mockScanner.nextLine())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("1234567890qwertyu")
                .thenReturn("BMV")
                .thenReturn("BMVx5")
                .thenReturn("2001")
                .thenReturn("30000")
                .thenReturn("0");
        boolean returnAns = garage.addCar();
        String ansAddCar = baos.toString();
        assertEquals(1, countOccurrences(ansAddCar, "Автомобиль успешно добавлен!"));
        assertEquals(true, returnAns);
    }

    private long countOccurrences(String text, String word) {
        return Pattern.compile(Pattern.quote(word))
                .matcher(text)
                .results()
                .count();
    }
}
