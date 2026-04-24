package com.payflow.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Currency;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule currencyModule = new SimpleModule();
        currencyModule.addSerializer(Currency.class, new JsonSerializer<>() {
            @Override
            public void serialize(Currency value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {
                gen.writeString(value.getCurrencyCode());
            }
        });
        currencyModule.addDeserializer(Currency.class, new JsonDeserializer<>() {
            @Override
            public Currency deserialize(JsonParser p, DeserializationContext ctx)
                    throws IOException {
                return Currency.getInstance(p.getText());
            }
        });

        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(currencyModule)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}