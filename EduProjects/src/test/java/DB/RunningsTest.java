package DB;

import DB.Repositories.KolobokRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class RunningsTest {
    static KolobokRepository kolobokRepo;
    static volatile int[] statI;
    static{
        statI = new int[]{0};
        Logger.getLogger("org.hibernate").setLevel(Level.OFF);
        Logger.getLogger("org.postgresql").setLevel(Level.OFF);
        kolobokRepo = new KolobokRepository();
    }

    @Test
    // действительно 10 потоков
    void testPooling() {
        kolobokRepo.testPool(); // действительно 10
    }

    @Test
    // ипользуем i++
    public void threadUnsafeIncrementRunningTest(){
        for (int i = 0; i < 2; i++) {
            int cntKoloboksFinished = threadUnsafeIncrementRunning();
            System.out.println(cntKoloboksFinished);
            System.out.flush();
            if (cntKoloboksFinished != 10)
                break;
        }
    }

    @Test
    // используем AtomicInteger
    public void threadSafeIncrementRunningTest(){
        for (int i = 0; i < 10; i++) {
            int cntKoloboksFinished = threadSafeIncrementRunning();
            System.out.println(cntKoloboksFinished);
            System.out.flush();
            if (cntKoloboksFinished != 10)
                break;
        }
    }

    @Test
    // используем synchronized
    public void synchronizedIncrementRunningTest(){
        for (int i = 0; i < 100; i++) {
            int cntKoloboksFinished = synchronizedIncrementRunning();
            System.out.println(cntKoloboksFinished);
            System.out.flush();
            if (cntKoloboksFinished != 10)
                break;
        }
    }

    @Test
    // используем volatile int[]
    public void volatileIncrementRunningTest(){
        for (int i = 0; i < 100; i++) {
            int cntKoloboksFinished = volatileIncrementRunning();
            System.out.println(cntKoloboksFinished);
            System.out.flush();
            if (cntKoloboksFinished != 10)
                return;
        }
    }

    @Test
    // deadlock в бд
    public void deadlockPairRunningTest(){
        deadlockPairRunning();
    }

    @AfterAll
    public static void shutdown() {
        HibernateConfig.shutdown();
        System.err.println("close");
    }

    static int deadlockPairRunning(){
        final Random random = new Random();
        int kolobokCnt = 2;
        CyclicBarrier barrier = new CyclicBarrier(kolobokCnt);
        try(ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(kolobokCnt+1)){
            // обёртка с блокирующей очередью
            CompletionService<String> pair = new ExecutorCompletionService<>(executor);

            for (int i = 1; i <= kolobokCnt; i++) {
                final int kolobokId = i;
                pair.submit(() -> {
                    Kolobok kolobok = kolobokRepo.findById(Kolobok.class, kolobokId).orElse(new Kolobok());
                    int randInt = random.nextInt(40);
                    barrier.await();

                    Thread.currentThread().sleep(kolobok.getSpeed() + randInt); // тормозит на старте
                    System.err.println(kolobok.getName() + " " + (kolobok.getSpeed()+randInt)  + " finished");
                    return kolobok.getName();
                });
            }

            int curCnt = 0;
            String namePrev = null, name;
            Phaser phaser = new Phaser(1);
            while (curCnt < kolobokCnt) {
                Future<String> future = pair.poll();

                if (future != null) {
                    name = future.get();
                    if (namePrev != null){
                        int idLoser = kolobokRepo.getByName(name).orElse(new Kolobok()).getId();
                        int idWinner = kolobokRepo.getByName(namePrev).orElse(new Kolobok()).getId();
                        System.out.println(idLoser + " " + idWinner);
                        phaser.register();
                        Thread t = new Thread(() -> {
                            System.err.println("start setWinner " + idLoser + " " + idWinner);
                            kolobokRepo.setWinner(idLoser, idWinner);
                            phaser.arrive();
                        });
                        t.start();
                        Thread t2 = new Thread(() -> {
                            System.err.println("start setLoser" + idLoser + " " + idWinner);
                            kolobokRepo.setLoser(idLoser, idWinner);
                            phaser.arrive();
                        });
                        t2.start();
                    }
                    namePrev = name;
                    curCnt++;
                } else {
                    Thread.sleep(100);
                }
            }
            phaser.arriveAndAwaitAdvance();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
    static int volatileIncrementRunning(){
        List<Integer> kolobokIds = Arrays.stream(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}).boxed().collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(kolobokIds, new Random());
        statI[0] = 0;
        System.err.println(kolobokIds);
        try(ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10)){
            Callable<String> task = () -> {
                Kolobok kolobok;
                kolobok = kolobokRepo.findById(Kolobok.class, kolobokIds.get(statI[0]++)).orElse(new Kolobok());
                System.err.println(statI[0] + " " + kolobok);

                if(kolobok != null){
                    Thread.currentThread().sleep(kolobok.getSpeed());
                    System.err.println(kolobok.getName() + " " + kolobok.getSpeed() + " finished");
                }
                return kolobok.getName();
            };
            List<Callable<String>> tasks = IntStream.range(0, 10).mapToObj(j -> task).toList();
            List<Future<String>> koloboksFinished = executor.invokeAll(tasks);

            for (Future<String> future : koloboksFinished) {
                //System.err.print(future.get() + " finished");
                String result = future.get();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return statI[0];
    }
    static int synchronizedIncrementRunning(){
        List<Integer> kolobokIds = Arrays.stream(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}).boxed().collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(kolobokIds, new Random());
        final int[] i = new int[]{0};
        System.err.println(kolobokIds);

        try(ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10)){
            Callable<String> task = () -> {
                Kolobok kolobok;
                synchronized(i){
                    kolobok = kolobokRepo.findById(Kolobok.class, kolobokIds.get(i[0]++)).orElse(new Kolobok());
                    System.err.println(i[0] + " " + kolobok);
                }
                if(kolobok != null){
                    Thread.currentThread().sleep(kolobok.getSpeed());
                    System.err.println(kolobok.getName() + " " + kolobok.getSpeed() + " finished");
                }
                return kolobok.getName();
            };
            List<Callable<String>> tasks = IntStream.range(0, 10).mapToObj(j -> task).toList();
            List<Future<String>> koloboksFinished = executor.invokeAll(tasks);

            for (Future<String> future : koloboksFinished) {
                //System.err.print(future.get() + " finished");
                String result = future.get();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return i[0];
    }
    static int threadSafeIncrementRunning(){
        List<Integer> kolobokIds = Arrays.stream(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}).boxed().collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(kolobokIds, new Random());
        final AtomicInteger[] i = { new AtomicInteger(0) };
        System.err.println(kolobokIds);

        try(ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10)){
            Callable<String> task = () -> {
                Kolobok kolobok = kolobokRepo.findById(Kolobok.class, kolobokIds.get(i[0].getAndIncrement())).orElse(new Kolobok());
                System.err.println(i[0] + " " + kolobok);
                if(kolobok != null){
                    Thread.currentThread().sleep(kolobok.getSpeed());
                    System.err.println(kolobok.getName() + " " + kolobok.getSpeed() + " finished");
                }
                return kolobok.getName();
            };
            List<Callable<String>> tasks = IntStream.range(0, 10).mapToObj(j -> task).toList();
            List<Future<String>> koloboksFinished = executor.invokeAll(tasks);

            for (Future<String> future : koloboksFinished) {
                //System.err.print(future.get() + " finished");
                String result = future.get();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return i[0].get();
    }
    static int threadUnsafeIncrementRunning(){
        List<Integer> kolobokIds = Arrays.stream(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}).boxed().collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(kolobokIds, new Random());
        final int[] i = {0};
        System.err.println(kolobokIds);

        try(ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10)){
            Callable<String> task = () -> {
                Kolobok kolobok = kolobokRepo.findById(Kolobok.class, kolobokIds.get(i[0]++)).orElse(new Kolobok());
                System.err.println(i[0] + " " + kolobok);
                if(kolobok != null){
                    Thread.currentThread().sleep(kolobok.getSpeed());
                    System.err.println(kolobok.getName() + " " + kolobok.getSpeed() + " finished");
                }
                return kolobok.getName();
            };
            List<Callable<String>> tasks = IntStream.range(0, 10).mapToObj(j -> task).toList();
            List<Future<String>> koloboksFinished = executor.invokeAll(tasks);

            for (Future<String> future : koloboksFinished) {
                //System.err.print(future.get() + " finished");
                String result = future.get();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return i[0];
    }
}