import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("agroflow-exchange");
    }
    @Bean
    public Queue inventoryQueue() {
        return new Queue("cola_inventario");
    }
    @Bean
    public Queue billingQueue() {
        return new Queue("cola_facturacion");
    }
    @Bean
    public Binding inventoryBinding(Queue inventoryQueue, DirectExchange exchange) {
        return BindingBuilder.bind(inventoryQueue).to(exchange).with("nueva_cosecha");
    }
    @Bean
    public Binding billingBinding(Queue billingQueue, DirectExchange exchange) {
        return BindingBuilder.bind(billingQueue).to(exchange).with("nueva_cosecha");
    }
}