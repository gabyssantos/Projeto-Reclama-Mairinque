package com.pbe.projetofinal.controller;

import com.pbe.projetofinal.model.Perfil;
import com.pbe.projetofinal.model.Pessoa;
import com.pbe.projetofinal.repository.PessoaRepository;
import com.pbe.projetofinal.service.ArquivoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pessoa")
public class PessoaController {

    private final PessoaRepository pessoaRepository;
    private final ArquivoService arquivoService;

    public PessoaController(
            PessoaRepository pessoaRepository,
            ArquivoService arquivoService) {

        this.pessoaRepository = pessoaRepository;
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

        // Encerra a sessão do usuário que estiver logado
        session.invalidate();

        // Cria uma nova pessoa vazia para o formulário
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

        // Validação dos campos
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

        // Configurações padrão
        pessoa.setPerfil(Perfil.NORMAL);
        pessoa.setAtivo(true);

        // Salvar pessoa
        pessoaRepository.save(pessoa);

        return "redirect:/pessoa/dashboard";
    }

    @PostMapping("/atualizar-endereco")
    public String atualizarEndereco(
            @ModelAttribute("pessoa") Pessoa pessoa,
            HttpSession session) {

        Pessoa pessoaBanco = pessoaRepository.findById(pessoa.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado.")
                );

        // Atualiza somente os dados de endereço
        pessoaBanco.setCep(pessoa.getCep());
        pessoaBanco.setLogradouro(pessoa.getLogradouro());
        pessoaBanco.setNumero(pessoa.getNumero());
        pessoaBanco.setComplemento(pessoa.getComplemento());
        pessoaBanco.setBairro(pessoa.getBairro());
        pessoaBanco.setCidade(pessoa.getCidade());
        pessoaBanco.setEstado(pessoa.getEstado());
        pessoaBanco.setLatitude(pessoa.getLatitude());
        pessoaBanco.setLongitude(pessoa.getLongitude());

        // Salva no banco
        pessoaRepository.save(pessoaBanco);

        // Atualiza a pessoa que está armazenada na sessão
        session.setAttribute("pessoaLogada", pessoaBanco);

        // Volta para o dashboard
        return "redirect:/pessoa/dashboard";
    }

    @PostMapping("/atualizar-foto")
    public String atualizarFoto(
            @RequestParam("id") Long id,
            @RequestParam("fotoArquivo") MultipartFile fotoArquivo)
            throws IOException {

        System.out.println("=================================");
        System.out.println("ID RECEBIDO: " + id);
        System.out.println("ARQUIVO: " + fotoArquivo.getOriginalFilename());
        System.out.println("TAMANHO: " + fotoArquivo.getSize());
        System.out.println("=================================");

        Pessoa pessoa = pessoaRepository.findById(id)
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
        }

        return "redirect:/pessoa/dashboard";
    }
    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        // Pega a pessoa que fez login
        Pessoa pessoa =
                (Pessoa) session.getAttribute("pessoaLogada");


        // Se não estiver logado, manda para o login
        if (pessoa == null) {
            return "redirect:/login";
        }


        // Envia os dados da pessoa para o dashboard
        model.addAttribute("pessoa", pessoa);


        // Estatísticas — por enquanto zeradas.
        // Depois vamos buscar os chamados reais no banco.
        model.addAttribute("totalChamados", 0);
        model.addAttribute("chamadosAbertos", 0);
        model.addAttribute("chamadosAtendidos", 0);
        model.addAttribute("chamadosPendentes", 0);


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

        model.addAttribute(
                "pessoa",
                pessoa
        );

        model.addAttribute(
                "perfis",
                Perfil.values()
        );

        return "pessoa/editar";
    }

    // =========================================================
    // ATUALIZAR USUÁRIO
    // =========================================================

    @PostMapping("/teste-post")
    @ResponseBody
    public String testePost() {

        System.out.println(
                "POST CHEGOU NO CONTROLLER!"
        );

        return "POST FUNCIONOU!";
    }

    @PostMapping("/atualizar")
    public String atualizar(
            @Valid @ModelAttribute("pessoa") Pessoa dados,
            BindingResult result,
            @RequestParam(
                    value = "foto",
                    required = false
            ) MultipartFile foto,
            Model model) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "perfis",
                    Perfil.values()
            );

            return "pessoa/editar";
        }

        Pessoa pessoa = pessoaRepository
                .findById(dados.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pessoa não encontrada: "
                                        + dados.getId()
                        )
                );

        // =====================================================
        // ATUALIZA DADOS PESSOAIS
        // =====================================================

        pessoa.setNome(dados.getNome());
        pessoa.setEmail(dados.getEmail());
        pessoa.setCpf(dados.getCpf());
        pessoa.setTelefone(dados.getTelefone());

        // =====================================================
        // ATUALIZA ENDEREÇO
        // =====================================================

        pessoa.setCep(dados.getCep());
        pessoa.setLogradouro(dados.getLogradouro());
        pessoa.setNumero(dados.getNumero());
        pessoa.setComplemento(dados.getComplemento());
        pessoa.setBairro(dados.getBairro());
        pessoa.setCidade(dados.getCidade());
        pessoa.setEstado(dados.getEstado());

        // =====================================================
        // ATUALIZA LOCALIZAÇÃO
        // =====================================================

        pessoa.setLatitude(dados.getLatitude());
        pessoa.setLongitude(dados.getLongitude());

        // =====================================================
        // ATUALIZA FOTO
        // =====================================================

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

                model.addAttribute(
                        "perfis",
                        Perfil.values()
                );

                return "pessoa/editar";
            }
        }

        // =====================================================
        // NÃO ALTERA:
        // senha
        // perfil
        // ativo
        // =====================================================

        pessoaRepository.save(pessoa);

        return "redirect:/pessoa/listagem";
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
