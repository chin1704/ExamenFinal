import com.agroflow.central.model.Cosecha;
import com.agroflow.central.repository.CosechaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cosechas")
public class AgriculturalController {

    private final CosechaRepository cosechaRepository;
    private final RabbitTemplate rabbitTemplate;

    public AgriculturalController(CosechaRepository cosechaRepository, RabbitTemplate rabbitTemplate) {
        this.cosechaRepository = cosechaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public Cosecha createCosecha(@RequestBody Cosecha cosecha) {
        cosecha.setEstado("PENDIENTE");
        Cosecha nuevaCosecha = cosechaRepository.save(cosecha);

        // Publica el evento en RabbitMQ
        rabbitTemplate.convertAndSend("agroflow-exchange", "nueva_cosecha", nuevaCosecha);

        return nuevaCosecha;
    }

    @PutMapping("/{id}/estado")
    public Cosecha updateCosechaStatus(@PathVariable Long id, @RequestBody String estado) {
        Cosecha cosecha = cosechaRepository.findById(id).orElseThrow();
        cosecha.setEstado(estado);
        return cosechaRepository.save(cosecha);
    }
}