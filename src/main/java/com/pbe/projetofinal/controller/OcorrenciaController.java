package com.pbe.projetofinal.controller;

import com.pbe.projetofinal.model.Ocorrencia;
import com.pbe.projetofinal.repository.OcorrenciaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.IOException;
import java.util.UUID;

@Controller
public class OcorrenciaController {

    @Autowired
    private OcorrenciaRepository ocorrenciaRepository;

    private final String uploadDir = "src/main/resources/static/foto_ocorrencia/";


    @GetMapping("/ocorrencia/nova")
    public String nova(Model model) {
        model.addAttribute("ocorrencia", new Ocorrencia());
        return "/ocorrencia/reclamacao";
    }
    // SALVAR
    @PostMapping("/ocorrencia/salvar")
    public String salvar(@Valid Ocorrencia ocorrencia,
                         BindingResult result,
                         @RequestParam(value="arquivoFoto", required=false) MultipartFile arquivoFoto){

        if(result.hasErrors()){

            if(ocorrencia.getId() != null){
                return "/ocorrencia/alterar";
            }

            return "/ocorrencia/cadastro";
        }

        try {

            if (arquivoFoto != null && !arquivoFoto.isEmpty()) {

                String nomeArquivo = UUID.randomUUID() + "_" + arquivoFoto.getOriginalFilename();

                Path caminhoDiretorio = Paths.get(uploadDir).toAbsolutePath();

                if (!Files.exists(caminhoDiretorio)) {
                    Files.createDirectories(caminhoDiretorio);
                }

                Path caminhoArquivo = caminhoDiretorio.resolve(nomeArquivo);

                arquivoFoto.transferTo(caminhoArquivo.toFile());

                ocorrencia.setFotoOcorrencia(nomeArquivo);
            }
            else if(ocorrencia.getId() != null){

                Ocorrencia ocorrenciaBanco = ocorrenciaRepository.findById(ocorrencia.getId()).orElse(null);

                if(ocorrenciaBanco != null){
                    ocorrencia.setFotoOcorrencia(ocorrenciaBanco.getFotoOcorrencia());
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        ocorrenciaRepository.save(ocorrencia);

        return "redirect:/ocorrencia/listagem";
    }

    // LISTAGEM
    @GetMapping("/ocorrencia/listagem")
    public String listagem(Model model){

        model.addAttribute("ocorrencias", ocorrenciaRepository.findAll());

        //return "/ocorrencia/listagem";
        return "/ocorrencia/card";
    }

    // ALTERAR
    @GetMapping("/ocorrencia/alterar/{id}")
    public String alterar(@PathVariable Long id, Model model){

        Ocorrencia ocorrencia = ocorrenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ocorrencia inválido"));

        model.addAttribute("ocorrencia", ocorrencia);

        return "/ocorrencia/alterar";
    }
    // EXCLUIR
    @GetMapping("/ocorrencia/excluir/{id}")
    public String excluir(@PathVariable Long id){

        ocorrenciaRepository.deleteById(id);

        return "redirect:/ocorrencia/listagem";
    }
}