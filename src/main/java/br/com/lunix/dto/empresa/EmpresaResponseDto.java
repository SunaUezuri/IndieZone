package br.com.lunix.dto.empresa;

// DTO que mostra a empresa dados de uma empresa
public record EmpresaResponseDto(String id, String nome, String paisOrigem, String urlLogo) {
}
