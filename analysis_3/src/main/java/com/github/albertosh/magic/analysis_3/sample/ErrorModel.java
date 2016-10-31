package com.github.albertosh.magic.analysis_3.sample;

import com.github.albertosh.swagplash.annotations.ApiModel;
import com.github.albertosh.swagplash.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ApiModel
public class ErrorModel {

    @Nonnull @ApiModelProperty
    public Integer id;

    @Nonnull @ApiModelProperty
    public String message;


}
