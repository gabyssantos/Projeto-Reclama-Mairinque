
package com.pbe.projetofinal.controller;

import com.pbe.projetofinal.model.StatusOcorrencia;
import com.pbe.projetofinal.repository.OcorrenciaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    private final OcorrenciaRepository ocorrenciaRepository;

    public HomeController(OcorrenciaRepository ocorrenciaRepository) {
        this.ocorrenciaRepository = ocorrenciaRepository;
    }

    @GetMapping
    public String home(Model model) {

        model.addAttribute("totalRegistradas",
                ocorrenciaRepository.count());

        model.addAttribute("totalEmAndamento",
                ocorrenciaRepository.countByStatusOcorrencia(StatusOcorrencia.EM_ANDAMENTO));

        model.addAttribute("totalAprovadas",
                ocorrenciaRepository.countByStatusOcorrencia(StatusOcorrencia.APROVADO));

        return "index";
    }
}
