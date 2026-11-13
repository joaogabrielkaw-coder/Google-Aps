package com.example.model

data class AiSuggestedTopic(
    val id: String,
    val tech: Technology,
    val titlePt: String,
    val titleEn: String,
    val explanationPt: String,
    val explanationEn: String,
    val difficultyPt: String,
    val difficultyEn: String,
    val estimatedMinutes: Int,
    val xpReward: Int,
    val tags: List<String> = emptyList()
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun explanation(language: Language): String = if (language == Language.PT) explanationPt else explanationEn
    fun difficulty(language: Language): String = if (language == Language.PT) difficultyPt else difficultyEn
}

object AiStudyTopicsCatalog {

    val DEFAULT_SUGGESTIONS = listOf(
        AiSuggestedTopic(
            id = "topic_py_list_comprehension",
            tech = Technology.PYTHON,
            titlePt = "List Comprehensions & Filtragem Funcional",
            titleEn = "List Comprehensions & Functional Filters",
            explanationPt = "Sintaxe concisa e pythônica para transformar e filtrar listas em uma única linha de alta performance.",
            explanationEn = "Concise and idiomatic Python syntax to transform and filter lists in a single high-performance line.",
            difficultyPt = "Intermediário",
            difficultyEn = "Intermediate",
            estimatedMinutes = 20,
            xpReward = 35,
            tags = listOf("Python", "Sintaxe Limpa", "Coleções")
        ),
        AiSuggestedTopic(
            id = "topic_sql_joins_group_by",
            tech = Technology.SQL,
            titlePt = "INNER & LEFT JOIN com GROUP BY e HAVING",
            titleEn = "INNER & LEFT JOIN with GROUP BY and HAVING",
            explanationPt = "Relacione múltiplas tabelas relacionais e aplique filtros agregados essenciais para relatórios analíticos.",
            explanationEn = "Join multiple relational tables and apply aggregate filtering essential for analytical reports.",
            difficultyPt = "Intermediário",
            difficultyEn = "Intermediate",
            estimatedMinutes = 25,
            xpReward = 40,
            tags = listOf("SQL", "Agregações", "Bancos Relacionais")
        ),
        AiSuggestedTopic(
            id = "topic_java_streams_lambda",
            tech = Technology.JAVA,
            titlePt = "Streams API & Expressões Lambda Modernas",
            titleEn = "Streams API & Modern Lambda Expressions",
            explanationPt = "Processe fluxos de dados de coleções com filter(), map() e collect() sem laços for imperativos tradicionais.",
            explanationEn = "Process collection data pipelines using filter(), map() and collect() without traditional loops.",
            difficultyPt = "Avançado",
            difficultyEn = "Advanced",
            estimatedMinutes = 25,
            xpReward = 45,
            tags = listOf("Java", "Streams", "Backend")
        ),
        AiSuggestedTopic(
            id = "topic_html_semantic_forms",
            tech = Technology.HTML,
            titlePt = "Formulários Semânticos, Validação & Acessibilidade",
            titleEn = "Semantic Forms, Validation & Accessibility",
            explanationPt = "Crie campos de formulário acessíveis com tags semânticas, atributos pattern, required e roles ARIA.",
            explanationEn = "Build accessible form controls with semantic markup, pattern constraints, and ARIA roles.",
            difficultyPt = "Iniciante",
            difficultyEn = "Beginner",
            estimatedMinutes = 15,
            xpReward = 25,
            tags = listOf("HTML5", "Acessibilidade", "Frontend")
        ),
        AiSuggestedTopic(
            id = "topic_css_flexbox_grid",
            tech = Technology.CSS,
            titlePt = "Layouts Responsivos com CSS Grid e Flexbox",
            titleEn = "Responsive Layouts with CSS Grid and Flexbox",
            explanationPt = "Alinhe elementos em 1D e 2D semânticos com facilidade, dominando gap, justify-content e media queries.",
            explanationEn = "Align 1D and 2D elements effortlessly, mastering gap, justify-content, and media queries.",
            difficultyPt = "Intermediário",
            difficultyEn = "Intermediate",
            estimatedMinutes = 20,
            xpReward = 30,
            tags = listOf("CSS3", "Design Responsivo", "UI")
        ),
        AiSuggestedTopic(
            id = "topic_mysql_indexes_explain",
            tech = Technology.MYSQL,
            titlePt = "Índices B-Tree & Análise de Consultas com EXPLAIN",
            titleEn = "B-Tree Indexes & Query Analysis with EXPLAIN",
            explanationPt = "Descubra como o otimizador do MySQL planeja consultas e evite varreduras completas de tabela (Full Table Scans).",
            explanationEn = "Learn how MySQL optimizer plans queries and prevent costly full table scans.",
            difficultyPt = "Avançado",
            difficultyEn = "Advanced",
            estimatedMinutes = 30,
            xpReward = 50,
            tags = listOf("MySQL", "Otimização", "Indexação")
        ),
        AiSuggestedTopic(
            id = "topic_postgres_jsonb_indexing",
            tech = Technology.POSTGRESQL,
            titlePt = "Colunas JSONB, Operadores de Consulta & Índices GIN",
            titleEn = "JSONB Columns, Query Operators & GIN Indexes",
            explanationPt = "Armazene e consulte documentos JSON aninhados no PostgreSQL com a velocidade de tabelas relacionais.",
            explanationEn = "Store and query nested JSON documents in PostgreSQL with relational performance using GIN indexes.",
            difficultyPt = "Avançado",
            difficultyEn = "Advanced",
            estimatedMinutes = 30,
            xpReward = 50,
            tags = listOf("PostgreSQL", "JSONB", "Dados Híbridos")
        ),
        AiSuggestedTopic(
            id = "topic_mongo_aggregation_pipeline",
            tech = Technology.MONGODB,
            titlePt = "Aggregation Pipeline: \$match, \$group e \$project",
            titleEn = "Aggregation Pipeline: \$match, \$group & \$project",
            explanationPt = "Construa pipelines de transformação analítica de documentos em lote no MongoDB com alto desempenho.",
            explanationEn = "Build high-throughput document transformation analytics pipelines in MongoDB.",
            difficultyPt = "Avançado",
            difficultyEn = "Advanced",
            estimatedMinutes = 30,
            xpReward = 50,
            tags = listOf("MongoDB", "NoSQL", "Pipelines")
        ),
        AiSuggestedTopic(
            id = "topic_js_async_await_promises",
            tech = Technology.JAVASCRIPT,
            titlePt = "Promises Concorrentes, Async/Await & Event Loop",
            titleEn = "Concurrent Promises, Async/Await & Event Loop",
            explanationPt = "Domine o fluxo assíncrono moderno, Promise.all, microtasks e tratamento robusto de erros com try/catch.",
            explanationEn = "Master modern async workflows, Promise.all, microtasks, and resilient try/catch error handling.",
            difficultyPt = "Avançado",
            difficultyEn = "Advanced",
            estimatedMinutes = 25,
            xpReward = 45,
            tags = listOf("JavaScript", "Async", "Promises", "Fullstack")
        ),
        AiSuggestedTopic(
            id = "topic_ts_generics_utility_types",
            tech = Technology.TYPESCRIPT,
            titlePt = "Generics Avançados, Constraints & Utility Types",
            titleEn = "Advanced Generics, Constraints & Utility Types",
            explanationPt = "Crie funções e tipos parametrizados escaláveis usando Partial, Pick, Record e extends com tipagem estrita.",
            explanationEn = "Build scalable typed APIs using Partial, Pick, Record, and extends generic constraints.",
            difficultyPt = "Avançado",
            difficultyEn = "Advanced",
            estimatedMinutes = 30,
            xpReward = 50,
            tags = listOf("TypeScript", "Generics", "Type Safety")
        ),
        AiSuggestedTopic(
            id = "topic_kt_coroutines_stateflow",
            tech = Technology.KOTLIN,
            titlePt = "Coroutines, withContext(Dispatchers.IO) & StateFlow",
            titleEn = "Coroutines, withContext(Dispatchers.IO) & StateFlow",
            explanationPt = "Orquestre operações concorrentes em background thread e emita fluxos de estado imutáveis reativos para a UI.",
            explanationEn = "Orchestrate background concurrency on I/O threads and emit immutable reactive StateFlow streams.",
            difficultyPt = "Avançado",
            difficultyEn = "Advanced",
            estimatedMinutes = 25,
            xpReward = 50,
            tags = listOf("Kotlin", "Coroutines", "Android", "Jetpack Compose")
        ),
        AiSuggestedTopic(
            id = "topic_go_goroutines_channels",
            tech = Technology.GO,
            titlePt = "Goroutines, Channels, Select & Context",
            titleEn = "Goroutines, Channels, Select & Context",
            explanationPt = "Construa serviços concorrentes de alta escala em Go usando goroutines leves, channels tipados e timeouts.",
            explanationEn = "Build scalable concurrent backend services in Go using lightweight goroutines, channels, and context.",
            difficultyPt = "Avançado",
            difficultyEn = "Advanced",
            estimatedMinutes = 25,
            xpReward = 50,
            tags = listOf("Go", "Concurrency", "Cloud", "Backend")
        )
    )

    fun getSuggestionsForTech(tech: Technology): List<AiSuggestedTopic> {
        return DEFAULT_SUGGESTIONS.filter { it.tech == tech }
    }
}
