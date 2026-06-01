package car.leasing;

import java.util.concurrent.CompletableFuture;

public class Main {
    public static void sleep(Integer msec){
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Критическая ошибка");
            throwable.printStackTrace();
            //System.exit(1);
        });
        CompletableFuture.runAsync(() -> {InitDB initDB = new InitDB();})
                .exceptionally(ex -> {
                    System.err.println("База данных не поднялась: " + ex);
                    ex.printStackTrace();
                    System.exit(1);
                    return null;
        });;
        MainHandler LeasingMenu = new MainHandler();
        LeasingMenu.mainMenu();
        System.out.println("Работа с лизингом закончена");
    }
}
