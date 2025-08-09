package com.agroflow.central.repository;

import com.agroflow.central.model.Cosecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CosechaRepository extends JpaRepository<Cosecha, Long> {
}