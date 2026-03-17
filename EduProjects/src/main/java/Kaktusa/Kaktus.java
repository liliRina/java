package Kaktusa;

import java.util.List;

public class Kaktus {
    String name;
    double height;
    boolean hasFlower;
    List<String> owners;

    Kaktus(String name){
        this.name = name;
    }

    @Override
    public boolean equals(Object obj){ // 5 правил
        owners.toArray();
        if (this == obj)
            return true;
        if (obj instanceof Kaktus kaktus)
            return name.equals(kaktus.name);
        return false;
    }
}

