package DB.Repositories;
import DB.Calorie;

import java.util.Optional;

public class CalorieRepository extends GenericRepository<Calorie> {

    @Override
    public Optional<Calorie> save(Calorie calorie){
        if (calorie.getFlourTrademark() == null){
            System.out.println("Flour trademark could not be null");
            return Optional.empty();
        }
        if (existByFlourTrademark(calorie.getFlourTrademark())){
            System.out.println(calorie.getFlourTrademark() + "already exist");
            return Optional.empty();
        }
        return saveWithRetry(calorie);
    }

    public boolean existByFlourTrademark(String flour_trademark) {
        return existByUnique(Calorie.class, flour_trademark);
    }
    public Optional<Calorie> getByFlourTrademark(String flour_trademark){
        return getByUnique(Calorie.class, flour_trademark);
    }

    @Override
    public Pair getNameRowAndTable() {
        return new Pair("flour_trademark", "Calorie");
    }
}
