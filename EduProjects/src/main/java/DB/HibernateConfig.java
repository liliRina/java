package DB;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.cfg.Configuration;
import jakarta.persistence.EntityManagerFactory;

import lombok.Getter;

public class HibernateConfig {
    static final String url = "jdbc:postgresql://localhost:5432/koloboks";
    static final String user = "postgres";
    static final String password = "12345"; // твой пароль

    @Getter
    private static final EntityManagerFactory managerFactory;

    static {
        try {
            managerFactory = setHibernateConfig(setHikariConfig());
        } catch (Exception e) {
            System.out.println(e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private static HikariDataSource setHikariConfig(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10); // макс подключений в пуле
        config.setMinimumIdle(1);      // минимальное количество простаивающих
        config.setConnectionTimeout(500000); // таймаут на получение подключения
        config.setIdleTimeout(600000); // время простаивания до закрытия
        config.setMaxLifetime(1800000); // макс время жизни подключения
        return new HikariDataSource(config);
    }
    private static EntityManagerFactory setHibernateConfig(HikariDataSource dataSource){
        Configuration hibernateConfig = new Configuration();

        hibernateConfig.getProperties().put("hibernate.connection.datasource", dataSource);
        hibernateConfig.setProperty("hibernate.connection.pool_size", "10");

        hibernateConfig.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        hibernateConfig.setProperty("hibernate.hbm2ddl.auto", "update");
        //hibernateConfig.setProperty("hibernate.hbm2ddl.auto", "create");

        hibernateConfig.setProperty("hibernate.show_sql", "false");
        hibernateConfig.setProperty("hibernate.format_sql", "false");
        hibernateConfig.setProperty("hibernate.use_sql_comments", "false");

        hibernateConfig.addAnnotatedClass(Kolobok.class);
        hibernateConfig.addAnnotatedClass(Calorie.class);

        EntityManagerFactory emf = hibernateConfig.buildSessionFactory();
        return emf;
    }

    public static void shutdown() {
        if (managerFactory != null) {
            managerFactory.close();
        }
    }
}