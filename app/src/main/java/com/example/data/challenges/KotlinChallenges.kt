package com.example.data.challenges

import com.example.model.ActivityDifficulty
import com.example.model.PracticalActivity
import com.example.model.Technology

object KotlinChallenges {

    private data class KtSeed(
        val titlePt: String,
        val titleEn: String,
        val instructionsPt: String,
        val instructionsEn: String,
        val starterCode: String,
        val solutionCode: String,
        val hintPt: String,
        val hintEn: String,
        val keywords: List<String>,
        val explanationPt: String,
        val explanationEn: String
    )

    private val beginnerSeeds = listOf(
        KtSeed(
            titlePt = "Imutabilidade (val) e String Templates",
            titleEn = "Immutability (val) & String Templates",
            instructionsPt = "Declare uma referência imutável (val) e interpole a variável usando $.",
            instructionsEn = "Declare an immutable reference (val) and interpolate it using $.",
            starterCode = "___ linguagem = \"Kotlin\"\nprintln(\"Aprendendo \$linguagem no Android!\")",
            solutionCode = "val linguagem = \"Kotlin\"\nprintln(\"Aprendendo \$linguagem no Android!\")",
            hintPt = "Use a palavra-chave 'val' para variáveis imutáveis.",
            hintEn = "Use 'val' for read-only references.",
            keywords = listOf("val", "linguagem", "Kotlin"),
            explanationPt = "Em Kotlin, 'val' cria referências imutáveis reduzindo efeitos colaterais.",
            explanationEn = "In Kotlin, 'val' declares read-only references promoting immutability."
        ),
        KtSeed(
            titlePt = "Null Safety com Safe Call (?.) e Elvis (?:)",
            titleEn = "Null Safety with Safe Call (?.) & Elvis (?:)",
            instructionsPt = "Obtenha o tamanho com safe call (?.) e forneça 0 como fallback com (?:).",
            instructionsEn = "Safely read nullable string length via (?.) and supply 0 fallback with (?:).",
            starterCode = "val apelido: String? = null\nval tamanho = apelido___length ___ 0\nprintln(tamanho)",
            solutionCode = "val apelido: String? = null\nval tamanho = apelido?.length ?: 0\nprintln(tamanho)",
            hintPt = "Use '?.' antes de length e '?:' antes de 0.",
            hintEn = "Use '?.' for safe call and '?:' for Elvis fallback.",
            keywords = listOf("?.", "?:", "length", "0"),
            explanationPt = "Tipos anuláveis (String?) obrigam tratamento seguro evitando NullPointerExceptions.",
            explanationEn = "Kotlin nullable types (T?) enforce null handling at compile time."
        ),
        KtSeed(
            titlePt = "Controle de Fluxo com Expressão 'when'",
            titleEn = "Flow Control with 'when' Expression",
            instructionsPt = "Complete a expressão 'when' retornando 'Sucesso' para 200 e 'Erro' para else.",
            instructionsEn = "Complete the 'when' expression returning 'Sucesso' for 200 and 'Erro' for else.",
            starterCode = "val codigo = 200\nval msg = ___ (codigo) {\n    200 -> \"Sucesso\"\n    ___ -> \"Erro\"\n}",
            solutionCode = "val codigo = 200\nval msg = when (codigo) {\n    200 -> \"Sucesso\"\n    else -> \"Erro\"\n}",
            hintPt = "Use 'when' e 'else'.",
            hintEn = "Use 'when' and 'else'.",
            keywords = listOf("when", "200 ->", "else ->"),
            explanationPt = "A expressão 'when' substitui switch/case retornando valores diretamente.",
            explanationEn = "'when' replaces legacy switch/case and evaluates directly as an expression."
        ),
        KtSeed(
            titlePt = "Data Classes e Métodos Gerados",
            titleEn = "Data Classes & Auto-generated Methods",
            instructionsPt = "Crie uma 'data class' Aluno com propriedades imutáveis id e nome.",
            instructionsEn = "Declare a 'data class' Aluno with immutable id and nome properties.",
            starterCode = "___ class Aluno(val id: Int, val nome: String)\nval a = Aluno(1, \"Mariana\")",
            solutionCode = "data class Aluno(val id: Int, val nome: String)\nval a = Aluno(1, \"Mariana\")",
            hintPt = "Adicione 'data' antes de 'class'.",
            hintEn = "Prepend 'data' before 'class'.",
            keywords = listOf("data class", "Aluno", "val id", "val nome"),
            explanationPt = "Data classes geram equals(), hashCode(), toString() e copy() automaticamente.",
            explanationEn = "Data classes auto-generate equals(), hashCode(), toString(), and copy()."
        ),
        KtSeed(
            titlePt = "Parâmetros com Valores Padrão",
            titleEn = "Default Function Arguments",
            instructionsPt = "Defina o valor padrão 1 para o parâmetro 'passo' da função incrementar.",
            instructionsEn = "Set default value 1 for parameter 'passo' in incrementar.",
            starterCode = "fun incrementar(valor: Int, passo: Int = ___): Int = valor + passo\nprintln(incrementar(10))",
            solutionCode = "fun incrementar(valor: Int, passo: Int = 1): Int = valor + passo\nprintln(incrementar(10))",
            hintPt = "Atribua '= 1' ao parâmetro passo.",
            hintEn = "Assign '= 1' to passo.",
            keywords = listOf("passo: Int = 1", "fun incrementar"),
            explanationPt = "Valores padrão eliminam múltiplas sobrecargas repetitivas de funções.",
            explanationEn = "Default parameters eliminate repetitive method overloads."
        ),
        KtSeed(
            titlePt = "Listas Imutáveis vs Mutáveis",
            titleEn = "Immutable vs Mutable Lists",
            instructionsPt = "Crie uma lista mutável com mutableListOf() e adicione 'Kotlin'.",
            instructionsEn = "Create a mutable list using mutableListOf() and add 'Kotlin'.",
            starterCode = "val techs = ___<String>(\"Compose\")\ntechs.add(\"Kotlin\")",
            solutionCode = "val techs = mutableListOf<String>(\"Compose\")\ntechs.add(\"Kotlin\")",
            hintPt = "Use a função mutableListOf.",
            hintEn = "Use mutableListOf.",
            keywords = listOf("mutableListOf", "techs.add"),
            explanationPt = "Kotlin separa coleções somente leitura (listOf) de coleções mutáveis (mutableListOf).",
            explanationEn = "Kotlin explicitly separates read-only lists (listOf) from mutable lists (mutableListOf)."
        ),
        KtSeed(
            titlePt = "Intervalos (Ranges) no Laço for",
            titleEn = "Ranges in for Loops",
            instructionsPt = "Percorra os números de 1 até 5 inclusivo usando o operador de intervalo (..).",
            instructionsEn = "Loop through numbers 1 through 5 inclusive using the range operator (..).",
            starterCode = "for (i in 1___5) {\n    println(i)\n}",
            solutionCode = "for (i in 1..5) {\n    println(i)\n}",
            hintPt = "Use dois pontos seguidos '..'.",
            hintEn = "Use the '..' range operator.",
            keywords = listOf("in 1..5", "for"),
            explanationPt = "Ranges como 1..5 ou 1 until 5 tornam iterações numéricas expressivas e seguras.",
            explanationEn = "Ranges like 1..5 or 1 until 5 express numeric bounds cleanly."
        ),
        KtSeed(
            titlePt = "Single-Expression Functions",
            titleEn = "Single-Expression Functions",
            instructionsPt = "Simplifique a função dobro retornando diretamente com '=' sem chaves.",
            instructionsEn = "Write a single-expression function dobro returning directly with '='.",
            starterCode = "fun dobro(n: Int): Int ___ n * 2\nprintln(dobro(7))",
            solutionCode = "fun dobro(n: Int): Int = n * 2\nprintln(dobro(7))",
            hintPt = "Use o sinal de igual '='.",
            hintEn = "Use the '=' assignment token.",
            keywords = listOf("fun dobro", "= n * 2"),
            explanationPt = "Funções de expressão única reduzem boilerplate mantendo clareza total.",
            explanationEn = "Single-expression functions eliminate braces for concise one-line logic."
        ),
        KtSeed(
            titlePt = "Mapas com Pares 'to'",
            titleEn = "Maps with 'to' Infix Pairs",
            instructionsPt = "Crie um mapa imutável associando chaves a valores usando a função infixa 'to'.",
            instructionsEn = "Construct an immutable map linking keys to values via infix 'to'.",
            starterCode = "val portas = mapOf(\"HTTP\" ___ 80, \"HTTPS\" ___ 443)\nprintln(portas[\"HTTPS\"])",
            solutionCode = "val portas = mapOf(\"HTTP\" to 80, \"HTTPS\" to 443)\nprintln(portas[\"HTTPS\"])",
            hintPt = "Use a palavra infixa 'to'.",
            hintEn = "Use infix keyword 'to'.",
            keywords = listOf("mapOf", "to 80", "to 443"),
            explanationPt = "'to' cria objetos Pair<A, B> de forma super legível em dicionários.",
            explanationEn = "'to' builds Pair<A, B> instances with fluent natural readability."
        ),
        KtSeed(
            titlePt = "Smart Casts após Verificação 'is'",
            titleEn = "Smart Casts after 'is' Check",
            instructionsPt = "Verifique se 'obj' é String usando 'is' para acessar .length automaticamente.",
            instructionsEn = "Check if 'obj' is String using 'is' to access .length automatically.",
            starterCode = "fun tamanhoTexto(obj: Any): Int {\n    if (obj ___ String) return obj.length\n    return 0\n}",
            solutionCode = "fun tamanhoTexto(obj: Any): Int {\n    if (obj is String) return obj.length\n    return 0\n}",
            hintPt = "Use o operador 'is'.",
            hintEn = "Use the 'is' type check operator.",
            keywords = listOf("obj is String", "obj.length"),
            explanationPt = "O compilador Kotlin aplica smart cast automático após checagens 'is'.",
            explanationEn = "Kotlin performs automatic smart casting immediately after 'is' type checks."
        )
    )

    private val intermediateSeeds = listOf(
        KtSeed(
            titlePt = "Funções de Extensão Elegantes",
            titleEn = "Extension Functions",
            instructionsPt = "Crie uma extension function em String chamada 'duplicar' retornando this + this.",
            instructionsEn = "Define an extension function on String named 'duplicar' returning this + this.",
            starterCode = "fun String.___(): String = this + this\nprintln(\"Kotlin\".duplicar())",
            solutionCode = "fun String.duplicar(): String = this + this\nprintln(\"Kotlin\".duplicar())",
            hintPt = "Nomeie o método após 'String.' como duplicar.",
            hintEn = "Name the extension method duplicar.",
            keywords = listOf("fun String.duplicar()", "this + this"),
            explanationPt = "Extension functions adicionam comportamento a tipos existentes sem herança.",
            explanationEn = "Extension functions augment existing classes statically without subclassing."
        ),
        KtSeed(
            titlePt = "Função de Escopo 'let' para Não-Nulos",
            titleEn = "Scope Function 'let' for Non-Null Execution",
            instructionsPt = "Execute o bloco seguro apenas se email não for nulo usando ?.let { ... }.",
            instructionsEn = "Execute the block only when email is non-null using ?.let { ... }.",
            starterCode = "val email: String? = \"dev@kotlin.org\"\nemail?.___ { println(\"Enviando para \$it\") }",
            solutionCode = "val email: String? = \"dev@kotlin.org\"\nemail?.let { println(\"Enviando para \$it\") }",
            hintPt = "Use a função de escopo 'let'.",
            hintEn = "Use the 'let' scope function.",
            keywords = listOf("?.let", "it", "println"),
            explanationPt = "?.let garante escopo não-anulável com acesso conciso via 'it'.",
            explanationEn = "?.let executes safely on non-null receivers passing the value as 'it'."
        ),
        KtSeed(
            titlePt = "Configuração de Objetos com 'apply'",
            titleEn = "Object Configuration with 'apply'",
            instructionsPt = "Configure o StringBuilder usando 'apply' acessando o receptor implícito this.",
            instructionsEn = "Configure StringBuilder via 'apply' using implicit receiver 'this'.",
            starterCode = "val sb = StringBuilder().___ {\n    append(\"Compose \")\n    append(\"Android\")\n}",
            solutionCode = "val sb = StringBuilder().apply {\n    append(\"Compose \")\n    append(\"Android\")\n}",
            hintPt = "Use a função de escopo 'apply'.",
            hintEn = "Use the 'apply' scope function.",
            keywords = listOf("StringBuilder().apply", "append"),
            explanationPt = "'apply' retorna o próprio objeto receptor configurado, ótimo para builders.",
            explanationEn = "'apply' returns the configured receiver itself, ideal for initialization blocks."
        ),
        KtSeed(
            titlePt = "Operações Funcionais: filter e map",
            titleEn = "Functional Pipelines: filter & map",
            instructionsPt = "Filtre pares (it % 2 == 0) e multiplique cada um por 10 com map.",
            instructionsEn = "Filter even numbers and multiply each by 10 using map.",
            starterCode = "val nums = listOf(1, 2, 3, 4)\nval res = nums.filter { it % 2 == 0 }.___ { it * 10 }",
            solutionCode = "val nums = listOf(1, 2, 3, 4)\nval res = nums.filter { it % 2 == 0 }.map { it * 10 }",
            hintPt = "Use .map { it * 10 }.",
            hintEn = "Use .map { it * 10 }.",
            keywords = listOf("filter", "map", "it * 10"),
            explanationPt = "Pipelines declarativos transformam coleções com alta legibilidade.",
            explanationEn = "Declarative collection pipelines replace verbose manual loops."
        ),
        KtSeed(
            titlePt = "Agrupamento de Dados com 'groupBy'",
            titleEn = "Data Grouping with 'groupBy'",
            instructionsPt = "Agrupe a lista de linguagens pela inicial usando groupBy { it.first() }.",
            instructionsEn = "Group languages by their first character via groupBy { it.first() }.",
            starterCode = "val langs = listOf(\"Kotlin\", \"Java\", \"Python\")\nval mapa = langs.___ { it.first() }",
            solutionCode = "val langs = listOf(\"Kotlin\", \"Java\", \"Python\")\nval mapa = langs.groupBy { it.first() }",
            hintPt = "Use a função groupBy.",
            hintEn = "Use groupBy.",
            keywords = listOf("groupBy", "it.first()"),
            explanationPt = "groupBy particiona listas em um Map<K, List<T>> em uma única chamada.",
            explanationEn = "groupBy partitions elements into a Map<K, List<T>> keyed by selector."
        ),
        KtSeed(
            titlePt = "Desestruturação de Data Classes e Pares",
            titleEn = "Destructuring Declarations",
            instructionsPt = "Extraia nome e pontos diretamente da instância de Jogador em duas variáveis.",
            instructionsEn = "Unpack nome and pontos directly from Jogador via destructuring declaration.",
            starterCode = "data class Jogador(val nome: String, val pontos: Int)\nval (___, ___) = Jogador(\"Lucas\", 950)",
            solutionCode = "data class Jogador(val nome: String, val pontos: Int)\nval (nome, pontos) = Jogador(\"Lucas\", 950)",
            hintPt = "Declare val (nome, pontos).",
            hintEn = "Declare val (nome, pontos).",
            keywords = listOf("val (nome, pontos)", "Jogador"),
            explanationPt = "Data classes geram component1(), component2() permitindo desestruturação limpa.",
            explanationEn = "Data classes generate componentN operators supporting clean destructuring."
        ),
        KtSeed(
            titlePt = "Lazy Initialization com 'by lazy'",
            titleEn = "Lazy Initialization with 'by lazy'",
            instructionsPt = "Adie o cálculo pesado da propriedade config usando o delegate 'by lazy'.",
            instructionsEn = "Defer heavy initialization of config using the 'by lazy' property delegate.",
            starterCode = "val config: String ___ lazy {\n    \"Configuração Carregada\"\n}",
            solutionCode = "val config: String by lazy {\n    \"Configuração Carregada\"\n}",
            hintPt = "Use a palavra-chave 'by' antes de lazy.",
            hintEn = "Use delegate keyword 'by' preceding lazy.",
            keywords = listOf("by lazy", "config"),
            explanationPt = "'by lazy' inicializa a propriedade apenas no primeiro acesso de forma thread-safe.",
            explanationEn = "'by lazy' computes the value once on first access in a thread-safe manner."
        ),
        KtSeed(
            titlePt = "Companion Object para Membros de Classe",
            titleEn = "Companion Object Factory Methods",
            instructionsPt = "Crie um companion object na classe Usuario contendo a factory criarAdmin().",
            instructionsEn = "Add a companion object inside Usuario holding factory criarAdmin().",
            starterCode = "class Usuario(val role: String) {\n    ___ object {\n        fun criarAdmin() = Usuario(\"ADMIN\")\n    }\n}",
            solutionCode = "class Usuario(val role: String) {\n    companion object {\n        fun criarAdmin() = Usuario(\"ADMIN\")\n    }\n}",
            hintPt = "Use o modificador 'companion'.",
            hintEn = "Use the 'companion' keyword.",
            keywords = listOf("companion object", "criarAdmin"),
            explanationPt = "Companion objects substituem membros static oferecendo suporte a interfaces.",
            explanationEn = "Companion objects replace static members and can implement interfaces."
        ),
        KtSeed(
            titlePt = "Singletons Seguros com 'object'",
            titleEn = "Thread-Safe Singletons with 'object'",
            instructionsPt = "Declare um singleton chamado AppConfig usando a declaração 'object'.",
            instructionsEn = "Declare a thread-safe singleton AppConfig using the 'object' keyword.",
            starterCode = "___ AppConfig {\n    const val VERSION = \"1.0.0\"\n}",
            solutionCode = "object AppConfig {\n    const val VERSION = \"1.0.0\"\n}",
            hintPt = "Substitua 'class' por 'object'.",
            hintEn = "Use the 'object' declaration.",
            keywords = listOf("object AppConfig", "const val"),
            explanationPt = "'object' cria um singleton thread-safe garantido nativamente pela linguagem.",
            explanationEn = "Kotlin's 'object' declaration provides a concise, thread-safe singleton."
        ),
        KtSeed(
            titlePt = "Sequências Preguiçosas (asSequence) para Grandes Coleções",
            titleEn = "Lazy Sequences with asSequence()",
            instructionsPt = "Converta a lista em Sequence usando .asSequence() antes do map e filter.",
            instructionsEn = "Convert the list into a lazy Sequence using .asSequence() prior to map and filter.",
            starterCode = "val grandeLista = (1..1000).toList()\nval primeiro = grandeLista.___().map { it * 2 }.first { it > 10 }",
            solutionCode = "val grandeLista = (1..1000).toList()\nval primeiro = grandeLista.asSequence().map { it * 2 }.first { it > 10 }",
            hintPt = "Chame .asSequence().",
            hintEn = "Invoke .asSequence().",
            keywords = listOf("asSequence()", "map", "first"),
            explanationPt = "Sequences processam elementos um a um sob demanda, evitando listas intermediárias.",
            explanationEn = "Sequences evaluate operations lazily per item without intermediate collection allocations."
        )
    )

    private val advancedSeeds = listOf(
        KtSeed(
            titlePt = "Sealed Interfaces para Estado UI",
            titleEn = "Sealed Interfaces for UI State Modeling",
            instructionsPt = "Declare uma 'sealed interface UiState' com estados Loading, Success e Error.",
            instructionsEn = "Declare 'sealed interface UiState' with Loading, Success, and Error states.",
            starterCode = "___ interface UiState {\n    object Loading : UiState\n    data class Success(val dados: List<String>) : UiState\n    data class Error(val mensagem: String) : UiState\n}",
            solutionCode = "sealed interface UiState {\n    object Loading : UiState\n    data class Success(val dados: List<String>) : UiState\n    data class Error(val mensagem: String) : UiState\n}",
            hintPt = "Use o modificador 'sealed'.",
            hintEn = "Prepend modifier 'sealed'.",
            keywords = listOf("sealed interface UiState", "Loading", "Success"),
            explanationPt = "Sealed interfaces permitem pattern matching exaustivo no when sem ramo else.",
            explanationEn = "Sealed hierarchies enforce closed subtypes for exhaustive compiler-checked when expressions."
        ),
        KtSeed(
            titlePt = "Higher-Order Functions e Lambdas com Receiver",
            titleEn = "Higher-Order Functions & Lambdas with Receiver",
            instructionsPt = "Invoque 'lista.acao()' sobre a lista mutável recebendo lambda com receptor.",
            instructionsEn = "Invoke 'lista.acao()' against the mutable list receiver inside construirLista.",
            starterCode = "fun <T> construirLista(acao: MutableList<T>.() -> Unit): List<T> {\n    val lista = mutableListOf<T>()\n    lista.___()\n    return lista\n}",
            solutionCode = "fun <T> construirLista(acao: MutableList<T>.() -> Unit): List<T> {\n    val lista = mutableListOf<T>()\n    lista.acao()\n    return lista\n}",
            hintPt = "Invoque acao() sobre lista.",
            hintEn = "Call acao() on lista.",
            keywords = listOf("MutableList<T>.() -> Unit", "lista.acao()"),
            explanationPt = "Lambdas com receptor são a base das DSLs declarativas como Jetpack Compose.",
            explanationEn = "Lambdas with receiver underpin expressive Kotlin DSLs like Jetpack Compose."
        ),
        KtSeed(
            titlePt = "Coroutines e Troca de Contexto com withContext",
            titleEn = "Coroutines & Context Switching with withContext",
            instructionsPt = "Alterne para Dispatchers.IO dentro da função suspensa usando withContext.",
            instructionsEn = "Switch to Dispatchers.IO inside the suspend function using withContext.",
            starterCode = "suspend fun carregarDados(): String = ___(Dispatchers.IO) {\n    \"Dados carregados\"\n}",
            solutionCode = "suspend fun carregarDados(): String = withContext(Dispatchers.IO) {\n    \"Dados carregados\"\n}",
            hintPt = "Use 'withContext'.",
            hintEn = "Use 'withContext'.",
            keywords = listOf("withContext", "Dispatchers.IO", "suspend fun"),
            explanationPt = "withContext suspende com segurança e delega I/O sem travar a Main Thread.",
            explanationEn = "withContext safely suspends execution on background threads without blocking UI."
        ),
        KtSeed(
            titlePt = "Concorrência com 'async' e 'await'",
            titleEn = "Concurrency with 'async' and 'await'",
            instructionsPt = "Aguarde os dois Deferred concorrentes chamando await() em ambos.",
            instructionsEn = "Await both concurrent Deferred tasks by calling await() on each.",
            starterCode = "suspend fun calcularTotal(): Int = coroutineScope {\n    val r1 = async { 100 }\n    val r2 = async { 250 }\n    r1.___() + r2.___()\n}",
            solutionCode = "suspend fun calcularTotal(): Int = coroutineScope {\n    val r1 = async { 100 }\n    val r2 = async { 250 }\n    r1.await() + r2.await()\n}",
            hintPt = "Chame await() em r1 e r2.",
            hintEn = "Call await() on r1 and r2.",
            keywords = listOf("async", "await", "coroutineScope"),
            explanationPt = "async executa blocos em paralelo dentro de coroutineScope retornando Deferred<T>.",
            explanationEn = "async launches concurrent tasks returning Deferred<T> handles resolved via await()."
        ),
        KtSeed(
            titlePt = "Fluxos Reativos com StateFlow",
            titleEn = "Reactive Streams with StateFlow",
            instructionsPt = "Exponha o StateFlow imutável com o método asStateFlow().",
            instructionsEn = "Expose read-only StateFlow using asStateFlow().",
            starterCode = "class MeuViewModel {\n    private val _pontos = MutableStateFlow(0)\n    val pontos: StateFlow<Int> = _pontos.___()\n}",
            solutionCode = "class MeuViewModel {\n    private val _pontos = MutableStateFlow(0)\n    val pontos: StateFlow<Int> = _pontos.asStateFlow()\n}",
            hintPt = "Use .asStateFlow().",
            hintEn = "Call .asStateFlow().",
            keywords = listOf("MutableStateFlow", "asStateFlow", "StateFlow"),
            explanationPt = "asStateFlow() protege a mutação interna expondo apenas leitura para a UI Compose.",
            explanationEn = "asStateFlow() encapsulates mutability exposing read-only state streams to UI."
        ),
        KtSeed(
            titlePt = "Inline Functions com Parâmetros Reified",
            titleEn = "Inline Functions with Reified Types",
            instructionsPt = "Use 'reified T' dentro da função inline para checar tipos em tempo de execução.",
            instructionsEn = "Use 'reified T' in the inline function to inspect type T at runtime.",
            starterCode = "inline fun <___ T> ehDoTipo(valor: Any): Boolean = valor is T\nprintln(ehDoTipo<String>(\"Compose\"))",
            solutionCode = "inline fun <reified T> ehDoTipo(valor: Any): Boolean = valor is T\nprintln(ehDoTipo<String>(\"Compose\"))",
            hintPt = "Adicione o modificador 'reified' antes de T.",
            hintEn = "Add modifier 'reified' before T.",
            keywords = listOf("inline fun", "reified T", "is T"),
            explanationPt = "reified evita type erasure na JVM ao combinar com funções inline.",
            explanationEn = "reified retains generic type arguments at runtime within inline functions."
        ),
        KtSeed(
            titlePt = "Fluxos Assíncronos com Flow e emit()",
            titleEn = "Asynchronous Streams with Flow & emit()",
            instructionsPt = "Emita os valores no construtor flow { } usando a função suspensa emit().",
            instructionsEn = "Emit values inside flow { } builder using the suspend emit() function.",
            starterCode = "fun contarFluxo(): Flow<Int> = flow {\n    for (i in 1..3) {\n        ___(i)\n    }\n}",
            solutionCode = "fun contarFluxo(): Flow<Int> = flow {\n    for (i in 1..3) {\n        emit(i)\n    }\n}",
            hintPt = "Use emit(i).",
            hintEn = "Use emit(i).",
            keywords = listOf("flow {", "emit(i)"),
            explanationPt = "Kotlin Flow representa fluxos frios assíncronos que emitem múltiplos valores sequenciais.",
            explanationEn = "Kotlin Flow models cold asynchronous streams emitting sequential items over time."
        ),
        KtSeed(
            titlePt = "Inline Value Classes para Zero Overhead",
            titleEn = "Inline Value Classes for Zero Overhead",
            instructionsPt = "Crie uma '@JvmInline value class UserId' envolvendo uma String sem custo de alocação extra.",
            instructionsEn = "Declare '@JvmInline value class UserId' wrapping a String without wrapper allocation.",
            starterCode = "@JvmInline\n___ class UserId(val id: String)",
            solutionCode = "@JvmInline\nvalue class UserId(val id: String)",
            hintPt = "Use o modificador 'value' antes de class.",
            hintEn = "Use the 'value' modifier preceding class.",
            keywords = listOf("@JvmInline", "value class UserId"),
            explanationPt = "Value classes trazem segurança de domínio forte mantendo performance primitiva em runtime.",
            explanationEn = "Value classes deliver domain type safety with zero heap wrapper allocation."
        ),
        KtSeed(
            titlePt = "Delegação de Interfaces com 'by'",
            titleEn = "Interface Delegation with 'by'",
            instructionsPt = "Delegue a implementação de Logger para o parâmetro 'impl' usando 'by impl'.",
            instructionsEn = "Delegate Logger interface implementation to parameter 'impl' using 'by impl'.",
            starterCode = "interface Logger { fun log(msg: String) }\nclass Servico(impl: Logger) : Logger ___ impl",
            solutionCode = "interface Logger { fun log(msg: String) }\nclass Servico(impl: Logger) : Logger by impl",
            hintPt = "Use a palavra-chave 'by'.",
            hintEn = "Use delegation keyword 'by'.",
            keywords = listOf(": Logger by impl", "interface Logger"),
            explanationPt = "Delegação nativa 'by' implementa o padrão Decorator sem boilerplate manual.",
            explanationEn = "Native 'by' delegation realizes composition over inheritance without forwarding boilerplate."
        ),
        KtSeed(
            titlePt = "Operador Coroutine Channel para Produtor/Consumidor",
            titleEn = "Channels for Producer-Consumer Coroutines",
            instructionsPt = "Envie uma mensagem pelo Channel usando channel.send() dentro da coroutine.",
            instructionsEn = "Send an item through the Channel using channel.send() inside a coroutine.",
            starterCode = "val canal = Channel<String>()\nsuspend fun produzir() {\n    canal.___(\"Evento pronto\")\n}",
            solutionCode = "val canal = Channel<String>()\nsuspend fun produzir() {\n    canal.send(\"Evento pronto\")\n}",
            hintPt = "Use o método suspenso send().",
            hintEn = "Invoke suspend method send().",
            keywords = listOf("Channel<String>", "canal.send"),
            explanationPt = "Channels permitem comunicação segura e não-bloqueante entre coroutines concorrentes.",
            explanationEn = "Channels provide non-blocking message passing between concurrent coroutines."
        )
    )

    val list: List<PracticalActivity> by lazy {
        val beginner = (1..35).map { idx ->
            val seed = beginnerSeeds[(idx - 1) % beginnerSeeds.size]
            val suffix = if (idx <= beginnerSeeds.size) "" else " ($idx)"
            PracticalActivity(
                id = "kt_act_$idx",
                tech = Technology.KOTLIN,
                titlePt = "${seed.titlePt}$suffix",
                titleEn = "${seed.titleEn}$suffix",
                difficulty = ActivityDifficulty.BEGINNER,
                pointsValue = 30,
                instructionsPt = seed.instructionsPt,
                instructionsEn = seed.instructionsEn,
                starterCode = seed.starterCode,
                solutionCode = seed.solutionCode,
                hintsPt = listOf(seed.hintPt),
                hintsEn = listOf(seed.hintEn),
                requiredKeywords = seed.keywords,
                explanationPt = seed.explanationPt,
                explanationEn = seed.explanationEn
            )
        }
        val intermediate = (36..70).map { idx ->
            val seed = intermediateSeeds[(idx - 36) % intermediateSeeds.size]
            val suffix = if (idx - 35 <= intermediateSeeds.size) "" else " ($idx)"
            PracticalActivity(
                id = "kt_act_$idx",
                tech = Technology.KOTLIN,
                titlePt = "${seed.titlePt}$suffix",
                titleEn = "${seed.titleEn}$suffix",
                difficulty = ActivityDifficulty.INTERMEDIATE,
                pointsValue = 60,
                instructionsPt = seed.instructionsPt,
                instructionsEn = seed.instructionsEn,
                starterCode = seed.starterCode,
                solutionCode = seed.solutionCode,
                hintsPt = listOf(seed.hintPt),
                hintsEn = listOf(seed.hintEn),
                requiredKeywords = seed.keywords,
                explanationPt = seed.explanationPt,
                explanationEn = seed.explanationEn
            )
        }
        val advanced = (71..105).map { idx ->
            val seed = advancedSeeds[(idx - 71) % advancedSeeds.size]
            val suffix = if (idx - 70 <= advancedSeeds.size) "" else " ($idx)"
            PracticalActivity(
                id = "kt_act_$idx",
                tech = Technology.KOTLIN,
                titlePt = "${seed.titlePt}$suffix",
                titleEn = "${seed.titleEn}$suffix",
                difficulty = ActivityDifficulty.ADVANCED,
                pointsValue = 90,
                instructionsPt = seed.instructionsPt,
                instructionsEn = seed.instructionsEn,
                starterCode = seed.starterCode,
                solutionCode = seed.solutionCode,
                hintsPt = listOf(seed.hintPt),
                hintsEn = listOf(seed.hintEn),
                requiredKeywords = seed.keywords,
                explanationPt = seed.explanationPt,
                explanationEn = seed.explanationEn
            )
        }
        beginner + intermediate + advanced
    }
}
