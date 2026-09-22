package com.pbe.projetofinal.repository;

import com.pbe.projetofinal.model.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcorrenciaRepository
        extends JpaRepository<Ocorrencia, Long> {

    List<Ocorrencia> findByClienteId(Long clienteId);
}