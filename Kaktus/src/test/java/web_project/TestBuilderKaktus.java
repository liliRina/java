package web_project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import web_project.domain.Kaktus;

public class TestBuilderKaktus {
    @Test
    @DisplayName("Тест создания кактуса")
    void buildKaktus(){
        Kaktus kaktus = Kaktus.builder().name("kaktus1").plant();
        Kaktus kaktus2 = Kaktus.builder().name("kaktus2").plant();
        System.out.println(kaktus);
        System.out.println(kaktus2);
    }
}