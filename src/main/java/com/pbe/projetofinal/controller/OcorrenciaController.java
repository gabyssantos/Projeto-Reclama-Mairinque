package com.pbe.projetofinal.controller;

import com.pbe.projetofinal.model.Ocorrencia;
import com.pbe.projetofinal.model.Pessoa;
import com.pbe.projetofinal.model.Perfil;
import com.pbe.projetofinal.model.StatusOcorrencia;
import com.pbe.projetofinal.repository.OcorrenciaRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

import com.pbe.projetofinal.model.Ocorrencia;
import com.pbe.projetofinal.model.Pessoa;
import com.pbe.projetofinal.model.Perfil;
import jakarta.servlet.http.HttpSession;

import java.util.List;
@Controller
public class OcorrenciaController {

    @Autowired
    private OcorrenciaRepository ocorrenciaRepository;

    private final String uploadDir =
            "src/main/resources/static/foto_ocorrencia/";


    // =================================================
    // NOVA OCORRÊNCIA
    // =================================================

    @GetMapping("/ocorrencia/nova")
    public String nova(
            Model model,
            HttpSession session) {

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        // Não está logado
        if (pessoaLogada == null) {
            return "redirect:/login";
        }

        // Envia a pessoa para o HTML
        model.addAttribute("pessoa", pessoaLogada);

        // Cria uma nova ocorrência
        model.addAttribute(
                "ocorrencia",
                new Ocorrencia()
        );

        return "/ocorrencia/reclamacao";
    }


    // =================================================
    // SALVAR
    // =================================================

    @PostMapping("/ocorrencia/salvar")
    public String salvar(
            @Valid Ocorrencia ocorrencia,
            BindingResult result,
            @RequestParam(
                    value = "arquivoFoto",
                    required = false
            ) MultipartFile arquivoFoto,
            HttpSession session,
            Model model) {

        // =================================================
        // PESSOA LOGADA
        // =================================================

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        if (pessoaLogada == null) {
            return "redirect:/login";
        }


        // =================================================
        // VALIDAÇÃO
        // =================================================

        if (result.hasErrors()) {

            // Precisamos enviar novamente a pessoa
            // para o formulário
            model.addAttribute(
                    "pessoa",
                    pessoaLogada
            );

            if (ocorrencia.getId() != null) {
                return "/ocorrencia/alterar";
            }

            return "/ocorrencia/reclamacao";
        }


        try {

            // =================================================
            // VINCULAR À PESSOA LOGADA
            // =================================================

            ocorrencia.setCliente(
                    pessoaLogada
            );


            // =================================================
            // DATA DA OCORRÊNCIA
            // =================================================

            if (ocorrencia.getDataOcorrencia() == null) {

                ocorrencia.setDataOcorrencia(
                        LocalDate.now()
                );
            }


            // =================================================
            // STATUS
            // =================================================

            if (pessoaLogada.getPerfil() == Perfil.NORMAL) {

                // Usuário normal:
                // sempre começa como RASCUNHO

                ocorrencia.setStatusOcorrencia(
                        StatusOcorrencia.RASCUNHO
                );

            } else {

                // Moderador ou administrador:
                // pode publicar diretamente

                ocorrencia.setStatusOcorrencia(
                        StatusOcorrencia.APROVADO
                );
            }


            // =================================================
            // FOTO
            // =================================================

            if (arquivoFoto != null
                    && !arquivoFoto.isEmpty()) {

                String nomeArquivo =
                        UUID.randomUUID()
                                + "_"
                                + arquivoFoto.getOriginalFilename();

                Path caminhoDiretorio =
                        Paths.get(uploadDir)
                                .toAbsolutePath();

                if (!Files.exists(caminhoDiretorio)) {

                    Files.createDirectories(
                            caminhoDiretorio
                    );
                }

                Path caminhoArquivo =
                        caminhoDiretorio.resolve(
                                nomeArquivo
                        );

                arquivoFoto.transferTo(
                        caminhoArquivo.toFile()
                );

                ocorrencia.setFotoOcorrencia(
                        nomeArquivo
                );
            }


            // =================================================
            // SALVAR NO BANCO
            // =================================================

            ocorrenciaRepository.save(
                    ocorrencia
            );

        } catch (IOException e) {

            e.printStackTrace();

            model.addAttribute(
                    "pessoa",
                    pessoaLogada
            );

            model.addAttribute(
                    "erro",
                    "Não foi possível salvar a foto da ocorrência."
            );

            return "/ocorrencia/reclamacao";
        }


        // =================================================
        // FINAL
        // =================================================

        return "redirect:/ocorrencia/listagem";
    }


    // =================================================
    // LISTAGEM
    // =================================================
    @GetMapping("/ocorrencia/listagem")
    public String listagem(Model model, HttpSession session) {

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        // Usuário não está logado
        if (pessoaLogada == null) {
            return "redirect:/login";
        }

        List<Ocorrencia> ocorrencias;

        // MODERADOR E ADMINISTRADOR
        // podem visualizar todas as ocorrências
        if (pessoaLogada.getPerfil() == Perfil.MODERADOR ||
                pessoaLogada.getPerfil() == Perfil.ADMINISTRADOR) {

            ocorrencias = ocorrenciaRepository.findAll();

        } else {

            // USUÁRIO NORMAL
            // visualiza somente as próprias ocorrências
            ocorrencias = ocorrenciaRepository
                    .findByClienteId(pessoaLogada.getId());
        }

        model.addAttribute("ocorrencias", ocorrencias);

        return "/ocorrencia/card";
    }


    // =================================================
    // ALTERAR
    // =================================================

    @GetMapping("/ocorrencia/alterar/{id}")
    public String alterar(
            @PathVariable Long id,
            Model model) {

        Ocorrencia ocorrencia =
                ocorrenciaRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ocorrência inválida"
                                )
                        );

        model.addAttribute(
                "ocorrencia",
                ocorrencia
        );

        return "/ocorrencia/alterar";
    }


    // =================================================
    // EXCLUIR
    // =================================================

    @GetMapping("/ocorrencia/excluir/{id}")
    public String excluir(
            @PathVariable Long id) {

        ocorrenciaRepository.deleteById(id);

        return "redirect:/ocorrencia/listagem";
    }
}