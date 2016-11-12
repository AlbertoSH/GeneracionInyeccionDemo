package com.github.albertosh.magic.injection_2.sample;

import com.github.albertosh.magic.injection_2.compiler.Builder;

@Builder
public class Sample {

    private final String someString;
    private final int someInteger;

    public Sample(String someString, int someInteger) {
        this.someString = someString;
        this.someInteger = someInteger;
    }

    public String getSomeString() {
        return someString;
    }

    public int getSomeInteger() {
        return someInteger;
    }
}
