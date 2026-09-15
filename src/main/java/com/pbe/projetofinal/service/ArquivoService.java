package com.pbe.projetofinal.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ArquivoService {

    // Pasta uploads dentro do projeto
    private final Path diretorio =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("src")
                    .resolve("main")
                    .resolve("resources")
                    .resolve("static")
                    .resolve("uploads");

    public String salvarFoto(MultipartFile arquivo)
            throws IOException {

        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }

        // Garante que a pasta exista
        Files.createDirectories(diretorio);

        // Pega o nome original do arquivo
        String nomeOriginal =
                arquivo.getOriginalFilename();

        String extensao = "";

        if (nomeOriginal != null
                && nomeOriginal.contains(".")) {

            extensao = nomeOriginal.substring(
                    nomeOriginal.lastIndexOf(".")
            ).toLowerCase();
        }

        // Gera um nome único
        String nomeArquivo =
                UUID.randomUUID() + extensao;

        // Caminho completo do arquivo
        Path caminho =
                diretorio.resolve(nomeArquivo);

        // Salva a imagem
        arquivo.transferTo(caminho.toFile());

        // Caminho utilizado pelo navegador
        return "/uploads/" + nomeArquivo;
    }
}
