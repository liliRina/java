package car.leasing;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Random;

public class OldTestClients {
    @Test
    public void testAddClient(){
        Random random = new Random();
        String input = "2\n" + //выбор клиент меню
                "1\n" +  // выбор добавить клиента
                " dfgfhfghfh dfghfdgd \n" + // ФИО всё можно
                " 2342dfsdfFDDF \n" + //неправильный паспорт
                "0\n" +

                "1\n" + // выбор добавить клиента
                " dfgfhfghfh dfghfdgd \n" + // ФИО всё можно
                " 1234567890 \n" + // правильный паспорт
                " +4123456789\n" + //неправильный тлф
                "0\n" +

                "1\n" + // выбор добавить клиента
                " dfgfhfghfh dfghfdgd \n" + // ФИО всё можно
                " 1234567890 \n" + // правильный паспорт
                " +71234567890\n" + // правильный тлф

                "1\n" + // добавим с таким же номером телефона
                " dfgfhfghfh dfghfdgd \n" + // ФИО всё можно
                random.nextInt(10000, 100000) + "12345\n" +
                "+7" + random.nextInt(10000, 100000) + "12345\n";

        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Main.main(new String[]{});
    }
    @Test
    public void testFindClients(){
        String input = "2\n" + //выбор клиент меню
                "3\n" +  // выбор поиск клиента

                "1\n"+
                " 0 \n" +

                "1\n"+
                " dfgfhfghfh dfghfdgd \n" + // ФИО всё можно

                "2\n" +
                "0\n" +

                "2\n" +
                " 123456d7890 \n" + // неправильный паспорт
                " 1234567890 \n"; // правильный паспорт


        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Main.main(new String[]{});

    }
}
