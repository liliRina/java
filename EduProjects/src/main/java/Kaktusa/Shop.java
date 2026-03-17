package Kaktusa;

import java.util.*;

public class Shop {
    Nursery nursery = new Nursery();
    LinkedList<String> customers = new LinkedList<>();
    HashSet<Kaktus> kaktusa = new HashSet<>();
    LinkedHashSet<Kaktus> wateringLine = new LinkedHashSet<>();
    TreeSet<Kaktus> kaktusaByHeight = new TreeSet<>(Comparator.comparing(k -> k.height));

    void getNewKaktus(){
        nursery.goToShop().ifPresent(kaktusa::add);
    }
    boolean hasKaktusByName(String name){
        //return kaktusa.contains(new Kaktus(name));
        return kaktusa.stream()
                .anyMatch(k -> k.name.equals(name));
    }

    void goWateringLine(Kaktus kaktus){
        wateringLine.add(kaktus);
    }
    private void waterKaktus(){
        Iterator<Kaktus> it = wateringLine.iterator();
        if (it.hasNext()){
            Kaktus kaktus = it.next();
            kaktus.height += 10;
            it.remove();
        }
    }
    public void waterAllKaktus(){
        while(wateringLine.size() != 0)
            waterKaktus();
    }

    public void ChooseKaktusTaller(int height){
        Kaktus kaktus = new Kaktus("default");
        kaktus.height = height;
        kaktusaByHeight.tailSet(kaktus, true); //headSet
    }
    public void ChooseKaktusIntervalH(int firstHeight, int secondHeight){
        Kaktus kaktusSmaller = new Kaktus("default");
        kaktusSmaller.height = firstHeight;
        Kaktus kaktusTaller = new Kaktus("default");
        kaktusTaller.height = secondHeight;
        kaktusaByHeight.subSet(kaktusSmaller, true, kaktusTaller, false);
    }
    public Kaktus ChooseKaktusByHeight(int height){
        Kaktus kaktus = new Kaktus("default");
        kaktus.height = height;
        return kaktusaByHeight.ceiling(kaktus); // == меньший, который больше kaktus
                            //.higher(kaktus);   без ==
                            //.floor
                            //.lower

    }
    public Kaktus takeSmallestKaktus(){
        return kaktusaByHeight.pollFirst(); // Last  // first, last - без удаления
    }

    boolean comeCustomer(String name){
        return customers.offerLast(name); // если ограниченный дек, то выдаст просто false, а addLast умрёт
    }
    String serveCustomer(){
        return customers.pollFirst(); // не упадёт
    }

    String whoLast(){
        return customers.peekLast();
    }
    String whoFirst(){
        return customers.peekFirst();
    }
}
