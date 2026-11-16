
package com.pbe.projetofinal.repository;

import com.pbe.projetofinal.model.Ocorrencia;
import com.pbe.projetofinal.model.StatusOcorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    List<Ocorrencia> findByClienteId(Long clienteId);

    long countByClienteId(Long clienteId);

    long countByStatusOcorrencia(StatusOcorrencia statusOcorrencia);



    long countByClienteIdAndStatusOcorrencia(
            Long clienteId,
            StatusOcorrencia statusOcorrencia
    );

    List<Ocorrencia> findTop5ByClienteIdOrderByIdDesc(Long clienteId);
}
