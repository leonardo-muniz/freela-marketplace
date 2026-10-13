package br.com.freela.contrato.infrastructure.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    NewTopic contratos() {
        return new NewTopic("contratos.v1", 3, (short) 1);
    }

    @Bean
    NewTopic contratosDlt() {
        return new NewTopic("contratos.v1.dlt", 3, (short) 1);
    }
}
