package com.pbe.projetofinal.repository;

import com.pbe.projetofinal.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    Optional<Pessoa> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    Optional<Pessoa> findByEmail(String email);

    boolean existsByEmail(String email);
}
