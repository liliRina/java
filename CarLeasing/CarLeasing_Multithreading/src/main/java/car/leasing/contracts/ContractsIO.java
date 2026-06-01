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

public class ContractsIO {
    private File file = new File("src/main/resources/contracts.json");
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private volatile boolean isLoading = false;
    private ExecutorService thread = Executors.newSingleThreadExecutor();

    public ContractsIO(){
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
            //Main.sleep(10000);
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
    public void saveNewContract(CopyOnWriteArrayList<LeasingContract> contracts, LeasingContract contract){
        isLoading = true;
        taskQueue.offer(() -> {
            isLoading = true;
            //Main.sleep(15000);
            contracts.add(contract);
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, contracts);
                System.out.println("Договор успешно добавлен!");
            } catch(IOException e2){
                contracts.removeLast();
                System.out.println("Ошибка записи в файл: " + e2);
            } finally {
                isLoading = false;
            }
        });
    }
    public Future<Boolean> payContract(CopyOnWriteArrayList<LeasingContract> contracts, LeasingContract contract){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        taskQueue.offer(() -> {
            //Main.sleep(15000);
            contract.pay();
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, contracts);
                System.out.println("Платёж оплачен!");
                future.complete(true);
            } catch (Exception e) {
                System.out.println("Ошибка записи в файл: " + e);
                contract.repay();
                future.complete(false);
            }
        });
        return future;
    }
    public Future<Boolean> returnPayContract(CopyOnWriteArrayList<LeasingContract> contracts, LeasingContract contract) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        taskQueue.offer(() -> {
            contract.repay();
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, contracts);
                System.out.println("Платёж оплачен!");
                future.complete(true);
            } catch (Exception e) {
                contract.pay();
                System.out.println("Ошибка записи в файл: " + e);
                future.complete(false);
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
}
