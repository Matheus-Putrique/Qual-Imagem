package com.example.qualimagem.viewmodel

// ╔══════════════════════════════════════════════╗
// ║  AdminUiState.kt                             ║
// ║  Estado das telas do Administrador:          ║
// ║  painel (lista, busca, sync) e formulário    ║
// ╚══════════════════════════════════════════════╝

import com.example.qualimagem.data.ItemQuizEntity

/** Estado do Painel Admin (lista do catálogo + sincronização com a API). */
data class AdminUiState(
    // Lista já filtrada e cortada na página atual
    val itensPagina: List<ItemQuizEntity> = emptyList(),
    val totalNoBanco: Int = 0,
    val totalFiltrado: Int = 0,

    // Filtros
    val busca: String = "",
    val categorias: List<String> = emptyList(),
    val categoriaFiltro: String = "",   // "" = todas
    val pagina: Int = 0,                // começa em 0

    // Sincronização com a API (Retrofit)
    val sincronizandoApi: Boolean = false,
    val ultimaSincronizacao: Long? = null,

    // Mensagens de retorno para o usuário
    val mensagemSucesso: String? = null,
    val mensagemErro: String? = null
) {
    val totalPaginas: Int
        get() = if (totalFiltrado == 0) 1 else (totalFiltrado + AdminViewModel.ITENS_POR_PAGINA - 1) / AdminViewModel.ITENS_POR_PAGINA

    val temPaginaAnterior: Boolean get() = pagina > 0
    val temProximaPagina: Boolean get() = pagina < totalPaginas - 1

    /** Posição (1, 21, 41...) do primeiro item exibido, para o texto "Exibindo 1–20 de 250". */
    val primeiroDaPagina: Int get() = if (totalFiltrado == 0) 0 else pagina * AdminViewModel.ITENS_POR_PAGINA + 1
    val ultimoDaPagina: Int get() = primeiroDaPagina - 1 + itensPagina.size
}

/** Campos do formulário que podem ter erro de validação. */
enum class CampoFormulario { NOME, CATEGORIA, IMAGEM_URL, DICA }

/** Rascunho do formulário de criar/editar item (só vai para o banco ao apertar "Salvar"). */
data class FormularioItem(
    val editando: Boolean = false,
    val nomeCorreto: String = "",
    val categoria: String = "Bandeiras",
    val imagemUrl: String = "",
    val regiao: String = "",
    val dica: String = "",
    val dificuldade: String = "Fácil",
    val salvando: Boolean = false,
    val erros: Map<CampoFormulario, String> = emptyMap(),
    val mensagemErro: String? = null
)
