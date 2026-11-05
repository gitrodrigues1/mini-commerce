package dev.minicommerce.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
public class MiniCommerceAmqpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    MessageConverter jacksonMessageConverter(ObjectProvider<JsonMapper> jsonMappers) {
        JsonMapper jsonMapper = jsonMappers.getIfAvailable(JsonMapper::new);
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(jsonMapper);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean
    EventPublisher eventPublisher(RabbitTemplate rabbitTemplate) {
        return new EventPublisher(rabbitTemplate);
    }
}
