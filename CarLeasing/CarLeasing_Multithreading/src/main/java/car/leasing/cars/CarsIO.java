package car.leasing.cars;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CarsIO {
    private File file = new File("src/main/resources/cars.json");
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private volatile boolean isLoading = false;
    private ExecutorService thread = Executors.newSingleThreadExecutor();

    public CarsIO(){
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
    public void readCars(CopyOnWriteArrayList<Car> cars) {
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
                        cars.addAll(mapper.readValue(file, new TypeReference<List<Car>>() {
                        }));
                    } catch (JsonMappingException e) {
                        System.out.println("Некорректный файл с машинами");
                        throw e;
                    }
                }
            } catch (IOException e) {
                System.out.println("Не удалось открыть/создать файл с машинами");
                throw new RuntimeException(e);
            }
        });
    }
    public void saveNewCar(CopyOnWriteArrayList<Car> cars, Car car){
        isLoading = true;
        taskQueue.offer(() -> {
            isLoading = true;
            //Main.sleep(10000);
            cars.add(car);
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, cars);
                System.out.println("Автомобиль успешно добавлен!");
            } catch(IOException e2){
                cars.removeLast();
                System.out.println("Ошибка записи в файл: " + e2);
            }
        });
    }
    public Future<Boolean> setStatusCars(CopyOnWriteArrayList<Car> cars, Car car, Car.Status status) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        isLoading = true;
        taskQueue.offer(() -> {
            isLoading = true;
            Car carInGarage = cars.stream()
                    .filter(c -> car.getVIN().equals(c.getVIN()))
                    .findFirst().get();
            Car.Status oldStatus = carInGarage.getStatus();
            carInGarage.setStatus(status);
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, cars);
                future.complete(true);
            } catch (IOException e) {
                System.out.println("Ошибка записи в файл с клиентами: " + e);
                carInGarage.setStatus(oldStatus);
                future.complete(false);
            }
        });
        return future;
    }

    public boolean isAvailable(){
        return !isLoading;
    }
    public ExecutorService getThread(){
        return thread;
    }

    public void finish() {
        taskQueue.offer(() -> Thread.currentThread().interrupt());
        thread.shutdown();
        try {
            thread.awaitTermination(10000000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}