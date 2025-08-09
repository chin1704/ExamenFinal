package com.agroflow.central.controller;

import com.agroflow.central.model.Cosecha;
import com.agroflow.central.repository.CosechaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    public ResponseEntity<Cosecha> createCosecha(@RequestBody Cosecha cosecha) {
        cosecha.setEstado("PENDIENTE");
        Cosecha nuevaCosecha = cosechaRepository.save(cosecha);

        // Publica el evento en RabbitMQ
        rabbitTemplate.convertAndSend("agroflow-exchange", "nueva_cosecha", nuevaCosecha);
        
        return ResponseEntity.ok(nuevaCosecha);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Cosecha> updateCosechaStatus(@PathVariable Long id, @RequestBody String estado) {
        Optional<Cosecha> cosechaOptional = cosechaRepository.findById(id);
        if (cosechaOptional.isPresent()) {
            Cosecha cosecha = cosechaOptional.get();
            cosecha.setEstado(estado);
            cosechaRepository.save(cosecha);
            return ResponseEntity.ok(cosecha);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}