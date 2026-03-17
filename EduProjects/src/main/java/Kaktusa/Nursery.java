package Kaktusa;

import java.util.*;
import java.util.stream.Collectors;

public class Nursery {
    private ArrayList<Kaktus> kaktusa;
    public void addKaktus(Kaktus kaktus){
        kaktusa.add(kaktus);
    }

    public Optional<Kaktus> goToShop(){
        if (kaktusa.size() == 0)
            return Optional.empty();
        return Optional.of(removeByInd(0));
    }

    public Optional<Kaktus> findKaktus(String name){
        int firstInd = kaktusa.indexOf(new Kaktus(name));
        int lastInd = kaktusa.lastIndexOf(new Kaktus(name));
        return firstInd != -1 ? Optional.of(kaktusa.get(firstInd)) : Optional.empty();
    }
    public boolean remove(Kaktus kaktus){
        return kaktusa.remove(kaktus);
    }
    public Kaktus removeByInd(int ind){
        return kaktusa.remove(ind);
    }
    public Kaktus ChangeKaktus(int ind, Kaktus newKaktus){
        try{
            return kaktusa.set(ind, newKaktus);
        }
        catch(IndexOutOfBoundsException e){
            System.out.println("Нет такого индекса");
            return null;
        }
    }
    public List<Kaktus> FlowerKaktus(){
        return kaktusa.stream()
                .filter(k -> k.hasFlower)
                .collect(ArrayList::new, List::add, List::addAll);
                //.collect(Collectors.toCollection(ArrayList::new));
                // .collect(Collectors.toList());
    }
    public List<Kaktus> findByHeight(int height){
        return Arrays.stream((kaktusa)
                .toArray(new Kaktus[kaktusa.size()]))
                .filter(k -> k.height > height)
                .collect(ArrayList::new, List::add, List::addAll);
    }
    public List<Kaktus> findByCountOwners(int countOwners){

        return Arrays.stream(kaktusa.toArray(new Kaktus[kaktusa.size()]))
                .map(k -> k.owners.size() == countOwners ? k : null)
                .reduce(new ArrayList<Kaktus>(),
                    (listK, k) -> {
                    listK.add(k);
                    return listK;
                }, (listK1, listK2) -> {
                    listK1.addAll(listK2);
                    return listK1;
                });
    }
    public void sortByName(){
        Collections.sort(kaktusa, Comparator.comparing(k -> k.name));
    }
    public void sortByHeight(){
        kaktusa = (ArrayList<Kaktus>) Arrays.stream(kaktusa.toArray(new Kaktus[kaktusa.size()]))
                //.sorted((k1, k2) -> Double.compare(k1.height, k2.height))
                .sorted(Comparator.comparingDouble(k -> k.height))
                .toList(); // возвращает неизменяемую коллекцию ( не эрей). Лучше не надо
    }
    public void sortedByFlower(){
        kaktusa.sort(Comparator.comparing(k -> k.hasFlower));
    }
    public double avgHeight(){
        return kaktusa.stream()
                .mapToDouble(k -> k.height)
                .summaryStatistics().getAverage();
    }
    public double maxHeight(){
        return kaktusa.stream()
                .mapToDouble(k -> k.height)
                .summaryStatistics().getMax();
    }
    public String nameAll(){
        return kaktusa.stream()
                .map(k -> k.name)
                .collect(Collectors.joining(", "));
    }
    public boolean throwBadKaktus(){
        return kaktusa.removeIf(k -> k.height < 10); // несколько может

    }
    public void throwBadKaktusIteration(){
        Iterator<Kaktus> it = kaktusa.iterator();
        while(it.hasNext()){
            Kaktus kaktus = it.next();
            if (kaktus.height < 10)
                it.remove();

        }
    }
}
