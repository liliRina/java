package DB;

import DB.Repositories.*;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RepositoryTest {
    static KolobokRepository kolobokRepo;
    static CalorieRepository calorieRepo;
    static{
        Logger.getLogger("org.hibernate").setLevel(Level.OFF);
        Logger.getLogger("org.postgresql").setLevel(Level.OFF);
        Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        kolobokRepo = new KolobokRepository();
        calorieRepo = new CalorieRepository();
    }

    @Test
    void fullCreateKoloboks(){
        Random random = new Random();
        for (int i = 1; i <= 10; i++) {
            int speed = random.nextInt(100);
            double roundness = random.nextDouble();
            Kolobok kolobok = new Kolobok("Kolobok" + String.valueOf(i), speed, roundness, EyesColor.fromInt(i));

            double water_amount = random.nextDouble(1);
            Dough dough = new Dough(water_amount, "Makfa", i%2 == 1);

            int calorificValue = random.nextInt(100, 200);
            String flourTrademark = "Makfa" + i;
            Calorie calorie = new Calorie(calorificValue, flourTrademark);

            kolobok.setDough(dough);
            kolobok.setCalorie(calorie);
            Kolobok savedKolobok = kolobokRepo.save(kolobok).orElse(new Kolobok());

            System.out.println(savedKolobok);
        }
    }

    @Test
    void createKoloboks() {
        for (int i = 1; i <= 10; i++) {
            Random random = new Random();
            int speed = random.nextInt(100);
            double roundness = random.nextDouble();
            Kolobok kolobok = new Kolobok("Kolobok" + String.valueOf(i), speed, roundness, EyesColor.fromInt(i));

            Optional<Kolobok> optionalKolobok = kolobokRepo.save(kolobok);
            if (optionalKolobok.isPresent()) {
                System.out.println(optionalKolobok);
            }
        }
    }

    @AfterAll
    public static void shutdown() {
        HibernateConfig.shutdown();
        System.err.println("close");
    }

}
