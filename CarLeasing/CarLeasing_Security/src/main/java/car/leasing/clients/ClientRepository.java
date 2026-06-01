package car.leasing.clients;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import car.leasing.clients.domain.Client;


import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByFullName(String fullName);
    Optional<Client> findByPassportNumber(String passportNumber);
    Optional<Client> findByPhoneNumber(String phoneNumber);
    Optional<Client> findByLogin(String login);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Client c WHERE c.passportNumber = :passport")
    Optional<Client> findByPassportNumberForUpdate(@Param("passport") String passport);



    @Modifying
    @Query("UPDATE Client c SET c.status = :status WHERE c.passportNumber = :passport")
    void updateStatus(@Param("passport") String passport, @Param("status") Client.Status status);

    @Modifying
    @Query("DELETE FROM Client c WHERE c.passportNumber = :passport AND c.status = 'NotActiveContract'")
    void deleteByPassportAndStatusNot(@Param("passport") String passport);

    boolean existsByPassportNumber(String passportNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsClientByLogin(String login);

    boolean existsByLogin(String login);

}