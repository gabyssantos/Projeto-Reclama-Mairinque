package com.pbe.projetofinal.controller;

import com.pbe.projetofinal.model.Pessoa;
import com.pbe.projetofinal.repository.PessoaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final PessoaRepository pessoaRepository;

    public LoginController(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    // =========================================================
    // PÁGINA DE LOGIN
    // =========================================================

    @GetMapping
    public String login(HttpSession session) {

        // Se já estiver logado, vai direto para o dashboard
        if (session.getAttribute("pessoaLogada") != null) {
            return "redirect:/pessoa/dashboard";
        }

        return "home/login";
    }

    // =========================================================
    // AUTENTICAR
    // =========================================================

    @PostMapping
    public String autenticar(
            @RequestParam String email,
            @RequestParam String senha,
            HttpSession session,
            Model model) {

        Pessoa pessoa = pessoaRepository
                .findByEmail(email)
                .orElse(null);

        // Usuário não encontrado
        if (pessoa == null) {

            model.addAttribute(
                    "erro",
                    "E-mail ou senha incorretos."
            );

            return "home/login";
        }

        // Usuário desativado
        if (Boolean.FALSE.equals(pessoa.getAtivo())) {

            model.addAttribute(
                    "erro",
                    "Este usuário está desativado."
            );

            return "home/login";
        }

        // Senha incorreta
        if (pessoa.getSenha() == null
                || !pessoa.getSenha().equals(senha)) {

            model.addAttribute(
                    "erro",
                    "E-mail ou senha incorretos."
            );

            return "home/login";
        }

        // =====================================================
        // LOGIN REALIZADO
        // =====================================================

        session.setAttribute(
                "pessoaLogada",
                pessoa
        );

        return "redirect:/pessoa/dashboard";
    }

    // =========================================================
    // SAIR
    // =========================================================

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}