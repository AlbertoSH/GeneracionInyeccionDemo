package com.github.albertosh.magic.injection_1.sample;

import com.github.albertosh.magic.injection_1.compiler.InjectGetter;

public class InjectedSample {

    @InjectGetter
    private String someString;
    @InjectGetter
    private String anotherString;

    public void setSomeString(String someString) {
        this.someString = someString;
    }

    public void setAnotherString(String anotherString) {
        this.anotherString = anotherString;
    }
}
