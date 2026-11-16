
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

        if (pessoaLogada == null) {
            return "redirect:/login";
        }

        model.addAttribute("pessoa", pessoaLogada);
        model.addAttribute("ocorrencia", new Ocorrencia());

        return "/ocorrencia/reclamacao";
    }

    // =================================================
    // SALVAR OU ALTERAR OCORRÊNCIA
    // =================================================

    @PostMapping("/ocorrencia/salvar")
    public String salvar(
            @Valid @ModelAttribute("ocorrencia") Ocorrencia ocorrencia,
            BindingResult result,
            @RequestParam(
                    value = "arquivoFoto",
                    required = false
            ) MultipartFile arquivoFoto,
            HttpSession session,
            Model model) {

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        if (pessoaLogada == null) {
            return "redirect:/login";
        }

        Ocorrencia ocorrenciaSalva = null;

        // VERIFICAR SE É UMA ALTERAÇÃO

        if (ocorrencia.getId() != null) {

            ocorrenciaSalva = ocorrenciaRepository
                    .findById(ocorrencia.getId())
                    .orElse(null);

            if (ocorrenciaSalva == null ||
                    !ocorrenciaSalva.getCliente().getId()
                            .equals(pessoaLogada.getId())) {

                return "redirect:/ocorrencia/listagem";
            }
        }

        // VALIDAÇÃO

        if (result.hasErrors()) {

            model.addAttribute("pessoa", pessoaLogada);

            if (ocorrenciaSalva != null) {

                ocorrencia.setFotoOcorrencia(
                        ocorrenciaSalva.getFotoOcorrencia()
                );

                return "/ocorrencia/alterar";
            }

            return "/ocorrencia/reclamacao";
        }

        try {

            // SE FOR ALTERAÇÃO

            if (ocorrenciaSalva != null) {

                ocorrenciaSalva.setRua(ocorrencia.getRua());

                ocorrenciaSalva.setBairro(ocorrencia.getBairro());

                ocorrenciaSalva.setCep(ocorrencia.getCep());

                ocorrenciaSalva.setCategoriaOcorrencia(
                        ocorrencia.getCategoriaOcorrencia()
                );

                ocorrenciaSalva.setDescricaoOcorrencia(
                        ocorrencia.getDescricaoOcorrencia()
                );

            } else {

                // SE FOR NOVA OCORRÊNCIA

                ocorrencia.setCliente(pessoaLogada);

                if (ocorrencia.getDataOcorrencia() == null) {
                    ocorrencia.setDataOcorrencia(LocalDate.now());
                }

                if (pessoaLogada.getPerfil() == Perfil.NORMAL) {

                    ocorrencia.setStatusOcorrencia(
                            StatusOcorrencia.RASCUNHO
                    );

                } else {

                    ocorrencia.setStatusOcorrencia(
                            StatusOcorrencia.APROVADO
                    );
                }

                ocorrenciaSalva = ocorrencia;
            }

            // FOTO

            if (arquivoFoto != null && !arquivoFoto.isEmpty()) {

                String nomeOriginal = Paths.get(
                        arquivoFoto.getOriginalFilename()
                ).getFileName().toString();

                String nomeArquivo =
                        UUID.randomUUID() + "_" + nomeOriginal;

                Path caminhoDiretorio =
                        Paths.get(uploadDir).toAbsolutePath();

                if (!Files.exists(caminhoDiretorio)) {
                    Files.createDirectories(caminhoDiretorio);
                }

                Path caminhoArquivo =
                        caminhoDiretorio.resolve(nomeArquivo);

                arquivoFoto.transferTo(caminhoArquivo.toFile());

                ocorrenciaSalva.setFotoOcorrencia(nomeArquivo);
            }

            // SALVAR NO BANCO

            ocorrenciaRepository.save(ocorrenciaSalva);

        } catch (IOException e) {

            e.printStackTrace();

            model.addAttribute("pessoa", pessoaLogada);
            model.addAttribute("ocorrencia", ocorrencia);

            model.addAttribute(
                    "erro",
                    "Não foi possível salvar a foto da ocorrência."
            );

            if (ocorrencia.getId() != null) {
                return "/ocorrencia/alterar";
            }

            return "/ocorrencia/reclamacao";
        }

        return "redirect:/ocorrencia/listagem";
    }

    // =================================================
    // FEED DE RECLAMAÇÕES APROVADAS
    // =================================================

    @GetMapping("/ocorrencia/feed")
    public String feed(Model model) {

        List<Ocorrencia> ocorrencias =
                ocorrenciaRepository.findByStatusOcorrenciaOrderByIdDesc(
                        StatusOcorrencia.APROVADO
                );

        model.addAttribute("ocorrencias", ocorrencias);

        return "/ocorrencia/feed";
    }

    // =================================================
    // LISTAGEM DAS OCORRÊNCIAS DO USUÁRIO
    // =================================================

    @GetMapping("/ocorrencia/listagem")
    public String listagem(
            Model model,
            HttpSession session) {

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        if (pessoaLogada == null) {
            return "redirect:/login";
        }

        List<Ocorrencia> ocorrencias =
                ocorrenciaRepository.findByClienteId(
                        pessoaLogada.getId()
                );

        model.addAttribute("ocorrencias", ocorrencias);

        return "/ocorrencia/card";
    }

    // =================================================
    // ALTERAR OCORRÊNCIA
    // =================================================

    @GetMapping("/ocorrencia/alterar/{id}")
    public String alterar(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        if (pessoaLogada == null) {
            return "redirect:/login";
        }

        Ocorrencia ocorrencia =
                ocorrenciaRepository.findById(id).orElse(null);

        if (ocorrencia == null ||
                !ocorrencia.getCliente().getId()
                        .equals(pessoaLogada.getId())) {

            return "redirect:/ocorrencia/listagem";
        }

        model.addAttribute("pessoa", pessoaLogada);
        model.addAttribute("ocorrencia", ocorrencia);

        return "/ocorrencia/alterar";
    }

    // =================================================
    // EXCLUIR OCORRÊNCIA
    // =================================================

    @GetMapping("/ocorrencia/excluir/{id}")
    public String excluir(
            @PathVariable Long id,
            HttpSession session) {

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        if (pessoaLogada == null) {
            return "redirect:/login";
        }

        Ocorrencia ocorrencia =
                ocorrenciaRepository.findById(id).orElse(null);

        if (ocorrencia == null ||
                !ocorrencia.getCliente().getId()
                        .equals(pessoaLogada.getId())) {

            return "redirect:/ocorrencia/listagem";
        }

        ocorrenciaRepository.delete(ocorrencia);

        return "redirect:/ocorrencia/listagem";
    }
}
