package car.leasing.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {

    @Query("SELECT p FROM Payment p WHERE p.id.contractId = :contractId")
    List<Payment> findByIdContractId(Long contractId);

    @Query("SELECT p FROM Payment p WHERE p.id.contractId = :contractId AND " +
            "p.status = 'UNPAID' ORDER BY p.id.paymentNumber LIMIT 1")
    Optional<Payment> findCurrentPaymentByContractId(@Param("contractId") Long contractId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Payment p SET p.status = 'PAID' WHERE p.id.contractId = :contractId AND " +
            "p.id.paymentNumber = (SELECT MIN(p2.id.paymentNumber) FROM Payment p2 WHERE p2.id.contractId = :contractId AND p2.status = 'UNPAID')")
    int payCurrentPayment(@Param("contractId") Long contractId);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.id.contractId = :contractId")
    void deleteByContractId(@Param("contractId") Long contractId);
}