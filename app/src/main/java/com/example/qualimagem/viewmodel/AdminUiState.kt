package com.example.qualimagem.viewmodel

import com.example.qualimagem.data.ItemQuizEntity

data class AdminUiState(
    val itensPagina: List<ItemQuizEntity> = emptyList(),
    val totalNoBanco: Int = 0,
    val totalFiltrado: Int = 0,

    val busca: String = "",
    val categorias: List<String> = emptyList(),
    val categoriaFiltro: String = "",
    val pagina: Int = 0,

    val sincronizandoApi: Boolean = false,
    val ultimaSincronizacao: Long? = null,

    val mensagemSucesso: String? = null,
    val mensagemErro: String? = null
) {
    val totalPaginas: Int
        get() = if (totalFiltrado == 0) 1 else (totalFiltrado + AdminViewModel.ITENS_POR_PAGINA - 1) / AdminViewModel.ITENS_POR_PAGINA

    val temPaginaAnterior: Boolean get() = pagina > 0
    val temProximaPagina: Boolean get() = pagina < totalPaginas - 1

    val primeiroDaPagina: Int get() = if (totalFiltrado == 0) 0 else pagina * AdminViewModel.ITENS_POR_PAGINA + 1
    val ultimoDaPagina: Int get() = primeiroDaPagina - 1 + itensPagina.size
}

enum class CampoFormulario { NOME, CATEGORIA, IMAGEM_URL, DICA }

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
