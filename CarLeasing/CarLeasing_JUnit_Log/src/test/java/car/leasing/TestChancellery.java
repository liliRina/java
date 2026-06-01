package car.leasing;

import car.leasing.contracts.Chancellery;
import car.leasing.contracts.LeasingContract;
import car.leasing.contracts.Payment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestChancellery {
    static ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Mock
    Scanner mockScanner;
    @InjectMocks
    MainHandler handler;

    @BeforeAll
    static void setUp(){
        System.setOut(new PrintStream(baos));
    }

    @ParameterizedTest
    @CsvSource({
            "false, ACTIVE, 0, 0, 0, 'По этому договору уже обрабатывается платёж, подождите'",
            "true, CLOSED, 0, 0, 0, 'Договор уже закрыт'",
            "true, ACTIVE, 0, 0, 0.000000, ''",
            "true, ACTIVE, 1, 54, 54.000000, 'Платёж принят'"
    })
    @DisplayName("Внесение платежа с консоли")
    void testGetUserPayment(
            boolean tryStartPaymentResult, // false - не получается захватить лок - уже есть платёж
            LeasingContract.Status status,
            int expectedCntContractsInProcess,
            BigDecimal readPaymentResult,
            BigDecimal expectedReturn,
            String expectedMessage
    ) {
        // полностью мокируем LeasingContract
        LeasingContract contract = mock(LeasingContract.class);
        Mockito.lenient().when(contract.tryStartPayment()).thenReturn(tryStartPaymentResult);
        Mockito.lenient().when(contract.getStatus()).thenReturn(status);

        // мокируем 1 метод chancellery
        Chancellery chancellery = spy(new Chancellery());
        Mockito.lenient()
                .doReturn(new Payment(1, 1, BigDecimal.valueOf(1000.0)))
                .when(chancellery).getCurrentPayment(any(LeasingContract.class));

        Mockito.lenient().when(mockScanner.nextLine())
                .thenReturn(readPaymentResult.toString())
                .thenReturn(readPaymentResult.toString())
                .thenReturn(readPaymentResult.toString())
                .thenReturn(readPaymentResult.toString());

        BigDecimal result = chancellery.getUserPayment(contract);
        assertEquals(expectedReturn, result);

        String output = baos.toString();
        if (!expectedMessage.isEmpty())
            assertTrue(output.contains(expectedMessage));

        try {
            Field counterField = Chancellery.class.getDeclaredField("cntContractsInProcess");
            counterField.setAccessible(true);
            AtomicInteger counter = (AtomicInteger) counterField.get(chancellery);
            assertEquals(expectedCntContractsInProcess, counter.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}