package com.example.qualimagem.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qualimagem.data.ItemQuizEntity
import com.example.qualimagem.data.repository.QuizRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

class AdminViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    companion object {
        const val ITENS_POR_PAGINA = 20
        val DIFICULDADES = listOf("Fácil", "Médio", "Difícil")
    }

    var uiState by mutableStateOf(AdminUiState())
        private set

    var formulario by mutableStateOf(FormularioItem())
        private set

    private var todosItens: List<ItemQuizEntity> = emptyList()

    private var itemEmEdicao: ItemQuizEntity? = null

    private val collator = Collator.getInstance(Locale("pt", "BR")).apply { strength = Collator.PRIMARY }

    init {
        viewModelScope.launch {
            quizRepository.buscarTodos().collect { itens ->
                todosItens = itens
                aplicarFiltros()
            }
        }
    }

    fun alterarBusca(texto: String) {
        uiState = uiState.copy(busca = texto, pagina = 0)
        aplicarFiltros()
    }

    fun selecionarCategoria(categoria: String) {
        uiState = uiState.copy(categoriaFiltro = categoria, pagina = 0)
        aplicarFiltros()
    }

    fun proximaPagina() {
        if (uiState.temProximaPagina) {
            uiState = uiState.copy(pagina = uiState.pagina + 1)
            aplicarFiltros()
        }
    }

    fun paginaAnterior() {
        if (uiState.temPaginaAnterior) {
            uiState = uiState.copy(pagina = uiState.pagina - 1)
            aplicarFiltros()
        }
    }

    private fun aplicarFiltros() {
        val busca = uiState.busca.trim()

        val filtrados = todosItens
            .filter { item ->
                (uiState.categoriaFiltro.isBlank() || item.categoria == uiState.categoriaFiltro) &&
                    (busca.isBlank() || item.nomeCorreto.contains(busca, ignoreCase = true))
            }
            .sortedWith(compareBy<ItemQuizEntity, String>(collator) { it.nomeCorreto })

        val ultimaPagina = if (filtrados.isEmpty()) 0 else (filtrados.size - 1) / ITENS_POR_PAGINA
        val pagina = uiState.pagina.coerceIn(0, ultimaPagina)

        uiState = uiState.copy(
            itensPagina = filtrados.drop(pagina * ITENS_POR_PAGINA).take(ITENS_POR_PAGINA),
            totalNoBanco = todosItens.size,
            totalFiltrado = filtrados.size,
            categorias = todosItens.map { it.categoria }.distinct().sortedWith(collator),
            pagina = pagina
        )
    }

    fun sincronizarApi() {
        if (uiState.sincronizandoApi) return

        viewModelScope.launch {
            uiState = uiState.copy(sincronizandoApi = true, mensagemSucesso = null, mensagemErro = null)

            val quantidadeAntes = todosItens.size
            val resultado = quizRepository.sincronizarBandeirasDaApi()

            if (resultado.isSuccess) {
                val quantidadeDepois = quizRepository.buscarTodos().first().size
                val novos = quantidadeDepois - quantidadeAntes
                uiState = uiState.copy(
                    sincronizandoApi = false,
                    ultimaSincronizacao = System.currentTimeMillis(),
                    mensagemSucesso = when {
                        novos == 1 -> "1 nova bandeira adicionada."
                        novos > 1 -> "$novos novas bandeiras adicionadas."
                        else -> "Catálogo já estava atualizado."
                    }
                )
            } else {
                uiState = uiState.copy(
                    sincronizandoApi = false,
                    mensagemErro = "Falha ao sincronizar. Verifique a internet e tente de novo."
                )
            }
        }
    }

    fun excluirItem(item: ItemQuizEntity) {
        viewModelScope.launch {
            val resultado = quizRepository.deletarItem(item)
            uiState = if (resultado.isSuccess) {
                uiState.copy(mensagemSucesso = "\"${item.nomeCorreto}\" foi excluído.", mensagemErro = null)
            } else {
                uiState.copy(mensagemErro = "Não foi possível excluir \"${item.nomeCorreto}\".", mensagemSucesso = null)
            }
        }
    }

    fun limparMensagens() {
        uiState = uiState.copy(mensagemSucesso = null, mensagemErro = null)
    }

    fun abrirFormulario(id: Int) {
        val item = todosItens.firstOrNull { it.id == id }
        itemEmEdicao = item

        formulario = if (item == null) {
            FormularioItem(categoria = uiState.categorias.firstOrNull() ?: "Bandeiras")
        } else {
            FormularioItem(
                editando = true,
                nomeCorreto = item.nomeCorreto,
                categoria = item.categoria,
                imagemUrl = item.imagemUrl,
                regiao = item.subcategoria,
                dica = item.dica,
                dificuldade = item.dificuldade
            )
        }
    }

    fun alterarNome(valor: String) {
        formulario = formulario.copy(nomeCorreto = valor, erros = formulario.erros - CampoFormulario.NOME)
    }

    fun alterarCategoria(valor: String) {
        formulario = formulario.copy(categoria = valor, erros = formulario.erros - CampoFormulario.CATEGORIA)
    }

    fun alterarImagemUrl(valor: String) {
        formulario = formulario.copy(imagemUrl = valor, erros = formulario.erros - CampoFormulario.IMAGEM_URL)
    }

    fun alterarRegiao(valor: String) {
        formulario = formulario.copy(regiao = valor)
    }

    fun alterarDica(valor: String) {
        formulario = formulario.copy(dica = valor, erros = formulario.erros - CampoFormulario.DICA)
    }

    fun alterarDificuldade(valor: String) {
        formulario = formulario.copy(dificuldade = valor)
    }

    private fun validar(f: FormularioItem): Map<CampoFormulario, String> {
        val erros = mutableMapOf<CampoFormulario, String>()
        val nome = f.nomeCorreto.trim()

        if (nome.isBlank()) erros[CampoFormulario.NOME] = "Informe o nome correto."
        if (f.categoria.isBlank()) erros[CampoFormulario.CATEGORIA] = "Informe a categoria."
        if (!f.imagemUrl.trim().startsWith("http://") && !f.imagemUrl.trim().startsWith("https://")) {
            erros[CampoFormulario.IMAGEM_URL] = "A URL deve começar com http:// ou https://"
        }
        if (f.dica.isBlank()) erros[CampoFormulario.DICA] = "Informe uma dica para o jogador."

        val duplicado = todosItens.any {
            it.id != itemEmEdicao?.id &&
                it.categoria.equals(f.categoria.trim(), ignoreCase = true) &&
                it.nomeCorreto.equals(nome, ignoreCase = true)
        }
        if (nome.isNotBlank() && duplicado) erros[CampoFormulario.NOME] = "Já existe \"$nome\" nesta categoria."

        return erros
    }

    fun salvarFormulario(aoConcluir: () -> Unit) {
        val f = formulario
        if (f.salvando) return

        val erros = validar(f)
        if (erros.isNotEmpty()) {
            formulario = f.copy(erros = erros)
            return
        }

        viewModelScope.launch {
            formulario = f.copy(salvando = true, mensagemErro = null)

            val original = itemEmEdicao
            val resultado = if (original != null) {
                quizRepository.atualizarItem(
                    original.copy(
                        nomeCorreto = f.nomeCorreto.trim(),
                        categoria = f.categoria.trim(),
                        imagemUrl = f.imagemUrl.trim(),
                        subcategoria = f.regiao.trim(),
                        dica = f.dica.trim(),
                        dificuldade = f.dificuldade
                    )
                )
            } else {
                quizRepository.inserirItem(
                    ItemQuizEntity(
                        nomeCorreto = f.nomeCorreto.trim(),
                        imagemUrl = f.imagemUrl.trim(),
                        categoria = f.categoria.trim(),
                        subcategoria = f.regiao.trim(),
                        dica = f.dica.trim(),
                        dificuldade = f.dificuldade
                    )
                )
            }

            if (resultado.isSuccess) {
                formulario = formulario.copy(salvando = false)
                uiState = uiState.copy(
                    mensagemSucesso = if (original != null) "\"${f.nomeCorreto.trim()}\" atualizado." else "\"${f.nomeCorreto.trim()}\" cadastrado.",
                    mensagemErro = null
                )
                itemEmEdicao = null
                aoConcluir()
            } else {
                formulario = formulario.copy(salvando = false, mensagemErro = "Não foi possível salvar. Tente novamente.")
            }
        }
    }
}
