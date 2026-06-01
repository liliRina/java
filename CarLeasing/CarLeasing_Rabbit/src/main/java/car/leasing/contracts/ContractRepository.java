package car.leasing.contracts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import car.leasing.contracts.domain.LeasingContract;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<LeasingContract, Long> {

    Optional<LeasingContract> findByContractId(Long contractId);
    List<LeasingContract> findByClientPassportNumber(String passportNumber);
    List<LeasingContract> findByClientPassportNumberAndStatus(String passportNumber, LeasingContract.Status status);
    boolean existsByClientPassportNumberAndStatus(String passportNumber, LeasingContract.Status status);

    @Modifying
    @Query("UPDATE LeasingContract c SET c.status = :status WHERE c.contractId = :Id")
    int updateStatus(@Param("Id") Long contractId, @Param("status") LeasingContract.Status status);
}
