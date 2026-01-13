package DB;

import DB.Repositories.KolobokRepository;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    static KolobokRepository kolobokRepo;

    static {
        Logger.getLogger("org.hibernate").setLevel(Level.OFF);
        Logger.getLogger("org.postgresql").setLevel(Level.OFF);
        Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        kolobokRepo = new KolobokRepository();
    }

    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {

        //HibernateConfig config = new HibernateConfig();

        //createKoloboks();
        //fullCreateKoloboks();

        HibernateConfig.shutdown();
    }


}