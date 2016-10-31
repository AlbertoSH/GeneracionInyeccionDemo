package com.github.albertosh.magic.analysis_3.sample;

import com.github.albertosh.swagplash.annotations.ApiModel;
import com.github.albertosh.swagplash.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ApiModel
public class NewPet {

    @Nonnull @ApiModelProperty
    public Long id;

    @Nonnull @ApiModelProperty
    public String name;

    @Nullable @ApiModelProperty
    public String tag;

}
