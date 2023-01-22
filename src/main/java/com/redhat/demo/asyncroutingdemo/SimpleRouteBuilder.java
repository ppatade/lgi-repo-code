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
            .when(header("client").isNotNull())
                .toD("atlasmap:maps/${header.client}.adm")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to("https://lgi-backend-git-lgi-poc-quote.apps.cluster-fzgbp.fzgbp.sandbox1096.opentlc.com/Motor?bridgeEndpoint=true")
                .convertBodyTo(String.class)
            .otherwise()
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to("https://lgi-backend-git-lgi-poc-quote.apps.cluster-fzgbp.fzgbp.sandbox1096.opentlc.com/Motor?bridgeEndpoint=true")
                .convertBodyTo(String.class);

    }
}
