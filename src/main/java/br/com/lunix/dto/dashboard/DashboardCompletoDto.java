package br.com.lunix.dto.dashboard;

// Dto que reúne todas as informações dos anteriores
public record DashboardCompletoDto(
        DashboardUsuariosDto usuarios,
        DashboardConteudoDto conteudo,
        DashboardEngajamentoDto engajamento
) {
}
