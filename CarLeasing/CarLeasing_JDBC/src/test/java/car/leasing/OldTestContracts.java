package car.leasing;

import org.junit.Test;

import java.io.ByteArrayInputStream;

public class OldTestContracts {
    @Test
    public void testAddContract(){
        String input = "3\n" + //выбор контракт меню
                "1\n" +  // выбор добавить договор

                "1\n" + // выбор ID клиента
                "7\n" + // нет такого клиента
                "0 \n" +

                "1\n" +  // выбор ID клиента
                "1\n" + // ID
                "1\n" +
                "0\n" + // вернулись назад

                "1\n" +  // выбор ID клиента
                "1\n" +
                "1\n" +
                "123456789qefrgwerYUIu\n" +
                "4T1BF1FKXEU123456\n" +
                " 0 \n " +

                "1\n" +  // выбор ID клиента
                "1\n" +
                "1\n" +
                "1234567897werYUIu\n" +
                "erty\n" +
                "8.9\n" +
                 "8\n" +
                "qweqwe\n" +
                "0\n" +

                "1\n" +  // выбор ID клиента
                "1\n" +
                "1\n" +
                "4T1BF1FKXEU123456\n" +
                "8\n" + // месяцы лизинка
                "69.77777\n" + // первоначальный взнос
                "werwer\n" +
                "0\n"+

                "1\n" +  // выбор ID клиента
                "1\n" +
                "1\n" +
                "4T1BF1FKXEU123456\n" +
                "2\n" +
                "69.апрпа77777\n" +
                "69.77777\n" +
                "45.87\n"+
                "11.6\n" + // первоначальный взнос
                "45.6\n";


        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Main.main(new String[]{});
    }
    @Test
    public void testFindContract(){
        String input = "3\n" + //выбор контракт меню

                "2\n" +  // поиск договора по номеру
                "10\n"+

                "2\n" +  // поиск договора по номеру
                "1\n"+

                "3\n" +  // поиск договора по номеру
                "+71234ert567896\n"+
                "0\n" +

                "3\n" +  // поиск договора по номеру
                "+71234567896\n"+


                "3\n" +  // поиск договора по клиенту
               "+71234567890\n" +

                "4\n" +  // поиск договора по клиенту
                "+71234567890\n";

        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Main.main(new String[]{});
    }
    @Test
    public void testPayments(){
//        String input = "4\n" +                // Главное меню -> Управление платежами
//                "1\n" +
//                "67\n" +                // Номер договора
//                "33sdfsdf933\n" +
//                "0\n" +                // Выход из оплаты
//
//                "1\n" +
//                "4\n" +             // Номер договора
//                "30000\n" +       //Сумма
//                "0\n" +
//
//                "1\n" +
//                "4\n" +             // Номер договора
//                "6.026919\n" +      // Сумма

//                "1\n" +
//                "4\n" +             // Номер договора
//                "6.026919\n"+      // Сумма
//
//                "1\n" +
//                "4\n" +             // Номер договора
//                "6.026919\n";      // Сумма
        String input = "4\n" +                // Главное меню -> Управление платежами
                    "1\n" +
                    "3\n" +                // Номер договора
                    "6.026919\n";

                ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Main.main(new String[]{});
    }
}
