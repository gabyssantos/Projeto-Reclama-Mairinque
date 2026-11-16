
package com.pbe.projetofinal.controller;

import com.pbe.projetofinal.model.Pessoa;
import com.pbe.projetofinal.model.Perfil;
import com.pbe.projetofinal.model.StatusOcorrencia;
import com.pbe.projetofinal.repository.PessoaRepository;
import com.pbe.projetofinal.repository.OcorrenciaRepository;
import com.pbe.projetofinal.service.ArquivoService;
import com.pbe.projetofinal.model.Ocorrencia;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PessoaRepository pessoaRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final ArquivoService arquivoService;

    public AdminController(
            PessoaRepository pessoaRepository,
            OcorrenciaRepository ocorrenciaRepository,
            ArquivoService arquivoService) {

        this.pessoaRepository = pessoaRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.arquivoService = arquivoService;
    }

    private boolean isAdministrador(HttpSession session) {
        Pessoa pessoa = (Pessoa) session.getAttribute("pessoaLogada");

        return pessoa != null
                && pessoa.getPerfil() == Perfil.ADMINISTRADOR;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        if (!isAdministrador(session)) {
            return "redirect:/login";
        }

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        Pessoa pessoa = pessoaRepository.findById(pessoaLogada.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Administrador não encontrado."));

        session.setAttribute("pessoaLogada", pessoa);
        model.addAttribute("pessoa", pessoa);

        model.addAttribute("totalUsuarios", pessoaRepository.count());
        model.addAttribute("totalReclamacoes", ocorrenciaRepository.count());

        model.addAttribute("reclamacoesRascunho",
                ocorrenciaRepository.countByStatusOcorrencia(StatusOcorrencia.RASCUNHO));

        model.addAttribute("reclamacoesEmAndamento",
                ocorrenciaRepository.countByStatusOcorrencia(StatusOcorrencia.EM_ANDAMENTO));

        model.addAttribute("reclamacoesAprovadas",
                ocorrenciaRepository.countByStatusOcorrencia(StatusOcorrencia.APROVADO));

        model.addAttribute("reclamacoesRecusadas",
                ocorrenciaRepository.countByStatusOcorrencia(StatusOcorrencia.RECUSADO));

        return "admin/dashboard";
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {

        if (!isAdministrador(session)) {
            return "redirect:/login";
        }

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        Pessoa pessoa = pessoaRepository.findById(pessoaLogada.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Administrador não encontrado."));

        session.setAttribute("pessoaLogada", pessoa);
        model.addAttribute("pessoa", pessoa);

        return "admin/perfil";
    }

    @PostMapping("/perfil/atualizar")
    public String atualizarPerfil(
            @ModelAttribute("pessoa") Pessoa dados,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            HttpSession session,
            Model model) throws IOException {

        System.out.println("ENTROU NO MÉTODO ATUALIZAR PERFIL");

        if (!isAdministrador(session)) {
            return "redirect:/login";
        }

        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        Pessoa pessoa = pessoaRepository.findById(pessoaLogada.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Administrador não encontrado."));

        pessoa.setNome(dados.getNome());
        pessoa.setEmail(dados.getEmail());
        pessoa.setCpf(dados.getCpf());
        pessoa.setTelefone(dados.getTelefone());

        pessoa.setCep(dados.getCep());
        pessoa.setLogradouro(dados.getLogradouro());
        pessoa.setNumero(dados.getNumero());
        pessoa.setComplemento(dados.getComplemento());
        pessoa.setBairro(dados.getBairro());
        pessoa.setCidade(dados.getCidade());
        pessoa.setEstado(dados.getEstado());

        pessoa.setLatitude(dados.getLatitude());
        pessoa.setLongitude(dados.getLongitude());

        // Verifica se uma foto foi enviada
        if (foto != null && !foto.isEmpty()) {

            System.out.println("Nome da foto: " +
                    foto.getOriginalFilename());

            System.out.println("Tamanho da foto: " +
                    foto.getSize());

            if (foto.getContentType() == null ||
                    !foto.getContentType().startsWith("image/")) {

                model.addAttribute("pessoa", pessoa);
                model.addAttribute("erroFoto",
                        "Selecione um arquivo de imagem válido.");

                return "admin/perfil";
            }

            String caminhoFoto = arquivoService.salvarFoto(foto);
            pessoa.setFoto(caminhoFoto);
        }

        pessoaRepository.save(pessoa);

        session.setAttribute("pessoaLogada", pessoa);

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/reclamacoes")
    public String reclamacoes(HttpSession session, Model model) {

        if (!isAdministrador(session)) {
            return "redirect:/login";
        }

        model.addAttribute("pessoa",
                session.getAttribute("pessoaLogada"));

        model.addAttribute("ocorrencias",
                ocorrenciaRepository.findAll());

        return "admin/reclamacoes";
    }

    @GetMapping("/usuarios")
    public String usuarios(HttpSession session, Model model) {

        if (!isAdministrador(session)) {
            return "redirect:/login";
        }

        model.addAttribute("pessoa",
                session.getAttribute("pessoaLogada"));

        model.addAttribute("usuarios",
                pessoaRepository.findAll());

        return "admin/usuarios";
    }
    @PostMapping("/reclamacoes/atualizar-status")
    public String atualizarStatus(
            @RequestParam Long id,
            @RequestParam StatusOcorrencia status,
            HttpSession session) {

        if (!isAdministrador(session)) {
            return "redirect:/login";
        }

        Ocorrencia ocorrencia = ocorrenciaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ocorrência não encontrada."));

        ocorrencia.setStatusOcorrencia(status);

        ocorrenciaRepository.save(ocorrencia);

        return "redirect:/admin/reclamacoes";
    }
}
