package car.leasing.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ClientBaseIO {
    private File file = new File("src/main/resources/clients.json");
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private volatile boolean isLoading = false;
    private ExecutorService thread = Executors.newSingleThreadExecutor();

    public ClientBaseIO(){
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
    public void readClients(CopyOnWriteArrayList<Client> clients) {
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
                        clients.addAll(mapper.readValue(file, new TypeReference<List<Client>>() {}));
                    } catch (JsonMappingException e) {
                        System.out.println("Некорректный файл с клиентами");
                        throw e;
                    }
                }
            } catch (IOException e) {
                System.out.println("Не удалось открыть/создать файл с клиентами");
                throw new RuntimeException(e);
            }
        });
    }
    public void saveNewClient(CopyOnWriteArrayList<Client> clients, Client client){
        isLoading = true;
        taskQueue.offer(() -> {
            isLoading = true;
            //Main.sleep(10000);
            clients.add(client);
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, clients);
                System.out.println("Клиент успешно добавлен!");
            } catch(IOException e2){
                System.out.println("Ошибка записи в файл: " + e2);
                clients.removeLast();
            }
        });
    }
    public boolean isAvailable(){
        return !isLoading;
    }

    public void finish() {
        taskQueue.offer(() ->  Thread.currentThread().interrupt());
        thread.shutdown();
        try {
            thread.awaitTermination(10000000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
