package com.github.albertosh.magic.injection_1.sample;

import java.lang.reflect.Field;

public class Main {

    public static void main(String[] args) {
        System.out.println("Reflection");
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            Sample sample = new Sample();
            sample.setSomeString("foo");
            printSomeStringValue(sample);

            sample.setSomeString("bar");
            printSomeStringValue(sample);

            sample.setAnotherString("foo");
            printAnotherStringValue(sample);

            sample.setAnotherString("bar");
            printAnotherStringValue(sample);
        }
        long end = System.nanoTime();
        System.out.println("Elapsed nano: " + (end - start));


        System.out.println("Magic");
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            InjectedSample injectedSample = new InjectedSample();
            injectedSample.setSomeString("foo");
            String someStringValue = injectedSample.getSomeString();

            injectedSample.setSomeString("bar");
            someStringValue = injectedSample.getSomeString();

            injectedSample.setAnotherString("foo");
            String anotherStringValye = injectedSample.getAnotherString();

            injectedSample.setAnotherString("bar");
            anotherStringValye = injectedSample.getAnotherString();
        }
        end = System.nanoTime();
        System.out.println("Elapsed nano: " + (end - start));
    }

    private static void printSomeStringValue(Sample sample) {
        printValueByName(sample, "someString");
    }

    private static void printAnotherStringValue(Sample sample) {
        printValueByName(sample, "anotherString");
    }

    private static void printValueByName(Sample sample, String fieldName) {
        // Recover someString value by reflection
        try {
            Field someStringField = sample.getClass().getDeclaredField(fieldName);
            if (!someStringField.isAccessible())
                someStringField.setAccessible(true);
            String someStringValue = (String) someStringField.get(sample);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
