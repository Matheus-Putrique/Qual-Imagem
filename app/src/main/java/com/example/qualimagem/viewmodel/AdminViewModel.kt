package com.example.qualimagem.viewmodel

// ╔══════════════════════════════════════════════╗
// ║  AdminViewModel.kt                           ║
// ║  Regras da área do Administrador:            ║
// ║  sincronizar a API, listar/buscar/paginar,   ║
// ║  criar, editar e excluir itens (CRUD)        ║
// ╚══════════════════════════════════════════════╝

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

    // Estado: painel (só o ViewModel pode mudar → private set)
    var uiState by mutableStateOf(AdminUiState())
        private set

    // Estado: formulário de criar/editar
    var formulario by mutableStateOf(FormularioItem())
        private set

    // Lista completa do banco (os filtros sempre partem dela)
    private var todosItens: List<ItemQuizEntity> = emptyList()

    // Item que está sendo editado (null = cadastrando um novo)
    private var itemEmEdicao: ItemQuizEntity? = null

    // Ordena "Alândia" antes de "Alemanha" (ignora acentos)
    private val collator = Collator.getInstance(Locale("pt", "BR")).apply { strength = Collator.PRIMARY }

    init {
        // Read do CRUD: o Flow do Room avisa sempre que a tabela muda
        viewModelScope.launch {
            quizRepository.buscarTodos().collect { itens ->
                todosItens = itens
                aplicarFiltros()
            }
        }
    }

    // ------------------------------------------------------------------
    // Painel: busca, filtro e paginação
    // ------------------------------------------------------------------

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

        // Se a lista diminuiu (ex.: excluiu o último item da página), volta para uma página válida
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

    // ------------------------------------------------------------------
    // Sincronização com a API (Retrofit → Repository → Room)
    // ------------------------------------------------------------------

    fun sincronizarApi() {
        if (uiState.sincronizandoApi) return   // evita dois cliques seguidos

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

    // ------------------------------------------------------------------
    // Delete
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Formulário: Create e Update
    // ------------------------------------------------------------------

    /** Abre o formulário vazio (id = -1) ou preenchido com o item a editar. */
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

    // Cada campo tem sua função; ao digitar, o erro daquele campo some
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

    /** Valida os campos; retorna um mapa vazio quando está tudo certo. */
    private fun validar(f: FormularioItem): Map<CampoFormulario, String> {
        val erros = mutableMapOf<CampoFormulario, String>()
        val nome = f.nomeCorreto.trim()

        if (nome.isBlank()) erros[CampoFormulario.NOME] = "Informe o nome correto."
        if (f.categoria.isBlank()) erros[CampoFormulario.CATEGORIA] = "Informe a categoria."
        if (!f.imagemUrl.trim().startsWith("http://") && !f.imagemUrl.trim().startsWith("https://")) {
            erros[CampoFormulario.IMAGEM_URL] = "A URL deve começar com http:// ou https://"
        }
        if (f.dica.isBlank()) erros[CampoFormulario.DICA] = "Informe uma dica para o jogador."

        // Não deixa cadastrar dois itens com o mesmo nome na mesma categoria
        val duplicado = todosItens.any {
            it.id != itemEmEdicao?.id &&
                it.categoria.equals(f.categoria.trim(), ignoreCase = true) &&
                it.nomeCorreto.equals(nome, ignoreCase = true)
        }
        if (nome.isNotBlank() && duplicado) erros[CampoFormulario.NOME] = "Já existe \"$nome\" nesta categoria."

        return erros
    }

    /** Insere (Create) ou atualiza (Update) e chama [aoConcluir] se deu certo. */
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
                // Update: copy() mantém o MESMO id, e é pelo id que o @Update acha a linha
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
                // Create: id = 0 faz o Room gerar um id novo (autoGenerate)
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
