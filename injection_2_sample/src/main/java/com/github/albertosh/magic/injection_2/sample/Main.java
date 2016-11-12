package com.github.albertosh.magic.injection_2.sample;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class Main {

    public static void main(String[] args) {
        Sample sample = new SampleBuilder()
                .withSomeInteger(2)
                .withSomeString("foo")
                .build();
        assertThat(sample.getSomeInteger(), is(2));
        assertThat(sample.getSomeString(), is(equalTo("foo")));

        Sample aCopy = new SampleBuilder()
                .fromPrototype(sample)
                .build();
        assertThat(aCopy.getSomeInteger(), is(2));
        assertThat(aCopy.getSomeString(), is(equalTo("foo")));

        Sample aModifiedCopy = new SampleBuilder()
                .fromPrototype(sample)
                .withSomeString("bar")
                .build();
        assertThat(aModifiedCopy.getSomeInteger(), is(2));
        assertThat(aModifiedCopy.getSomeString(), is(equalTo("bar")));

        System.out.println("Hooray!");
    }
}
