package fr.insee.rmes;

public class Test {

    record MyRecord(String myValue) implements InterfaceA, InterfaceB{}

    interface InterfaceA {
        String myValue();
        String methode(String arg);
    }

    interface InterfaceB {
        default String methode(String arg){
            return arg;
        }
    }
}
