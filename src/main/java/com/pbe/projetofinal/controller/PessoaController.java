
package com.pbe.projetofinal.controller;

import com.pbe.projetofinal.model.Perfil;
import com.pbe.projetofinal.model.Pessoa;
import com.pbe.projetofinal.model.StatusOcorrencia;
import com.pbe.projetofinal.repository.PessoaRepository;
import com.pbe.projetofinal.repository.OcorrenciaRepository;
import com.pbe.projetofinal.service.ArquivoService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/pessoa")
public class PessoaController {

    private final PessoaRepository pessoaRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final ArquivoService arquivoService;

    public PessoaController(
            PessoaRepository pessoaRepository,
            OcorrenciaRepository ocorrenciaRepository,
            ArquivoService arquivoService) {

        this.pessoaRepository = pessoaRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.arquivoService = arquivoService;
    }

    // =========================================================
    // TESTE
    // =========================================================

    @GetMapping("/teste")
    @ResponseBody
    public String teste() {
        return "PessoaController funcionando!";
    }

    // =========================================================
    // LISTAGEM
    // =========================================================

    @GetMapping("/listagem")
    public String listagem(Model model) {

        model.addAttribute(
                "pessoas",
                pessoaRepository.findAll()
        );

        return "pessoa/listagem";
    }

    // =========================================================
    // CADASTRO
    // =========================================================

    @GetMapping("/cadastro")
    public String cadastro(
            Model model,
            HttpSession session) {

        session.invalidate();

        model.addAttribute(
                "pessoa",
                new Pessoa()
        );

        return "pessoa/cadastro";
    }

    // =========================================================
    // SALVAR CADASTRO
    // =========================================================

    @PostMapping("/salvar")
    public String salvar(
            @Valid @ModelAttribute("pessoa") Pessoa pessoa,
            BindingResult result,
            @RequestParam("confirmarSenha") String confirmarSenha,
            Model model) {

        if (result.hasErrors()) {
            return "pessoa/cadastro";
        }

        // Confirmar senha
        if (pessoa.getSenha() == null
                || !pessoa.getSenha().equals(confirmarSenha)) {

            model.addAttribute(
                    "senhaErro",
                    "As senhas não são iguais."
            );

            return "pessoa/cadastro";
        }

        // CPF duplicado
        if (pessoa.getCpf() != null
                && !pessoa.getCpf().isBlank()
                && pessoaRepository.existsByCpf(pessoa.getCpf())) {

            result.rejectValue(
                    "cpf",
                    "cpf.duplicado",
                    "Este CPF já está cadastrado."
            );

            return "pessoa/cadastro";
        }

        // E-mail duplicado
        if (pessoa.getEmail() != null
                && !pessoa.getEmail().isBlank()
                && pessoaRepository.existsByEmail(pessoa.getEmail())) {

            result.rejectValue(
                    "email",
                    "email.duplicado",
                    "Este e-mail já está cadastrado."
            );

            return "pessoa/cadastro";
        }

        pessoa.setPerfil(Perfil.NORMAL);
        pessoa.setAtivo(true);

        pessoaRepository.save(pessoa);

        return "redirect:/pessoa/dashboard";
    }

    // =========================================================
    // ATUALIZAR ENDEREÇO
    // =========================================================

    @PostMapping("/atualizar-endereco")
    public String atualizarEndereco(
            @ModelAttribute("pessoa") Pessoa pessoa,
            HttpSession session) {

        Pessoa pessoaBanco = pessoaRepository
                .findById(pessoa.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        )
                );

        pessoaBanco.setCep(pessoa.getCep());
        pessoaBanco.setLogradouro(pessoa.getLogradouro());
        pessoaBanco.setNumero(pessoa.getNumero());
        pessoaBanco.setComplemento(pessoa.getComplemento());
        pessoaBanco.setBairro(pessoa.getBairro());
        pessoaBanco.setCidade(pessoa.getCidade());
        pessoaBanco.setEstado(pessoa.getEstado());
        pessoaBanco.setLatitude(pessoa.getLatitude());
        pessoaBanco.setLongitude(pessoa.getLongitude());

        pessoaRepository.save(pessoaBanco);

        session.setAttribute("pessoaLogada", pessoaBanco);

        return "redirect:/pessoa/dashboard";
    }

    // =========================================================
    // ATUALIZAR FOTO
    // =========================================================

    @PostMapping("/atualizar-foto")
    public String atualizarFoto(
            @RequestParam("id") Long id,
            @RequestParam("fotoArquivo") MultipartFile fotoArquivo,
            HttpSession session) throws IOException {

        Pessoa pessoa = pessoaRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        )
                );

        if (fotoArquivo != null && !fotoArquivo.isEmpty()) {

            String caminhoFoto =
                    arquivoService.salvarFoto(fotoArquivo);

            pessoa.setFoto(caminhoFoto);

            pessoaRepository.save(pessoa);

            Pessoa pessoaLogada =
                    (Pessoa) session.getAttribute("pessoaLogada");

            if (pessoaLogada != null
                    && pessoaLogada.getId().equals(pessoa.getId())) {

                session.setAttribute("pessoaLogada", pessoa);
            }
        }

        return "redirect:/pessoa/dashboard";
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        Pessoa pessoa = (Pessoa) session.getAttribute("pessoaLogada");

        if (pessoa == null) {
            return "redirect:/login";
        }

        // Busca os dados atualizados no banco
        pessoa = pessoaRepository
                .findById(pessoa.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pessoa não encontrada."
                        )
                );

        // Atualiza a sessão
        session.setAttribute("pessoaLogada", pessoa);

        Long idPessoa = pessoa.getId();

        model.addAttribute("pessoa", pessoa);

        // Total de chamados
        model.addAttribute(
                "totalChamados",
                ocorrenciaRepository.countByClienteId(idPessoa)
        );

        // Rascunhos
        model.addAttribute(
                "chamadosRascunho",
                ocorrenciaRepository.countByClienteIdAndStatusOcorrencia(
                        idPessoa,
                        StatusOcorrencia.RASCUNHO
                )
        );

        // Chamados em andamento
        model.addAttribute(
                "chamadosEmAndamento",
                ocorrenciaRepository.countByClienteIdAndStatusOcorrencia(
                        idPessoa,
                        StatusOcorrencia.EM_ANDAMENTO
                )
        );

        // Chamados aprovados
        model.addAttribute(
                "chamadosAprovados",
                ocorrenciaRepository.countByClienteIdAndStatusOcorrencia(
                        idPessoa,
                        StatusOcorrencia.APROVADO
                )
        );

        // Chamados recusados
        model.addAttribute(
                "chamadosRecusados",
                ocorrenciaRepository.countByClienteIdAndStatusOcorrencia(
                        idPessoa,
                        StatusOcorrencia.RECUSADO
                )
        );

        // Últimos 5 chamados da pessoa logada
        model.addAttribute(
                "chamadosRecentes",
                ocorrenciaRepository.findTop5ByClienteIdOrderByIdDesc(idPessoa)
        );

        return "pessoa/dashboard";
    }

    // =========================================================
    // EDITAR
    // =========================================================

    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model) {

        Pessoa pessoa = pessoaRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pessoa não encontrada: " + id
                        )
                );

        model.addAttribute("pessoa", pessoa);
        model.addAttribute("perfis", Perfil.values());

        return "pessoa/editar";
    }

    // =========================================================
    // TESTE POST
    // =========================================================

    @PostMapping("/teste-post")
    @ResponseBody
    public String testePost() {

        System.out.println("POST CHEGOU NO CONTROLLER!");

        return "POST FUNCIONOU!";
    }

    // =========================================================
    // ATUALIZAR USUÁRIO
    // =========================================================

    @PostMapping("/atualizar")
    public String atualizar(
            @Valid @ModelAttribute("pessoa") Pessoa dados,
            BindingResult result,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Model model,
            HttpSession session) {

        if (result.hasErrors()) {

            model.addAttribute("perfis", Perfil.values());

            return "pessoa/editar";
        }

        Pessoa pessoa = pessoaRepository
                .findById(dados.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pessoa não encontrada: " + dados.getId()
                        )
                );

        // Dados pessoais
        pessoa.setNome(dados.getNome());
        pessoa.setEmail(dados.getEmail());
        pessoa.setCpf(dados.getCpf());
        pessoa.setTelefone(dados.getTelefone());

        // Endereço
        pessoa.setCep(dados.getCep());
        pessoa.setLogradouro(dados.getLogradouro());
        pessoa.setNumero(dados.getNumero());
        pessoa.setComplemento(dados.getComplemento());
        pessoa.setBairro(dados.getBairro());
        pessoa.setCidade(dados.getCidade());
        pessoa.setEstado(dados.getEstado());

        // Localização
        pessoa.setLatitude(dados.getLatitude());
        pessoa.setLongitude(dados.getLongitude());

        // Foto
        if (foto != null && !foto.isEmpty()) {

            try {

                String caminhoFoto =
                        arquivoService.salvarFoto(foto);

                pessoa.setFoto(caminhoFoto);

            } catch (Exception e) {

                e.printStackTrace();

                model.addAttribute(
                        "erroFoto",
                        "Não foi possível salvar a foto."
                );

                model.addAttribute("perfis", Perfil.values());

                return "pessoa/editar";
            }
        }

        // Salva no banco
        pessoaRepository.save(pessoa);

        // Atualiza a sessão se for a própria pessoa logada
        Pessoa pessoaLogada =
                (Pessoa) session.getAttribute("pessoaLogada");

        if (pessoaLogada != null
                && pessoaLogada.getId().equals(pessoa.getId())) {

            session.setAttribute("pessoaLogada", pessoa);
        }

        return "redirect:/pessoa/dashboard";
    }

    // =========================================================
    // ATUALIZAR DADOS SIMPLES
    // =========================================================

    @PostMapping("/atualizar-dados")
    public String atualizarDados(
            @RequestParam Long id,
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String telefone) {

        Pessoa pessoa = pessoaRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pessoa não encontrada."
                        )
                );

        pessoa.setNome(nome);
        pessoa.setEmail(email);
        pessoa.setCpf(cpf);
        pessoa.setTelefone(telefone);

        pessoaRepository.save(pessoa);

        return "redirect:/pessoa/editar/" + id;
    }

    // =========================================================
    // EXCLUIR
    // =========================================================

    @GetMapping("/excluir/{id}")
    public String excluir(
            @PathVariable Long id) {

        pessoaRepository.deleteById(id);

        return "redirect:/pessoa/listagem";
    }
}
