package car.leasing.contracts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ContractsDB {
    private final File file = new File("src/main/resources/contracts.json");
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private volatile boolean isLoading = false;
    private final ExecutorService thread = Executors.newSingleThreadExecutor();
    private final PaymentsDB paymentsDB;

    public ContractsDB(){
        paymentsDB = new PaymentsDB();
        thread.execute(() -> {
            while (!Thread.interrupted()){
                try {
                    taskQueue.take().run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (RuntimeException e){
                    System.out.println("Ошибка: " + e);
                    e.printStackTrace();
                } finally {
                    if (taskQueue.isEmpty())
                        isLoading = false;
                }
            }
        });
    }
    public void readContracts(CopyOnWriteArrayList<LeasingContract> contracts) {
        isLoading = true;
        taskQueue.offer(() -> {
            isLoading = true;
            try {
                if (!file.exists()){
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    new ObjectMapper().writeValue(file, new ArrayList<>());
                }
                else{
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        contracts.addAll(mapper.readValue(file, new TypeReference<List<LeasingContract>>() {}));
                    } catch (JsonMappingException e) {
                        System.out.println("Некорректный файл с договорами");
                        throw e;
                    }
                }
            } catch (IOException e) {
                System.out.println("Не удалось открыть/создать файл с договорами");
                throw new RuntimeException(e);
            } finally {
                isLoading = false;
            }
        });

    }
    public CompletableFuture<Boolean> saveNewContract(CopyOnWriteArrayList<LeasingContract> contracts, LeasingContract contract){
        isLoading = true;
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        taskQueue.offer(() -> {
            isLoading = true;
            if (!paymentsDB.createContractPayments(contract)){
                future.complete(false);
                return;
            }
            contracts.add(contract);
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, contracts);
                System.out.println("Договор успешно добавлен!");
                future.complete(true);
            } catch(IOException e){
                contracts.removeLast();
                System.out.println("Ошибка записи договора в файл: " + e);
                paymentsDB.deleteContractPayments(contract);
                future.complete(false);
            } finally {
                isLoading = false;
            }
        });
        return future;
    }
    public List<Payment> getPayments(LeasingContract contract) {
        return paymentsDB.getPaymentsByContractId(contract);
    }

    public Future<Boolean> payContract(CopyOnWriteArrayList<LeasingContract> contracts, LeasingContract contract){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        taskQueue.offer(() -> {
            if(!paymentsDB.pay(contract)){
                future.complete(false);
                return;
            }
            if (paymentsDB.getPaymentsByContractId(contract).stream()
                    .allMatch(p -> p.getStatus() == Payment.Status.PAID)) {
                contract.setStatus(LeasingContract.Status.CLOSED);
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                    mapper.writeValue(file, contracts);
                    System.out.println("Договор закрыт!");
                    future.complete(true);
                } catch (Exception e) {
                    System.out.println("Ошибка записи в файл с договорами: " + e);
                    paymentsDB.repay(contract);
                    contract.setStatus(LeasingContract.Status.ACTIVE);
                    future.complete(false);
                }
            }
            else {
                future.complete(false);
                System.out.println("Платёж оплачен!");
            }

        });
        return future;
    }
    public boolean isAvailable(){
        return !isLoading;
    }
    public void finish(){
        taskQueue.offer(() ->  Thread.currentThread().interrupt());
        thread.shutdown();
        try {
            thread.awaitTermination(10000000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public Payment getCurrentPaymentByContract(LeasingContract contract) {
        return paymentsDB.getCurrentPaymentByContract(contract);
    }
}
