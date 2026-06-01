package car.leasing;

public class Main {
    static void print(String message){
        System.out.println(message);
    }
    public static void main(String[] args) {
        MainHandler LeasingMenu = new MainHandler();
        LeasingMenu.mainMenu();
        System.out.println("Работа с лизингом закончена");
    }
}
