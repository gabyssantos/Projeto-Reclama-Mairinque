package com.pbe.projetofinal.model;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataOcorrencia;
    private LocalDate dataSolucao;

    @Column(length = 1000)
    private String descricaoOcorrencia;

    @Column(length = 1000)
    private String descricaoSolucao;

    private String status;

    private String fotoOcorrencia;
    private String fotoSolucao;

    private String bairro;
    private String rua;

    private String nome;
    private String telefone;

    // GETTERS E SETTERS


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataOcorrencia() {
        return dataOcorrencia;
    }

    public void setDataOcorrencia(LocalDate dataOcorrencia) {
        this.dataOcorrencia = dataOcorrencia;
    }

    public LocalDate getDataSolucao() {
        return dataSolucao;
    }

    public void setDataSolucao(LocalDate dataSolucao) {
        this.dataSolucao = dataSolucao;
    }

    public String getDescricaoOcorrencia() {
        return descricaoOcorrencia;
    }

    public void setDescricaoOcorrencia(String descricaoOcorrencia) {
        this.descricaoOcorrencia = descricaoOcorrencia;
    }

    public String getDescricaoSolucao() {
        return descricaoSolucao;
    }

    public void setDescricaoSolucao(String descricaoSolucao) {
        this.descricaoSolucao = descricaoSolucao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFotoOcorrencia() {
        return fotoOcorrencia;
    }

    public void setFotoOcorrencia(String fotoOcorrencia) {
        this.fotoOcorrencia = fotoOcorrencia;
    }

    public String getFotoSolucao() {
        return fotoSolucao;
    }

    public void setFotoSolucao(String fotoSolucao) {
        this.fotoSolucao = fotoSolucao;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
