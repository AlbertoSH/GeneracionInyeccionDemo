package com.github.albertosh.magic.analysis_2.sample;

import com.github.albertosh.magic.analysis_2.compiler.Toothpick;

public class Sample2 {

    @Toothpick
    public Sample2(Sample3 sample3) {}

    public Sample2() {
        this(null);
    }
}
