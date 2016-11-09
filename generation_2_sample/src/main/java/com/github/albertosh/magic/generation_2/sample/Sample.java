package com.github.albertosh.magic.generation_2.sample;

import com.github.albertosh.magic.generation_2.compiler.Builder;

@Builder
public class Sample {

    private String someString;
    private int someInteger;

    Sample(SampleBuilder builder) {
        this.someString = builder.getSomeString();
        this.someInteger = builder.getSomeInteger();
    }

    public String getSomeString() {
        return someString;
    }

    public int getSomeInteger() {
        return someInteger;
    }
}
