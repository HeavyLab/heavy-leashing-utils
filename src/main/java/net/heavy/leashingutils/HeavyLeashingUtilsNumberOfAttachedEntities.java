package net.heavy.leashingutils;

public class HeavyLeashingUtilsNumberOfAttachedEntities {
    private static HeavyLeashingUtilsNumberOfAttachedEntities INSTANCE;
    private int numberOfAttachedEntities;

    private HeavyLeashingUtilsNumberOfAttachedEntities() {
        numberOfAttachedEntities = 0;
    }


    public static HeavyLeashingUtilsNumberOfAttachedEntities getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new HeavyLeashingUtilsNumberOfAttachedEntities();
        }

        return INSTANCE;
    }

    public void increment() {
        this.numberOfAttachedEntities = numberOfAttachedEntities + 1;
    }

    public void decrement() {
        this.numberOfAttachedEntities = numberOfAttachedEntities - 1;
    }

    public int getNumberOfAttachedEntities() {
        return numberOfAttachedEntities;
    }
}
