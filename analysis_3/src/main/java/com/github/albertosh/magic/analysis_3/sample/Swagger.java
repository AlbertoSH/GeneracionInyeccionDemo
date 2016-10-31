package com.github.albertosh.magic.analysis_3.sample;

import com.github.albertosh.swagplash.annotations.Contact;
import com.github.albertosh.swagplash.annotations.Info;
import com.github.albertosh.swagplash.annotations.License;
import com.github.albertosh.swagplash.annotations.SwaggerDefinition;

@SwaggerDefinition(
        info = @Info(
                title = "Swagger Petstore (Simple)",
                version = "1.0.0",
                description = "A sample API that uses a petstore as an example to" +
                        " demonstrate features in the swagger-2.0 specification",
                termsOfService = "http://helloreverb.com/terms/",
                contact = @Contact(
                        name = "Swagger API team",
                        email = "foo@example.com",
                        url = "http://swagger.io"
                ),
                license = @License(
                        name = "MIT",
                        url = "http://opensource.org/licenses/MIT"
                )
        ),
        host = "petstore.swagger.io",
        basePath = "/api",
        schemes = SwaggerDefinition.Scheme.HTTP,
        consumes = {"application/json"},
        produces = {"application/json"}
)
public class Swagger {
}
