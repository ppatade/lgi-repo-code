package com.redhat.demo.asyncroutingdemo;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import java.sql.Timestamp; 
import java.time.LocalDate;
import java.util.UUID;

import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;


@Component
public class SimpleRouteBuilder extends RouteBuilder {

    private final Environment env;

    public SimpleRouteBuilder(Environment env) {
        this.env = env;
    }

    @Override
    public void configure() throws Exception {

        restConfiguration()
        .component("servlet");

        rest()
        .consumes(MediaType.APPLICATION_JSON_VALUE)
        .produces(MediaType.APPLICATION_JSON_VALUE)

        .post("/motor").to("direct:async-quote");

        from("direct:async-quote")
        .wireTap("direct:wiretap-quotelo-req")
        .choice()
            .when(simple("${header.client} == 'policybazaar'"))
                .to("atlasmap:maps/policybazaar.adm")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to("https://63cab6c4f36cbbdfc75e8aa1.mockapi.io/Motor")
                .convertBodyTo(String.class)
            .when(simple("${header.client}=='coverfox'"))
                .to("atlasmap:maps/policybazaar.adm")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to("https://63cab6c4f36cbbdfc75e8aa1.mockapi.io/Motor")
                .convertBodyTo(String.class)
            .otherwise()
                .to("atlasmap:maps/policybazaar.adm")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to("https://63cab6c4f36cbbdfc75e8aa1.mockapi.io/Motor")
                .convertBodyTo(String.class);;

    }
}
