package car.leasing;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void sleep(Integer msec){
//        try {
//            Thread.sleep(msec);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
    public static final Logger LOG = Logger.getLogger("LeasingApp");
    static {
        LOG.setLevel(Level.OFF);  // или OFF для тишины
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Критическая ошибка");
            throwable.printStackTrace();
            System.exit(1);
        });
        MainHandler LeasingMenu = new MainHandler();
        LeasingMenu.mainMenu();
        System.out.println("Работа с лизингом закончена");
    }
}