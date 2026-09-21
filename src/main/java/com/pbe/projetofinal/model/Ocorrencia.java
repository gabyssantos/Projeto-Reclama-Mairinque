package com.pbe.projetofinal.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ocorrencia")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =================================================
    // CLIENTE / SOLICITANTE
    // =================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Pessoa cliente;


    // =================================================
    // DADOS DA OCORRÊNCIA
    // =================================================

    private LocalDate dataOcorrencia;

    private LocalDate dataSolucao;

    @Column(length = 1000, nullable = false)
    private String descricaoOcorrencia;

    @Column(length = 1000)
    private String descricaoSolucao;

    private String categoriaOcorrencia;


    // =================================================
    // STATUS DA OCORRÊNCIA
    // =================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOcorrencia statusOcorrencia = StatusOcorrencia.RASCUNHO;


    // =================================================
    // FOTOS
    // =================================================

    private String fotoOcorrencia;

    private String fotoSolucao;


    // =================================================
    // LOCAL DA OCORRÊNCIA
    // =================================================

    private String bairro;

    private String rua;

    private String cep;


    // =================================================
    // JUSTIFICATIVA DA RECUSA
    // =================================================

    @Column(length = 1000)
    private String justificativaRecusa;


    // =================================================
    // GETTERS E SETTERS
    // =================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Pessoa getCliente() {
        return cliente;
    }

    public void setCliente(Pessoa cliente) {
        this.cliente = cliente;
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


    public String getCategoriaOcorrencia() {
        return categoriaOcorrencia;
    }

    public void setCategoriaOcorrencia(String categoriaOcorrencia) {
        this.categoriaOcorrencia = categoriaOcorrencia;
    }


    public StatusOcorrencia getStatusOcorrencia() {
        return statusOcorrencia;
    }

    public void setStatusOcorrencia(StatusOcorrencia statusOcorrencia) {
        this.statusOcorrencia = statusOcorrencia;
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


    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }


    public String getJustificativaRecusa() {
        return justificativaRecusa;
    }

    public void setJustificativaRecusa(String justificativaRecusa) {
        this.justificativaRecusa = justificativaRecusa;
    }
}