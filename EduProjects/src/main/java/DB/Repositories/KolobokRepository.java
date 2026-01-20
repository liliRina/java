package DB.Repositories;

import DB.HibernateConfig;
import DB.Kolobok;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;


public class KolobokRepository extends GenericRepository<Kolobok> {
    private static final EntityManagerFactory managerFactory = HibernateConfig.getManagerFactory();

    @Override
    public Optional<Kolobok> save(Kolobok kolobok){
        if (kolobok.getName() == null){
            System.out.println("Name could not be null");
            return Optional.empty();
        }
        if (existByName(kolobok.getName())){
            System.out.println(kolobok.getName() + " already exist");
            return Optional.empty();
        }
        return saveWithRetry(kolobok);
    }

    public boolean existByName(String name) {
        return existByUnique(Kolobok.class, name);
    }
    public Optional<Kolobok> getByName(String name) {
        return getByUnique(Kolobok.class, name);
    }

    @Override
    public Pair getNameRowAndTable() {
        return new Pair("name", "Kolobok");
    }
    public void setWinner(int idLoser, int idWinner) {
       try(EntityManager em = managerFactory.createEntityManager()) {
            EntityTransaction et = em.getTransaction();
            try {
                et.begin();

                Kolobok kolobokLoser = em.find(Kolobok.class, idLoser, LockModeType.PESSIMISTIC_WRITE);
                System.err.println("get idLoser" + kolobokLoser);
                Kolobok kolobokWinner = em.find(Kolobok.class, idWinner, LockModeType.PESSIMISTIC_WRITE);
                System.err.println("get idWinner"  + kolobokWinner);

                kolobokLoser.setRunnerAhead(kolobokWinner.getName());
                Thread.sleep(500);
                et.commit();
            } catch (InterruptedException e) {
                System.err.println(e);
                if (et != null && et.isActive()) {
                    et.rollback();
                }
            } catch (Exception e){
                System.out.println(e);
                if (et != null && et.isActive()) {
                    et.rollback();
                }
            }
        }
        System.out.println(idLoser + " " + idWinner);
    }
    public void setLoser(int idLoser, int idWinner) {
        try(EntityManager em = managerFactory.createEntityManager()) {
            EntityTransaction et = em.getTransaction();
            try {
                et.begin();

                Kolobok kolobokWinner = em.find(Kolobok.class, idWinner, LockModeType.PESSIMISTIC_WRITE);
                System.err.println("get idWinner"  + kolobokWinner);
                Kolobok kolobokLoser = em.find(Kolobok.class, idLoser, LockModeType.PESSIMISTIC_WRITE);
                System.err.println("get idLoser" + kolobokLoser);

                kolobokWinner.setRunnerAhead(kolobokWinner.getName());
                Thread.sleep(500);
                et.commit();
            } catch (InterruptedException e) {
                System.err.println(e);
                if (et != null && et.isActive()) {
                    et.rollback();
                }
            } catch (PersistenceException e){
                System.out.println(e);
                if (et != null && et.isActive()) {
                    et.rollback();
                }
            }
        }
    }

    public void testPool() {
        ExecutorService executor = Executors.newFixedThreadPool(40);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            tasks.add(() -> {
                System.err.println("1. Создание em");
                EntityManager em = managerFactory.createEntityManager();

                System.err.println("1. Создание em");
                em.getTransaction().begin();
                em.createQuery("SELECT k FROM Kolobok k").getResultList();

                System.err.println("3. Сон");
                Thread.sleep(2000);

                System.err.println("4. Закрывание EM");
                em.getTransaction().commit();
                em.close();
                return null;
            });
        }
        try {
            // Блокирует поток
            List<Future<Void>> futures = executor.invokeAll(tasks, 20, TimeUnit.SECONDS);
            System.out.println("Все задачи завершены");
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

}
