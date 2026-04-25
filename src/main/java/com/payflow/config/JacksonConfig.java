package com.payflow.config;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Currency;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule currencyModule = new SimpleModule();
        currencyModule.addSerializer(Currency.class, new StdSerializer<>(Currency.class) {
            @Override
            public void serialize(Currency value, JsonGenerator gen, SerializationContext provider) {
                gen.writeString(value.getCurrencyCode());
            }
        });
        currencyModule.addDeserializer(Currency.class, new StdDeserializer<>(Currency.class) {
            @Override
            public Currency deserialize(JsonParser p, DeserializationContext ctx) {
                return Currency.getInstance(p.getString());
            }
        });

        return JsonMapper.builder()
                .findAndAddModules()
                .addModule(currencyModule)
                .build();
    }
}