QUAL É A IMAGEM?

Aplicativo Android de quiz em que o jogador vê uma imagem e precisa descobrir o que ela representa entre quatro alternativas. A primeira categoria disponível é Bandeiras: o app baixa os países de uma API pública, guarda tudo no banco local do aparelho e monta as partidas a partir desse catálogo. Além do jogo, o app tem ranking, galeria para estudar as imagens e uma área de administração para manter o catálogo.

Projeto desenvolvido como trabalho final em grupo da disciplina de Desenvolvimento Android.


1. O QUE O APP FAZ

Tela de entrada
Dois botões: Entrar como Usuário e Entrar como Administrador. Não há senha; a escolha só define o perfil da sessão. Ao voltar para essa tela a sessão é encerrada.

Jogar
O jogador informa um apelido, escolhe a categoria e o nível (Fácil, Médio ou Difícil) e inicia a partida. Se o banco ainda estiver vazio, a própria tela oferece um botão para baixar as bandeiras.

Cada partida tem 10 rodadas e o jogador começa com 3 vidas. Em cada rodada aparecem a imagem e quatro alternativas, sendo uma correta e três erradas sorteadas da mesma categoria. Existe um botão de dica que mostra a capital do país. Depois de responder, o app mostra se acertou ou errou e passa sozinho para a próxima pergunta em 1,5 segundo.

Pontuação por acerto:
  Fácil    100 pontos
  Médio    150 pontos
  Difícil  200 pontos

Errar custa uma vida. A partida termina quando as rodadas acabam ou quando as vidas chegam a zero. Na tela final aparecem pontuação, acertos, rodadas jogadas e vidas restantes, com as opções Jogar novamente e Voltar ao início. O resultado é salvo automaticamente no ranking.

Galeria
Lista todos os itens do catálogo com busca por nome, filtro por categoria e ordenação de A a Z ou de Z a A (respeitando acentos do português). Tocar em um item abre a tela de detalhe com a imagem, a região, o nível e a dica.

Ranking
Mostra um pódio com os três primeiros e a lista completa abaixo. Dá para buscar por jogador e filtrar por categoria, nível e período (Hoje, Últimos 7 dias ou Geral). A ordem é por pontuação; em caso de empate vence quem acertou mais e, persistindo o empate, quem jogou primeiro. Cada registro pode ter o apelido editado ou ser apagado, sempre com confirmação.

Área do administrador
Disponível apenas para quem entrou como Administrador. A aba Admin só aparece na barra inferior nesse perfil, e as rotas de administração mostram Acesso Negado para o perfil de usuário.
  Sincronizar Bandeiras da API: baixa os países e insere apenas os que ainda não existem no banco, informando quantos foram adicionados.
  Lista paginada do catálogo (20 itens por página) com busca e filtro por categoria.
  Cadastro de novo item e edição de item existente.
  Exclusão de itens.

O formulário valida os campos antes de salvar: nome, categoria e dica são obrigatórios, a URL da imagem precisa começar com http:// ou https://, e não é permitido cadastrar dois itens com o mesmo nome na mesma categoria.


2. DE ONDE VÊM OS DADOS

As bandeiras vêm do arquivo countriesV3.1.json do projeto REST Countries, hospedado no GitLab. Para cada país o app aproveita:

  nome em português      vira a resposta correta
  imagem PNG da bandeira vira a imagem da pergunta
  região                 vira a subcategoria
  capital                vira a dica
  população              define o nível

A regra de nível usa a população como aproximação de quão conhecido o país é:
  mais de 50 milhões de habitantes   Fácil
  entre 10 e 50 milhões              Médio
  até 10 milhões                     Difícil

Depois da primeira sincronização o jogo funciona sem internet, já que as perguntas saem do banco local. A internet continua sendo necessária para carregar as imagens das bandeiras, que são buscadas pela URL.


3. TECNOLOGIAS

  Kotlin
  Jetpack Compose com Material 3 para as telas
  Navigation Compose para a navegação entre telas e a barra inferior
  ViewModel e StateFlow para o estado das telas
  Room para o banco de dados local (com KSP)
  Retrofit com conversor Gson para consumir a API
  Coil para carregar imagens a partir de URL
  Coroutines e Flow para as operações assíncronas

Configuração do projeto: minSdk 24, targetSdk e compileSdk 37, Java 11, Android Gradle Plugin 9.3.3 e Kotlin 2.2.10. As versões de todas as bibliotecas estão em gradle/libs.versions.toml.


4. ARQUITETURA

O app segue MVVM com repositórios. As telas não acessam o banco nem a API diretamente: elas leem o estado exposto pelo ViewModel e chamam funções dele quando o usuário interage. Os ViewModels recebem os repositórios por meio de Factories, e os repositórios conversam com o Room e com o Retrofit.

Fluxo resumido:
  Tela (Compose)  >  ViewModel  >  Repository  >  DAO do Room ou serviço Retrofit

Como o Room devolve as consultas em Flow, qualquer alteração no banco (uma sincronização, um cadastro no painel admin, uma partida encerrada) aparece na hora nas outras telas, sem precisar recarregar nada manualmente.

Os repositórios tratam erros e devolvem Result, de modo que o ViewModel só decide qual mensagem mostrar ao usuário.


5. BANCO DE DADOS

Banco Room chamado quiz_database, com duas tabelas.

itens_quiz
  id             chave primária gerada automaticamente
  nomeCorreto    resposta certa
  imagemUrl      endereço da imagem
  categoria      por padrão "Bandeiras"
  subcategoria   região do país
  dica           texto exibido ao pedir dica
  dificuldade    Fácil, Médio ou Difícil

placares
  id               chave primária gerada automaticamente
  nomeJogador      apelido informado antes da partida
  categoriaJogada  categoria da partida
  pontuacao        pontos totais
  acertos          quantidade de respostas corretas
  dificuldade      nível escolhido
  dataHora         momento em que a partida terminou (milissegundos)


6. ESTRUTURA DE PASTAS

Todo o código fica em app/src/main/java/com/example/qualimagem.

MainActivity.kt
  Ponto de entrada. Aplica o tema e abre a navegação.

data
  AppDatabase.kt, ItemQuizEntity.kt, ItemQuizDAO.kt, PlacarEntity.kt, PlacarDAO.kt
  Banco Room, tabelas e consultas.

data/remote
  PaisDto.kt, PaisesApiService.kt, RetrofitClient.kt
  Modelo da resposta da API e configuração do Retrofit.

data/repository
  QuizRepository.kt     catálogo de itens e sincronização com a API
  PlacarRepository.kt   resultados das partidas

navigation
  Rotas.kt          nomes de todas as rotas do app
  AppNavigation.kt  NavHost, criação dos ViewModels e controle de acesso ao admin
  BottomNavBar.kt   barra inferior com Jogar, Galeria, Ranking e Admin

viewmodel
  SessaoViewModel   perfil da sessão (usuário ou administrador)
  JogoViewModel     regras da partida: sorteio, respostas, vidas, pontos, dica e gravação do placar
  RankingViewModel  ranking e galeria, com seus filtros e ordenações
  AdminViewModel    painel do administrador e formulário de cadastro e edição
  Cada um tem seu arquivo de UiState e, quando recebe repositórios, sua Factory.

ui
  LoginScreen, GaleriaScreen, DetalheItemScreen, RankingScreen e componentes compartilhados.

ui/jogo
  InicioJogoScreen, JogoQuizScreen, FimDeJogoConteudo, componentes e cores da partida.

ui/admin
  AdminDashboardScreen e AdminFormularioScreen.

ui/theme e ui/components
  Tema escuro baseado no protótipo do Figma, tipografia e componentes reutilizáveis.

docs
  Material de apoio para a apresentação da parte do motor do jogo.


7. COMO RODAR

Requisitos
  Android Studio em versão recente, compatível com o Android Gradle Plugin 9.3.3
  SDK do Android 37 instalado
  Emulador ou celular com Android 7.0 (API 24) ou superior
  Conexão com a internet na primeira execução

Pelo Android Studio
  1. Abra a pasta do projeto pelo Android Studio (File, Open).
  2. Aguarde a sincronização do Gradle terminar.
  3. Selecione um emulador ou um aparelho conectado.
  4. Clique em Run.

Pela linha de comando, na pasta do projeto
  Windows:        gradlew.bat assembleDebug
  Linux ou Mac:   ./gradlew assembleDebug
  O APK gerado fica em app/build/outputs/apk/debug.

Primeiro uso
  O banco começa vazio. Para ter perguntas, faça uma das duas coisas:
  Entre como Administrador e toque em Sincronizar Bandeiras da API, ou
  Entre como Usuário e use o botão de baixar bandeiras que aparece na tela de jogo quando não há categorias.
  Depois disso, informe um apelido, escolha o nível e comece a partida.


8. DIVISÃO DO TRABALHO

O desenvolvimento foi dividido entre os integrantes do grupo, e o histórico do Git registra cada etapa:

  Banco de dados Room com as entidades e os DAOs
  Integração com a API via Retrofit e criação dos repositórios
  Pessoa 3: motor do jogo, com as telas de início, quiz e fim de partida
  Pessoa 4: ranking com pódio e filtros, galeria e tela de detalhe
  Pessoa 5: tela de entrada, sessão, tema escuro do Figma, barra inferior e navegação
  Pessoa 6: painel do administrador, sincronização, busca, paginação, exclusão e formulário com validação


9. LIMITAÇÕES CONHECIDAS

  A entrada como administrador não pede senha. A separação de perfis serve para organizar o app, não como segurança.
  Por enquanto só existe a categoria Bandeiras vinda da API. Outras categorias podem ser criadas manualmente pelo painel do administrador, e elas aparecem no jogo automaticamente.
  Para uma categoria aparecer jogável, ela precisa de pelo menos quatro itens e de algum item no nível escolhido.
  As imagens dependem de internet para carregar, mesmo com o catálogo já salvo.
