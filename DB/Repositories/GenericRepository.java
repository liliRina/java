package DB.Repositories;

import DB.EntityFromTable;
import DB.HibernateConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Optional;

record Pair(String first, String second) {}

public abstract class GenericRepository <T> {
    private static final EntityManagerFactory managerFactory = HibernateConfig.getManagerFactory();

    public Optional<T> findById(Class<T> entityClass, int id){
        EntityManager em = null;
        try{
            em = managerFactory.createEntityManager();
            T entity = em.find(entityClass, id); // не бросает NoResultException
            return Optional.ofNullable(entity);
        } catch (RuntimeException e) {
            System.out.println(e); // проблемы с бд
            return Optional.empty();
        } finally {
            if (em != null && em.isOpen())
                em.close();
        }
    }

    public abstract Optional<T> save(T entity);
    public Optional<T> saveWithRetry(T entity) {
        EntityManager em = null;
        int maxRetries = 4;
        try{
            em = managerFactory.createEntityManager();
            for (int i = 0; i < maxRetries; i++) {
                try {
                    em.getTransaction().begin();
                    em.persist(entity);
                    em.getTransaction().commit();
                    System.out.println("Saved successfully");
                    return Optional.of(entity);
                } catch (RuntimeException e) {
                    if (em.getTransaction().isActive())
                        em.getTransaction().rollback();

                    System.out.println(e);
                    em.clear(); // в кэше могут быть проблемы

                    if (e instanceof ConstraintViolationException)
                        return Optional.empty();

                    try{
                        Thread.sleep(100); // Ждём и пробуем снова
                    }
                    catch (InterruptedException interE){
                        Thread.currentThread().interrupt();
                        System.out.println(interE);
                        return Optional.empty();
                    }
                }
            }
        } finally {
            if (em != null && em.isOpen())
                em.close();
        }
        return Optional.empty();
    }

    public boolean existByUnique(Class<T> entityClass, String uniqueValue){
        Optional<T> value = getByUnique(entityClass, uniqueValue);
        return value.isPresent();
    }
    public Optional<T> getByUnique(Class<T> entityClass, String uniqueValue){
        if (!EntityFromTable.class.isAssignableFrom(entityClass))
            return Optional.empty();
        EntityManager em = null;

        Pair rowTable = getNameRowAndTable();
        try {
            em = managerFactory.createEntityManager();
            T entity = em.createQuery(
                    "SELECT t FROM " +
                            rowTable.second() +
                            " t WHERE t." +
                            rowTable.first() +
                            " =:" +
                            rowTable.first(),
                    entityClass)
                    .setParameter(rowTable.first(), uniqueValue)
                    .getSingleResult();
            return Optional.ofNullable(entity);
        }
        catch (NoResultException e) {
            System.out.println(e);
            return Optional.empty();
        }
        catch (RuntimeException e){
            System.out.println(e);
            return Optional.empty();
        }
        finally {
            if (em != null && em.isOpen())
                em.close();
        }
    }

    public abstract Pair getNameRowAndTable();

}
