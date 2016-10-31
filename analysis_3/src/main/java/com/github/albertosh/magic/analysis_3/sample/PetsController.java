package com.github.albertosh.magic.analysis_3.sample;

import com.github.albertosh.swagplash.annotations.Api;
import com.github.albertosh.swagplash.annotations.ApiBodyParam;
import com.github.albertosh.swagplash.annotations.ApiOperation;
import com.github.albertosh.swagplash.annotations.ApiPathParam;
import com.github.albertosh.swagplash.annotations.ApiQueryParam;
import com.github.albertosh.swagplash.annotations.ApiResponse;

import java.util.Optional;

@Api
public class PetsController {

    @ApiOperation(
            description = "Returns all pets from the system that the user has access to",
            httpMethod = ApiOperation.HttpMethod.GET,
            path = "/pets",
            produces = {
                    "application/json",
                    "application/xml",
                    "text/xml",
                    "text/html"})
    @ApiResponse(
            code = 200,
            message = "Pet response",
            responseContainer = "List",
            response = Pet.class
    )
    @ApiResponse(
            message = "Unexpected error",
            response = ErrorModel.class
    )
    public void findPets(
            @ApiQueryParam("Tags to filter by") String[] tags,
            @ApiQueryParam Integer limit) {
        // ...
    }



    @ApiOperation(
            description = "Creates a new pet in the store. Duplicates are allowed",
            httpMethod = ApiOperation.HttpMethod.POST,
            path = "/pets",
            produces = {
                    "application/json",
                    "application/xml",
                    "text/xml",
                    "text/html"})
    @ApiBodyParam(name = "id", dataType = ApiBodyParam.DataType.INT, required = true)
    @ApiBodyParam(name = "name", required = true)
    @ApiBodyParam(name = "tag")
    @ApiResponse(
            code = 200,
            message = "Pet response",
            response = Pet.class
    )
    @ApiResponse(
            message = "Unexpected error",
            response = ErrorModel.class
    )
    public void addPet() {
        // ...
    }



    @ApiOperation(
            description = "Returns a user based on a single ID, if the user does not have access to the pet",
            httpMethod = ApiOperation.HttpMethod.GET,
            path = "/pets/{id}",
            produces = "application/json")
    @ApiResponse(
            code = 200,
            message = "Pet response",
            response = Pet.class
    )
    @ApiResponse(
            message = "Unexpected error",
            response = ErrorModel.class
    )
    public void findPetById(
            @ApiPathParam Long id
    ) {
        // ...
    }


    @ApiOperation(
            description = "deletes a single pet based on the ID supplied",
            httpMethod = ApiOperation.HttpMethod.DELETE,
            path = "/pets/{id}")
    @ApiResponse(
            code = 204,
            message = "Pet deleted"
    )
    @ApiResponse(
            message = "Unexpected error",
            response = ErrorModel.class
    )
    public void deletePet(
            @ApiPathParam Long id
    ) {
        // ...
    }
}
