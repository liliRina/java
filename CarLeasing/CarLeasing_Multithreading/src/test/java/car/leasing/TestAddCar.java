package car.leasing;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Random;

public class TestAddCar {
    @Test
    public void testAddCar(){
        Random random = new Random();
        String input = "1\n" + //выбор авто меню
                        "1\n" + // выбор добавить авто
                        "123456\n" + // некорректный VIN
                        "2342dfsdfFDDF\n" +
                        "0\n" + //вернулись назад

                        "1\n" + // выбор добавить авто
                        " 123456789qwerYUIu \n" + // корректный VIN
                        " BMVХхъ{}{???///++...!#$%^&*( \n" + // марка всё можно!
                        "0 \n" + //вернулись назад

                        "1\n" + // выбор добавить авто
                        " 123456789qwerYUIu \n" + // корректный VIN
                        " BMVХхъ{}{???///++...!#$%^&*( \n" + // марка всё можно!
                        " BMVх5Ххъ{}{???///++...!#$%^&*( \n" + // модель всё можно!
                        " 0\n" + // вернулись назад

                        "1\n" + // выбор добавить авто
                        " 123456789qwerYUIu \n" + // корректный VIN
                        " BMVХхъ{}{???///++...!#$%^&*( \n" + // марка всё можно!
                        " BMVх5Ххъ{}{???///++...!#$%^&*( \n" + // модель всё можно!
                        "1999\n" + //неккоректный год
                        " 2027 \n" + //неккоректный год
                        "0 \n" + // вернулись назад

                        "1\n" + // выбор добавить авто
                        " 123456789qwerYUIu \n" + // корректный VIN
                        " BMVХхъ{}{???///++...!#$%^&*( \n" + // марка всё можно!
                        " BMVх5Ххъ{}{???///++...!#$%^&*( \n" + // модель всё можно!
                        "  2007 \n" + //корректный год
                        " -1\n" +
                        " efdgd\n" +
                        " 0\n" +

                        "1\n" + // выбор добавить авто
                        + random.nextInt(1000, 10000) + "qweRTYuioQWEr\n" +
                        " 123456789qwerYUIu \n" + // корректный VIN
                        " BMVХхъ{}{???///++...!#$%^&*( \n" + // марка всё можно!
                        " BMVх5Ххъ{}{???///++...!#$%^&*( \n" + // модель всё можно!
                        "  2007 \n" + //корректный год
                        "23\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Main.main(new String[]{});
    }
    @Test
    public void testGetCars(){
        String input = "1\n" + //выбор авто меню
                        "2\n"; // выбор получить все авто
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Main.main(new String[]{});
    }
    @Test
    public void testFindCars(){
        String input = "1\n" + //выбор авто меню
                        "3\n" + // выбор получить все авто

                        "1\n" + // выбор по марке
                        "Fordff\n" + // нет марки

                        "1\n" + // выбор по марке
                        "0\n" + // нет марки

                        "1\n" + // выбор по марке
                        "Ford\n" +

                        "2\n" +
                        "0\n" + // выбор по модели
                        "2\n" + // выбор по модели
                        "Focuававаs\n" +
                        "2\n" + // выбор по модели
                        "Focus\n" +

                        "3\n" + // выбор по году
                        "0\n" +

                        "3\n" + // выбор по году
                        "2090\n" +
                        "2378\n" + // вернулись назад
                        "0\n" +

                        "3\n" + // выбор по году
                        "2025\n";

        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Main.main(new String[]{});
    }
}

//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        PrintStream originalOut = System.out;
//        System.setOut(new PrintStream(out));