package com.example.qualimagem.ui.admin

// ╔══════════════════════════════════════════════╗
// ║  AdminFormularioScreen.kt                    ║
// ║  FORMULÁRIO: cadastrar item novo (@Insert)   ║
// ║  ou editar item existente (@Update)          ║
// ║  Recebe o id pela rota admin_formulario?id=  ║
// ╚══════════════════════════════════════════════╝

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.qualimagem.ui.ChipFiltro
import com.example.qualimagem.ui.RodapeInfo
import com.example.qualimagem.ui.components.BarraTopo
import com.example.qualimagem.ui.components.BotaoPrincipal
import com.example.qualimagem.ui.components.CardPadrao
import com.example.qualimagem.ui.jogo.ChipOpcao
import com.example.qualimagem.ui.theme.QualImagemTema
import com.example.qualimagem.viewmodel.AdminViewModel
import com.example.qualimagem.viewmodel.CampoFormulario
import com.example.qualimagem.viewmodel.FormularioItem

/** Versão "com ViewModel": carrega o item pelo id e repassa as ações. */
@Composable
fun AdminFormularioScreen(
    id: Int,
    viewModel: AdminViewModel,
    onVoltar: () -> Unit
) {
    // Ao abrir a tela (ou trocar o id), prepara o formulário: vazio ou com os dados do item
    LaunchedEffect(id) {
        viewModel.abrirFormulario(id)
    }

    BackHandler(onBack = onVoltar)

    AdminFormularioConteudo(
        formulario = viewModel.formulario,
        categoriasExistentes = viewModel.uiState.categorias,
        onNomeChange = viewModel::alterarNome,
        onCategoriaChange = viewModel::alterarCategoria,
        onImagemUrlChange = viewModel::alterarImagemUrl,
        onRegiaoChange = viewModel::alterarRegiao,
        onDicaChange = viewModel::alterarDica,
        onDificuldadeChange = viewModel::alterarDificuldade,
        onSalvar = { viewModel.salvarFormulario(aoConcluir = onVoltar) },
        onVoltar = onVoltar
    )
}

@Composable
fun AdminFormularioConteudo(
    formulario: FormularioItem,
    categoriasExistentes: List<String>,
    onNomeChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onImagemUrlChange: (String) -> Unit,
    onRegiaoChange: (String) -> Unit,
    onDicaChange: (String) -> Unit,
    onDificuldadeChange: (String) -> Unit,
    onSalvar: () -> Unit,
    onVoltar: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // O Scaffold principal (AppNavigation) já reserva o espaço da barra de status
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            BarraTopo(
                titulo = if (formulario.editando) "Editar item" else "Novo item",
                onVoltar = onVoltar
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            CabecalhoPreview(formulario)

            Spacer(Modifier.height(20.dp))

            CampoFormularioTexto(
                rotulo = "Nome Correto",
                valor = formulario.nomeCorreto,
                onValorChange = onNomeChange,
                placeholder = "Ex.: Japão",
                icone = Icons.Outlined.Title,
                erro = formulario.erros[CampoFormulario.NOME],
                capitalizar = true
            )

            CampoFormularioTexto(
                rotulo = "Categoria",
                valor = formulario.categoria,
                onValorChange = onCategoriaChange,
                placeholder = "Ex.: Bandeiras",
                icone = Icons.Outlined.Category,
                erro = formulario.erros[CampoFormulario.CATEGORIA],
                capitalizar = true
            )
            // Atalhos: categorias que já existem no banco (ou digite uma nova acima)
            if (categoriasExistentes.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriasExistentes.forEach { categoria ->
                        ChipFiltro(
                            texto = categoria,
                            ativo = categoria == formulario.categoria,
                            onClick = { onCategoriaChange(categoria) }
                        )
                    }
                }
            }

            CampoFormularioTexto(
                rotulo = "URL da Imagem",
                valor = formulario.imagemUrl,
                onValorChange = onImagemUrlChange,
                placeholder = "https://flagcdn.com/w640/jp.png",
                icone = Icons.Outlined.Link,
                erro = formulario.erros[CampoFormulario.IMAGEM_URL],
                tipoTeclado = KeyboardType.Uri
            )

            CampoFormularioTexto(
                rotulo = "Região",
                valor = formulario.regiao,
                onValorChange = onRegiaoChange,
                placeholder = "Ex.: Asia",
                icone = Icons.Outlined.Place,
                erro = null,
                capitalizar = true
            )

            CampoFormularioTexto(
                rotulo = "Dica",
                valor = formulario.dica,
                onValorChange = onDicaChange,
                placeholder = "Ex.: Capital: Tóquio",
                icone = Icons.Outlined.Lightbulb,
                erro = formulario.erros[CampoFormulario.DICA],
                capitalizar = true
            )

            Text("Dificuldade", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminViewModel.DIFICULDADES.forEach { nivel ->
                    ChipOpcao(
                        texto = nivel,
                        selecionado = nivel == formulario.dificuldade,
                        onClick = { onDificuldadeChange(nivel) }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            formulario.mensagemErro?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
            }

            BotaoPrincipal(
                texto = "Salvar",
                icone = Icons.Rounded.Check,
                carregando = formulario.salvando,
                onClick = onSalvar
            )
            Spacer(Modifier.height(12.dp))
            RodapeInfo(
                "As alterações serão aplicadas ao catálogo.",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Topo do formulário: prévia da imagem digitada + "CATÁLOGO DE BANDEIRAS". */
@Composable
private fun CabecalhoPreview(formulario: FormularioItem) {
    CardPadrao {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (formulario.imagemUrl.isBlank()) {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    // Prévia ao vivo: o Coil carrega a URL enquanto o admin digita
                    AsyncImage(
                        model = formulario.imagemUrl.trim(),
                        contentDescription = "Prévia da imagem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "CATÁLOGO DE ${formulario.categoria.ifBlank { "ITENS" }.uppercase()}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Text(
                    if (formulario.editando) "Informações do item" else "Novo item do catálogo",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

/** Rótulo acima + OutlinedTextField + mensagem de erro (padrão de formulário das aulas). */
@Composable
private fun CampoFormularioTexto(
    rotulo: String,
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    icone: ImageVector,
    erro: String?,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    capitalizar: Boolean = false
) {
    Text(rotulo, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icone, contentDescription = null, modifier = Modifier.size(20.dp)) },
        isError = erro != null,
        supportingText = if (erro != null) { { Text(erro) } } else null,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            capitalization = if (capitalizar) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
            imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            errorContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(if (erro != null) 4.dp else 16.dp))
}

// ── Preview ──────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminFormularioPreview() {
    QualImagemTema {
        AdminFormularioConteudo(
            formulario = FormularioItem(
                editando = true,
                nomeCorreto = "Japão",
                categoria = "Bandeiras",
                imagemUrl = "https://flagcdn.com/w640/jp.png",
                regiao = "Asia",
                dica = "Capital: Tóquio",
                dificuldade = "Fácil"
            ),
            categoriasExistentes = listOf("Bandeiras"),
            onNomeChange = {}, onCategoriaChange = {}, onImagemUrlChange = {}, onRegiaoChange = {},
            onDicaChange = {}, onDificuldadeChange = {}, onSalvar = {}, onVoltar = {}
        )
    }
}
