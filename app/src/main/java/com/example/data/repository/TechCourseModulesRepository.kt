package com.example.data.repository

import com.example.model.CourseLesson
import com.example.model.CourseModule
import com.example.model.CourseProgressSummary
import com.example.model.CourseReference
import com.example.model.CourseResourceType
import com.example.model.TechCourseCurriculum
import com.example.model.Technology

/**
 * Repositório curado de Módulos de Aula Práticos e Referências Gratuitas
 * (YouTube & Cursos Livres Oficiais) para todas as 12 trilhas do app.
 */
object TechCourseModulesRepository {

    private val curriculumMap: Map<Technology, TechCourseCurriculum> by lazy {
        listOf(
            buildPythonCurriculum(),
            buildSqlCurriculum(),
            buildHtmlCurriculum(),
            buildCssCurriculum(),
            buildJavaScriptCurriculum(),
            buildTypeScriptCurriculum(),
            TechCourseModulesDataPart2.buildJavaCurriculum(),
            TechCourseModulesDataPart2.buildKotlinCurriculum(),
            TechCourseModulesDataPart2.buildGoCurriculum(),
            TechCourseModulesDataPart2.buildMySqlCurriculum(),
            TechCourseModulesDataPart2.buildPostgresCurriculum(),
            TechCourseModulesDataPart2.buildMongoCurriculum()
        ).associateBy { it.tech }
    }

    fun getCurriculum(tech: Technology): TechCourseCurriculum =
        curriculumMap[tech] ?: buildPythonCurriculum()

    fun getModules(tech: Technology): List<CourseModule> = getCurriculum(tech).modules

    fun getReferences(tech: Technology): List<CourseReference> = getCurriculum(tech).references

    fun getAllLessonIds(tech: Technology): Set<String> =
        getModules(tech).flatMap { it.lessons }.map { it.id }.toSet()

    fun getCourseProgress(
        tech: Technology,
        completedLessonIds: Set<String>
    ): CourseProgressSummary {
        val lessons = getModules(tech).flatMap { it.lessons }
        val total = lessons.size
        val completed = lessons.count { completedLessonIds.contains(it.id) }
        val totalMins = lessons.sumOf { it.durationMinutes }
        val completedMins = lessons.filter { completedLessonIds.contains(it.id) }.sumOf { it.durationMinutes }
        val fraction = if (total > 0) (completed.toFloat() / total).coerceIn(0f, 1f) else 0f
        val percent = (fraction * 100f).toInt().coerceIn(0, 100)
        return CourseProgressSummary(
            totalLessons = total,
            completedLessons = completed,
            remainingLessons = (total - completed).coerceAtLeast(0),
            totalMinutes = totalMins,
            completedMinutes = completedMins,
            progressFraction = fraction,
            progressPercent = percent
        )
    }

    private fun buildPythonCurriculum() = TechCourseCurriculum(
        tech = Technology.PYTHON,
        references = listOf(
            CourseReference(
                id = "py_ref_cev_site",
                titlePt = "Curso em Vídeo — Portal Oficial Python 3 (Mundos 1, 2 e 3)",
                titleEn = "Curso em Vídeo — Official Portal Python 3 (Worlds 1–3)",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "3 Mundos • Grátis",
                descriptionPt = "Site oficial do Curso em Vídeo com todas as aulas gratuitas do Prof. Gustavo Guanabara organizadas em módulos.",
                descriptionEn = "Official Curso em Vídeo platform with Gustavo Guanabara's complete free modular Python 3 tracks."
            ),
            CourseReference(
                id = "py_ref_cev_m1",
                titlePt = "Curso em Vídeo — Python 3 Mundo 1 (Fundamentos)",
                titleEn = "Curso em Vídeo — Python 3 World 1 (Fundamentals)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlKP6QQCekuIPky1CiwmdI6",
                durationLabel = "43 vídeos",
                descriptionPt = "Início completo do zero: variáveis, tipos primitivos, operadores, strings e condições em português.",
                descriptionEn = "Beginner Portuguese playlist covering variables, types, string manipulation, and conditionals."
            ),
            CourseReference(
                id = "py_ref_cev_m2",
                titlePt = "Curso em Vídeo — Python 3 Mundo 2 (Estruturas de Controle)",
                titleEn = "Curso em Vídeo — Python 3 World 2 (Control Structures)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dk_nZHmxxf_J0WRAqy5Czye",
                durationLabel = "44 vídeos",
                descriptionPt = "Playlist do Mundo 2 com condições aninhadas, laços for, while, break e exercícios práticos.",
                descriptionEn = "World 2 playlist covering nested conditionals, for/while loops, break, and guided exercises."
            ),
            CourseReference(
                id = "py_ref_cev_m3",
                titlePt = "Curso em Vídeo — Python 3 Mundo 3 (Estruturas Compostas)",
                titleEn = "Curso em Vídeo — Python 3 World 3 (Data Structures)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dksnH2jVTIVNviIMBVYyFnH",
                durationLabel = "50+ vídeos",
                descriptionPt = "Tuplas, listas, dicionários, funções, modularização e tratamento de erros com o Guanabara.",
                descriptionEn = "Tuples, lists, dictionaries, functions, modules, and error handling by Gustavo Guanabara."
            ),
            CourseReference(
                id = "py_ref_fcc",
                titlePt = "freeCodeCamp — Python para Iniciantes (4h26)",
                titleEn = "freeCodeCamp — Learn Python Full Course (4h26)",
                provider = "YouTube • freeCodeCamp.org",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=rfscVS0vtbw",
                durationLabel = "4h 26m",
                descriptionPt = "Aula única completa em inglês com projetos práticos, funções, classes, arquivos e módulos.",
                descriptionEn = "Comprehensive full video course covering variables, loops, functions, files, and OOP in Python."
            ),
            CourseReference(
                id = "py_ref_docs",
                titlePt = "Tutorial Oficial do Python 3 (em Português)",
                titleEn = "Official Python 3 Tutorial (PT-BR / EN)",
                provider = "docs.python.org",
                isPortuguese = true,
                type = CourseResourceType.OFFICIAL_DOCS,
                url = "https://docs.python.org/pt-br/3/tutorial/",
                durationLabel = "Guia Interativo",
                descriptionPt = "Documentação oficial traduzida pela comunidade Python Brasil para consulta rápida e aprofundamento.",
                descriptionEn = "Official Python documentation tutorial with canonical syntax references and standard library examples."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "py_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Fundamentos e Lógica (Mundo 1)",
                titleEn = "Module 1: Fundamentals & Logic",
                objectivePt = "Dominar variáveis, tipos primitivos, operadores e entrada/saída em Python.",
                objectiveEn = "Master variables, primitive types, operators, and I/O in Python.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlKP6QQCekuIPky1CiwmdI6",
                primaryVideoTitlePt = "Assistir Mundo 1 no YouTube",
                primaryVideoTitleEn = "Watch World 1 on YouTube",
                lessons = listOf(
                    CourseLesson(
                        id = "py_m1_l1",
                        lessonNumber = 1,
                        titlePt = "Variáveis, Tipos Primitivos e f-strings",
                        titleEn = "Variables, Primitive Types & f-strings",
                        summaryPt = "Declare int, float, str e bool e formate saídas limpas com f\"...\".",
                        summaryEn = "Declare int, float, str, and bool and format clean outputs with f-strings.",
                        durationMinutes = 25,
                        topicsPt = listOf("input() e print()", "Conversão int()/float()", "Formatação com f-strings"),
                        topicsEn = listOf("input() & print()", "Type casting int()/float()", "f-string formatting"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlKP6QQCekuIPky1CiwmdI6",
                        videoRefLabelPt = "Playlist Mundo 1 (Curso em Vídeo)",
                        videoRefLabelEn = "World 1 Playlist (Curso em Vídeo)"
                    ),
                    CourseLesson(
                        id = "py_m1_l2",
                        lessonNumber = 2,
                        titlePt = "Operadores Aritméticos, Lógicos e Condições",
                        titleEn = "Arithmetic, Logical Operators & Conditionals",
                        summaryPt = "Construa decisões com if, elif, else e expressões booleanas combinadas.",
                        summaryEn = "Build decision branches with if, elif, else, and compound boolean expressions.",
                        durationMinutes = 30,
                        topicsPt = listOf("if / elif / else", "Operadores and, or, not", "Validação de faixas numéricas"),
                        topicsEn = listOf("if / elif / else", "and, or, not operators", "Range validation"),
                        videoRefUrl = "https://www.youtube.com/watch?v=rfscVS0vtbw",
                        videoRefLabelPt = "freeCodeCamp Python Completo",
                        videoRefLabelEn = "freeCodeCamp Python Full Course"
                    ),
                    CourseLesson(
                        id = "py_m1_l3",
                        lessonNumber = 3,
                        titlePt = "Manipulação de Strings e Fatiamento",
                        titleEn = "String Manipulation & Slicing",
                        summaryPt = "Use slicing texto[0:5], strip(), split(), upper() e replace() em textos reais.",
                        summaryEn = "Apply slicing, strip(), split(), upper(), and replace() on real text inputs.",
                        durationMinutes = 25,
                        topicsPt = listOf("Fatiamento [inicio:fim]", "strip() e split()", "Contagem e busca em strings"),
                        topicsEn = listOf("Slicing [start:end]", "strip() & split()", "Searching substrings"),
                        videoRefUrl = "https://docs.python.org/pt-br/3/tutorial/",
                        videoRefLabelPt = "Tutorial Oficial Python.org",
                        videoRefLabelEn = "Official Python.org Tutorial"
                    )
                )
            ),
            CourseModule(
                id = "py_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Repetições e Estruturas (Mundo 2)",
                titleEn = "Module 2: Loops & Control Structures",
                objectivePt = "Automatizar tarefas com laços for, while, break e contadores acumulados.",
                objectiveEn = "Automate tasks with for loops, while loops, break, and accumulators.",
                levelLabelPt = "Intermediário",
                levelLabelEn = "Intermediate",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dk_nZHmxxf_J0WRAqy5Czye",
                primaryVideoTitlePt = "Assistir Mundo 2 no YouTube",
                primaryVideoTitleEn = "Watch World 2 on YouTube",
                lessons = listOf(
                    CourseLesson(
                        id = "py_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Laços for com range() e enumerate()",
                        titleEn = "For Loops with range() & enumerate()",
                        summaryPt = "Percorra sequências numéricas e listas acessando índice e valor com clareza.",
                        summaryEn = "Iterate numeric ranges and sequences cleanly accessing index and value.",
                        durationMinutes = 30,
                        topicsPt = listOf("range(inicio, fim, passo)", "enumerate()", "Acumuladores e somatórios"),
                        topicsEn = listOf("range(start, stop, step)", "enumerate()", "Running totals"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dk_nZHmxxf_J0WRAqy5Czye",
                        videoRefLabelPt = "Playlist Mundo 2 (Curso em Vídeo)",
                        videoRefLabelEn = "World 2 Playlist (Curso em Vídeo)"
                    ),
                    CourseLesson(
                        id = "py_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Laços while, Flags e break",
                        titleEn = "While Loops, Flags & break",
                        summaryPt = "Crie menus interativos e validações contínuas até o usuário digitar a saída.",
                        summaryEn = "Build interactive loops and input validation until exit conditions are met.",
                        durationMinutes = 25,
                        topicsPt = listOf("while True + break", "Validação de entrada", "Prevenção de loop infinito"),
                        topicsEn = listOf("while True + break", "Input validation", "Avoiding infinite loops"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dk_nZHmxxf_J0WRAqy5Czye",
                        videoRefLabelPt = "Aulas de While (Mundo 2)",
                        videoRefLabelEn = "While Lessons (World 2)"
                    ),
                    CourseLesson(
                        id = "py_m2_l3",
                        lessonNumber = 3,
                        titlePt = "List Comprehensions e Filtros",
                        titleEn = "List Comprehensions & Filtering",
                        summaryPt = "Escreva transformações pythônicas concisas como [x*2 for x in nums if x > 0].",
                        summaryEn = "Write concise Pythonic transforms such as [x*2 for x in nums if x > 0].",
                        durationMinutes = 30,
                        topicsPt = listOf("Compreensão de listas", "Filtros inline com if", "Comparação com laço tradicional"),
                        topicsEn = listOf("List comprehensions", "Inline if filtering", "Comparing with standard loops"),
                        videoRefUrl = "https://docs.python.org/pt-br/3/tutorial/",
                        videoRefLabelPt = "Guia de Estruturas Python.org",
                        videoRefLabelEn = "Python.org Data Structures"
                    )
                )
            ),
            CourseModule(
                id = "py_mod_3",
                moduleNumber = 3,
                titlePt = "Módulo 3: Coleções, Funções e POO (Mundo 3)",
                titleEn = "Module 3: Collections, Functions & OOP",
                objectivePt = "Organizar código reutilizável com listas, dicionários, funções def e classes.",
                objectiveEn = "Organize reusable code with lists, dicts, functions, and classes.",
                levelLabelPt = "Prático",
                levelLabelEn = "Practical",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dksnH2jVTIVNviIMBVYyFnH",
                primaryVideoTitlePt = "Assistir Mundo 3 no YouTube",
                primaryVideoTitleEn = "Watch World 3 on YouTube",
                lessons = listOf(
                    CourseLesson(
                        id = "py_m3_l1",
                        lessonNumber = 1,
                        titlePt = "Tuplas, Listas e Dicionários na Prática",
                        titleEn = "Tuples, Lists & Dictionaries in Practice",
                        summaryPt = "Modele cadastros estruturados combinando listas de dicionários e ordenação.",
                        summaryEn = "Model structured records combining lists of dictionaries and sorting.",
                        durationMinutes = 35,
                        topicsPt = listOf("append(), pop(), sort()", "dict.get() e items()", "Listas aninhadas"),
                        topicsEn = listOf("append(), pop(), sort()", "dict.get() & items()", "Nested collections"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dksnH2jVTIVNviIMBVYyFnH",
                        videoRefLabelPt = "Playlist Mundo 3 (Curso em Vídeo)",
                        videoRefLabelEn = "World 3 Playlist (Curso em Vídeo)"
                    ),
                    CourseLesson(
                        id = "py_m3_l2",
                        lessonNumber = 2,
                        titlePt = "Funções, Escopo e Tratamento de Erros",
                        titleEn = "Functions, Scope & Exception Handling",
                        summaryPt = "Crie funções com parâmetros padrão, *args e blocos try/except seguros.",
                        summaryEn = "Write functions with default parameters, *args, and safe try/except blocks.",
                        durationMinutes = 30,
                        topicsPt = listOf("def, return e docstrings", "try / except / finally", "Modularização de scripts"),
                        topicsEn = listOf("def, return & docstrings", "try / except / finally", "Script modularization"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dksnH2jVTIVNviIMBVYyFnH",
                        videoRefLabelPt = "Módulo Funções & Erros (Mundo 3)",
                        videoRefLabelEn = "Functions & Errors (World 3)"
                    ),
                    CourseLesson(
                        id = "py_m3_l3",
                        lessonNumber = 3,
                        titlePt = "Introdução a Classes, Objetos e Arquivos",
                        titleEn = "Classes, Objects & File Handling",
                        summaryPt = "Use class, __init__ e with open(...) para persistir dados em arquivos JSON/texto.",
                        summaryEn = "Use class, __init__, and with open(...) to persist data in JSON/text files.",
                        durationMinutes = 40,
                        topicsPt = listOf("class e __init__", "Métodos de instância", "Leitura/escrita com with open()"),
                        topicsEn = listOf("class & __init__", "Instance methods", "File I/O with open()"),
                        videoRefUrl = "https://www.youtube.com/watch?v=rfscVS0vtbw",
                        videoRefLabelPt = "Projeto Final OOP (freeCodeCamp)",
                        videoRefLabelEn = "Final OOP Project (freeCodeCamp)"
                    )
                )
            )
        )
    )

    private fun buildSqlCurriculum() = TechCourseCurriculum(
        tech = Technology.SQL,
        references = listOf(
            CourseReference(
                id = "sql_ref_cev",
                titlePt = "Curso em Vídeo — Banco de Dados SQL / MySQL (Guanabara)",
                titleEn = "Curso em Vídeo — Relational SQL & MySQL Course",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkBsANmRuSNnETnERnGRvAM",
                durationLabel = "16 aulas • PT-BR",
                descriptionPt = "Curso gratuito completo do Prof. Guanabara ensinando SELECT, WHERE, GROUP BY, modelagem relacional e JOINs.",
                descriptionEn = "Free Portuguese course by Gustavo Guanabara teaching SELECT, WHERE, GROUP BY, modeling, and JOINs."
            ),
            CourseReference(
                id = "sql_ref_cev_site",
                titlePt = "Curso em Vídeo — Portal de Cursos Gratuitos",
                titleEn = "Curso em Vídeo — Official Free Courses Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Portal Grátis",
                descriptionPt = "Acesse o site oficial do Curso em Vídeo para estudar Banco de Dados, Lógica e linguagens gratuitamente.",
                descriptionEn = "Visit the official Curso em Vídeo site for free structured Database and programming courses."
            ),
            CourseReference(
                id = "sql_ref_fcc",
                titlePt = "freeCodeCamp — Curso Completo de SQL (4h20)",
                titleEn = "freeCodeCamp — SQL Full Database Course (4h20)",
                provider = "YouTube • freeCodeCamp.org",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=HXV3zeQKqGY",
                durationLabel = "4h 20m",
                descriptionPt = "Curso completo do zero cobrindo modelagem relacional, SELECT, joins, subqueries e triggers.",
                descriptionEn = "Full SQL database course covering schema design, SELECT queries, joins, nested queries, and triggers."
            ),
            CourseReference(
                id = "sql_ref_sqlbolt",
                titlePt = "SQLBolt — Lições Interativas no Navegador",
                titleEn = "SQLBolt — Interactive Browser SQL Lessons",
                provider = "sqlbolt.com",
                isPortuguese = false,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://sqlbolt.com/",
                durationLabel = "18 lições",
                descriptionPt = "Site gratuito com exercícios interativos para testar SELECT, JOIN, GROUP BY e UPDATE em tempo real.",
                descriptionEn = "Free interactive website to write and test SELECT, JOIN, GROUP BY, and UPDATE queries live."
            ),
            CourseReference(
                id = "sql_ref_sqlzoo",
                titlePt = "SQLZoo — Desafios Práticos de Consultas",
                titleEn = "SQLZoo — Practical Query Challenges",
                provider = "sqlzoo.net",
                isPortuguese = false,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://sqlzoo.net/",
                durationLabel = "Exercícios",
                descriptionPt = "Plataforma aberta com bancos reais para treinar agregações, subconsultas e junções complexas.",
                descriptionEn = "Open practice portal with real datasets to train aggregations, subqueries, and multi-table joins."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "sql_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Consultas SELECT e Filtros",
                titleEn = "Module 1: SELECT Queries & Filtering",
                objectivePt = "Consultar colunas específicas, filtrar linhas e ordenar resultados.",
                objectiveEn = "Query specific columns, filter rows, and sort query outputs.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/watch?v=HXV3zeQKqGY",
                primaryVideoTitlePt = "Assistir Fundamentos de SQL",
                primaryVideoTitleEn = "Watch SQL Fundamentals",
                lessons = listOf(
                    CourseLesson(
                        id = "sql_m1_l1",
                        lessonNumber = 1,
                        titlePt = "SELECT, DISTINCT e Aliases (AS)",
                        titleEn = "SELECT, DISTINCT & Column Aliases",
                        summaryPt = "Extraia apenas os dados necessários sem sobrecarga usando projeção limpa.",
                        summaryEn = "Retrieve only required columns cleanly with projection and aliases.",
                        durationMinutes = 20,
                        topicsPt = listOf("SELECT col1, col2", "DISTINCT", "Renomeando com AS"),
                        topicsEn = listOf("SELECT col1, col2", "DISTINCT", "Aliasing with AS"),
                        videoRefUrl = "https://sqlbolt.com/",
                        videoRefLabelPt = "Praticar Lições 1–4 no SQLBolt",
                        videoRefLabelEn = "Practice Lessons 1–4 on SQLBolt"
                    ),
                    CourseLesson(
                        id = "sql_m1_l2",
                        lessonNumber = 2,
                        titlePt = "Filtros com WHERE, LIKE, IN e BETWEEN",
                        titleEn = "Filtering with WHERE, LIKE, IN & BETWEEN",
                        summaryPt = "Combine operadores lógicos e curingas % para buscas precisas em tabelas.",
                        summaryEn = "Combine logical operators and % wildcards for exact table filtering.",
                        durationMinutes = 25,
                        topicsPt = listOf("WHERE + AND/OR", "LIKE '%texto%'", "IN (...) e BETWEEN"),
                        topicsEn = listOf("WHERE + AND/OR", "LIKE wildcards", "IN (...) & BETWEEN"),
                        videoRefUrl = "https://www.youtube.com/watch?v=HXV3zeQKqGY",
                        videoRefLabelPt = "Aula de Filtros (freeCodeCamp)",
                        videoRefLabelEn = "Filtering Lesson (freeCodeCamp)"
                    ),
                    CourseLesson(
                        id = "sql_m1_l3",
                        lessonNumber = 3,
                        titlePt = "Ordenação ORDER BY e Paginação LIMIT",
                        titleEn = "Sorting with ORDER BY & Pagination LIMIT",
                        summaryPt = "Crie rankings Top-N ordenando por múltiplas colunas com ASC/DESC.",
                        summaryEn = "Build Top-N rankings sorting across multiple columns with ASC/DESC.",
                        durationMinutes = 20,
                        topicsPt = listOf("ORDER BY DESC", "LIMIT e OFFSET", "Tratamento de NULL"),
                        topicsEn = listOf("ORDER BY DESC", "LIMIT & OFFSET", "Handling NULL values"),
                        videoRefUrl = "https://sqlbolt.com/",
                        videoRefLabelPt = "Exercícios ORDER BY (SQLBolt)",
                        videoRefLabelEn = "ORDER BY Exercises (SQLBolt)"
                    )
                )
            ),
            CourseModule(
                id = "sql_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Agregações e Junções (JOIN)",
                titleEn = "Module 2: Aggregations & Joins",
                objectivePt = "Cruzar múltiplas tabelas relacionais e resumir métricas de negócio.",
                objectiveEn = "Combine relational tables and summarize business metrics.",
                levelLabelPt = "Intermediário",
                levelLabelEn = "Intermediate",
                primaryVideoUrl = "https://sqlzoo.net/",
                primaryVideoTitlePt = "Praticar JOINs no SQLZoo",
                primaryVideoTitleEn = "Practice JOINs on SQLZoo",
                lessons = listOf(
                    CourseLesson(
                        id = "sql_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Funções COUNT, SUM, AVG e GROUP BY",
                        titleEn = "COUNT, SUM, AVG & GROUP BY",
                        summaryPt = "Agrupe registros por categoria e filtre grupos agregados com HAVING.",
                        summaryEn = "Group records by category and filter aggregated buckets with HAVING.",
                        durationMinutes = 30,
                        topicsPt = listOf("COUNT(*) vs COUNT(col)", "GROUP BY", "HAVING vs WHERE"),
                        topicsEn = listOf("COUNT(*) vs COUNT(col)", "GROUP BY", "HAVING vs WHERE"),
                        videoRefUrl = "https://www.youtube.com/watch?v=HXV3zeQKqGY",
                        videoRefLabelPt = "Agregações SQL (freeCodeCamp)",
                        videoRefLabelEn = "SQL Aggregations (freeCodeCamp)"
                    ),
                    CourseLesson(
                        id = "sql_m2_l2",
                        lessonNumber = 2,
                        titlePt = "INNER JOIN, LEFT JOIN e Chaves Estrangeiras",
                        titleEn = "INNER JOIN, LEFT JOIN & Foreign Keys",
                        summaryPt = "Relacione pedidos, clientes e produtos entendendo registros órfãos no LEFT JOIN.",
                        summaryEn = "Connect orders, customers, and items while handling unmatched rows with LEFT JOIN.",
                        durationMinutes = 35,
                        topicsPt = listOf("INNER JOIN ON", "LEFT OUTER JOIN", "Chaves PK e FK"),
                        topicsEn = listOf("INNER JOIN ON", "LEFT OUTER JOIN", "PK & FK keys"),
                        videoRefUrl = "https://sqlzoo.net/",
                        videoRefLabelPt = "Desafios de JOIN (SQLZoo)",
                        videoRefLabelEn = "JOIN Challenges (SQLZoo)"
                    ),
                    CourseLesson(
                        id = "sql_m2_l3",
                        lessonNumber = 3,
                        titlePt = "Subqueries e CTEs (WITH)",
                        titleEn = "Subqueries & Common Table Expressions (WITH)",
                        summaryPt = "Quebre relatórios complexos em etapas legíveis usando cláusulas WITH.",
                        summaryEn = "Break complex analytical reports into readable steps with WITH CTEs.",
                        durationMinutes = 30,
                        topicsPt = listOf("Subquery no WHERE", "Cláusula WITH (CTE)", "EXISTS"),
                        topicsEn = listOf("WHERE subqueries", "WITH CTEs", "EXISTS operator"),
                        videoRefUrl = "https://sqlbolt.com/",
                        videoRefLabelPt = "Guia Avançado SQLBolt",
                        videoRefLabelEn = "Advanced SQLBolt Guide"
                    )
                )
            ),
            CourseModule(
                id = "sql_mod_3",
                moduleNumber = 3,
                titlePt = "Módulo 3: Modelagem DDL, DML e Transações",
                titleEn = "Module 3: Schema Design, DML & Transactions",
                objectivePt = "Criar tabelas seguras com constraints, índices e transações ACID.",
                objectiveEn = "Design safe schemas with constraints, indexes, and ACID transactions.",
                levelLabelPt = "Prático",
                levelLabelEn = "Practical",
                primaryVideoUrl = "https://www.youtube.com/watch?v=HXV3zeQKqGY",
                primaryVideoTitlePt = "Assistir Modelagem SQL",
                primaryVideoTitleEn = "Watch SQL Schema Design",
                lessons = listOf(
                    CourseLesson(
                        id = "sql_m3_l1",
                        lessonNumber = 1,
                        titlePt = "CREATE TABLE e Constraints (PK, FK, UNIQUE)",
                        titleEn = "CREATE TABLE & Constraints",
                        summaryPt = "Garanta integridade estrutural com NOT NULL, CHECK, DEFAULT e FOREIGN KEY.",
                        summaryEn = "Enforce structural integrity with NOT NULL, CHECK, DEFAULT, and FOREIGN KEY.",
                        durationMinutes = 25,
                        topicsPt = listOf("PRIMARY KEY", "FOREIGN KEY ON DELETE", "UNIQUE e CHECK"),
                        topicsEn = listOf("PRIMARY KEY", "FOREIGN KEY ON DELETE", "UNIQUE & CHECK"),
                        videoRefUrl = "https://www.youtube.com/watch?v=HXV3zeQKqGY",
                        videoRefLabelPt = "Aula de Tabelas (freeCodeCamp)",
                        videoRefLabelEn = "Tables Lesson (freeCodeCamp)"
                    ),
                    CourseLesson(
                        id = "sql_m3_l2",
                        lessonNumber = 2,
                        titlePt = "INSERT, UPDATE e DELETE Seguros",
                        titleEn = "Safe INSERT, UPDATE & DELETE",
                        summaryPt = "Altere registros com segurança testando o filtro WHERE antes de executar.",
                        summaryEn = "Modify records safely by verifying WHERE conditions before mutations.",
                        durationMinutes = 25,
                        topicsPt = listOf("INSERT INTO múltiplo", "UPDATE com WHERE", "BEGIN / COMMIT / ROLLBACK"),
                        topicsEn = listOf("Multi-row INSERT", "Filtered UPDATE", "Transactions"),
                        videoRefUrl = "https://sqlbolt.com/",
                        videoRefLabelPt = "Lições DML no SQLBolt",
                        videoRefLabelEn = "DML Lessons on SQLBolt"
                    )
                )
            )
        )
    )

    private fun buildHtmlCurriculum() = TechCourseCurriculum(
        tech = Technology.HTML,
        references = listOf(
            CourseReference(
                id = "html_ref_cev",
                titlePt = "Curso em Vídeo — HTML5 e CSS3 Módulo 1 (Guanabara)",
                titleEn = "Curso em Vídeo — HTML5 & CSS3 Module 1",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkZ9-atkcmcBaMZdmLHft8n",
                durationLabel = "41 aulas",
                descriptionPt = "Melhor curso livre introdutório em português sobre estrutura HTML5, semântica, links e mídias.",
                descriptionEn = "Top free Portuguese playlist covering HTML5 document structure, semantic tags, links, and media."
            ),
            CourseReference(
                id = "html_ref_cev_m3",
                titlePt = "Curso em Vídeo — HTML5 & CSS3 Módulo 3 (Tabelas & GitHub Pages)",
                titleEn = "Curso em Vídeo — HTML5 & CSS3 Module 3 (Tables & Hosting)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dmcAviDwiGgHbeEJToxbOpZ",
                durationLabel = "Módulo 3",
                descriptionPt = "Aprenda tabelas semânticas completas, imagens de fundo e publicação gratuita com o Prof. Guanabara.",
                descriptionEn = "Learn structured HTML tables, background media, and free web hosting with Gustavo Guanabara."
            ),
            CourseReference(
                id = "html_ref_cev_m4",
                titlePt = "Curso em Vídeo — HTML5 & CSS3 Módulo 4 (Formulários & Iframes)",
                titleEn = "Curso em Vídeo — HTML5 & CSS3 Module 4 (Forms & Iframes)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkcVCk2Bn_fdVQ81Fkrh6WT",
                durationLabel = "Módulo 4",
                descriptionPt = "Formulários modernos, inputs nativos, iframes seguros e adaptação para telas móveis.",
                descriptionEn = "Modern HTML5 forms, native inputs, safe iframes, and responsive mobile layouts."
            ),
            CourseReference(
                id = "html_ref_cev_site",
                titlePt = "Curso em Vídeo — Portal Oficial HTML5 & CSS3",
                titleEn = "Curso em Vídeo — Official HTML5 & CSS3 Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Site Oficial",
                descriptionPt = "Acesse os módulos completos e materiais gratuitos de apoio no site oficial do Curso em Vídeo.",
                descriptionEn = "Access full free HTML5/CSS3 modules and study resources on the official Curso em Vídeo portal."
            ),
            CourseReference(
                id = "html_ref_fcc",
                titlePt = "freeCodeCamp — HTML Full Course (2h02)",
                titleEn = "freeCodeCamp — HTML Full Course (2h02)",
                provider = "YouTube • freeCodeCamp.org",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=pQN-pnXPaVg",
                durationLabel = "2h 02m",
                descriptionPt = "Curso direto ao ponto em vídeo construindo páginas completas com tags essenciais do HTML5.",
                descriptionEn = "Hands-on HTML5 full course building real pages using core document and semantic tags."
            ),
            CourseReference(
                id = "html_ref_mdn",
                titlePt = "MDN Web Docs — Aprenda Desenvolvimento Web",
                titleEn = "MDN Web Docs — Learn Web Development",
                provider = "developer.mozilla.org",
                isPortuguese = true,
                type = CourseResourceType.OFFICIAL_DOCS,
                url = "https://developer.mozilla.org/pt-BR/docs/Learn",
                durationLabel = "Guia Gratuito",
                descriptionPt = "Referência padrão-ouro da Mozilla em português com guias passo a passo de HTML semântico.",
                descriptionEn = "Mozilla's gold-standard web development curriculum with semantic HTML guides."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "html_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Estrutura do Documento e Semântica",
                titleEn = "Module 1: Document Structure & Semantics",
                objectivePt = "Estruturar páginas válidas com DOCTYPE, head, hierarquia de títulos e tags semânticas.",
                objectiveEn = "Build valid pages with DOCTYPE, head metadata, heading hierarchy, and semantic tags.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkZ9-atkcmcBaMZdmLHft8n",
                primaryVideoTitlePt = "Assistir HTML5 Módulo 1",
                primaryVideoTitleEn = "Watch HTML5 Module 1",
                lessons = listOf(
                    CourseLesson(
                        id = "html_m1_l1",
                        lessonNumber = 1,
                        titlePt = "Anatomy HTML5: DOCTYPE, Head e Meta Tags",
                        titleEn = "HTML5 Anatomy: DOCTYPE, Head & Meta Tags",
                        summaryPt = "Entenda charset UTF-8, viewport responsivo e SEO básico no cabeçalho.",
                        summaryEn = "Configure UTF-8 charset, responsive viewport, and essential head SEO metadata.",
                        durationMinutes = 20,
                        topicsPt = listOf("<!DOCTYPE html>", "<meta viewport>", "<title> e favicon"),
                        topicsEn = listOf("<!DOCTYPE html>", "<meta viewport>", "<title> & favicon"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkZ9-atkcmcBaMZdmLHft8n",
                        videoRefLabelPt = "Aula 1–10 (Curso em Vídeo)",
                        videoRefLabelEn = "Lessons 1–10 (Curso em Vídeo)"
                    ),
                    CourseLesson(
                        id = "html_m1_l2",
                        lessonNumber = 2,
                        titlePt = "Tags Semânticas: header, main, section, article",
                        titleEn = "Semantic Tags: header, main, section, article",
                        summaryPt = "Substitua divs genéricas por marcação acessível para leitores de tela e buscadores.",
                        summaryEn = "Replace generic divs with accessible semantic landmarks for screen readers and SEO.",
                        durationMinutes = 25,
                        topicsPt = listOf("<header> e <nav>", "<main> e <article>", "<footer> e <aside>"),
                        topicsEn = listOf("<header> & <nav>", "<main> & <article>", "<footer> & <aside>"),
                        videoRefUrl = "https://developer.mozilla.org/pt-BR/docs/Learn",
                        videoRefLabelPt = "Guia Semântico na MDN",
                        videoRefLabelEn = "MDN Semantic HTML Guide"
                    ),
                    CourseLesson(
                        id = "html_m1_l3",
                        lessonNumber = 3,
                        titlePt = "Listas, Links e Mídias Responsivas",
                        titleEn = "Lists, Links & Responsive Media",
                        summaryPt = "Crie menus com <ul>, links externos seguros e imagens com alt descritivo.",
                        summaryEn = "Build navigation lists, safe external links, and accessible images with alt attributes.",
                        durationMinutes = 25,
                        topicsPt = listOf("<ul>, <ol>, <li>", "<a href target rel>", "<img alt> e <picture>"),
                        topicsEn = listOf("<ul>, <ol>, <li>", "<a href target rel>", "<img alt> & <picture>"),
                        videoRefUrl = "https://www.youtube.com/watch?v=pQN-pnXPaVg",
                        videoRefLabelPt = "HTML Completo (freeCodeCamp)",
                        videoRefLabelEn = "HTML Full Course (freeCodeCamp)"
                    )
                )
            ),
            CourseModule(
                id = "html_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Formulários, Tabelas e Acessibilidade",
                titleEn = "Module 2: Forms, Tables & Accessibility",
                objectivePt = "Coletar dados de usuários com validação nativa HTML5 e tabelas semânticas.",
                objectiveEn = "Collect user input using native HTML5 validation and semantic data tables.",
                levelLabelPt = "Prático",
                levelLabelEn = "Practical",
                primaryVideoUrl = "https://www.freecodecamp.org/learn",
                primaryVideoTitlePt = "Praticar no freeCodeCamp",
                primaryVideoTitleEn = "Practice on freeCodeCamp",
                lessons = listOf(
                    CourseLesson(
                        id = "html_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Formulários Modernos e Validação Nativa",
                        titleEn = "Modern Forms & Native Validation",
                        summaryPt = "Vincule <label for> a <input>, use type=email/number, required e pattern.",
                        summaryEn = "Connect <label for> to inputs and apply email, number, required, and pattern rules.",
                        durationMinutes = 30,
                        topicsPt = listOf("<form action method>", "<label> e acessibilidade", "required, minlength, pattern"),
                        topicsEn = listOf("<form action method>", "<label> accessibility", "Native validation attributes"),
                        videoRefUrl = "https://www.freecodecamp.org/learn",
                        videoRefLabelPt = "Projeto Formulário (freeCodeCamp)",
                        videoRefLabelEn = "Survey Form Project (freeCodeCamp)"
                    ),
                    CourseLesson(
                        id = "html_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Tabelas Estruturadas: thead, tbody, th scope",
                        titleEn = "Structured Tables: thead, tbody, th scope",
                        summaryPt = "Organize dados tabulares com legendas <caption> e escopo de cabeçalho.",
                        summaryEn = "Organize tabular data cleanly using <caption>, <thead>, and header scopes.",
                        durationMinutes = 20,
                        topicsPt = listOf("<thead>, <tbody>, <tfoot>", "colspan e rowspan", "scope='col'/'row'"),
                        topicsEn = listOf("<thead>, <tbody>, <tfoot>", "colspan & rowspan", "scope='col'/'row'"),
                        videoRefUrl = "https://developer.mozilla.org/pt-BR/docs/Learn",
                        videoRefLabelPt = "Tabelas HTML na MDN",
                        videoRefLabelEn = "MDN HTML Tables"
                    ),
                    CourseLesson(
                        id = "html_m2_l3",
                        lessonNumber = 3,
                        titlePt = "ARIA, Teclado e Boas Práticas de Acessibilidade",
                        titleEn = "ARIA Landmarks & Accessibility Best Practices",
                        summaryPt = "Garanta foco lógico por teclado, contrastes corretos e aria-label quando necessário.",
                        summaryEn = "Ensure keyboard focus order, semantic buttons, and meaningful aria-labels.",
                        durationMinutes = 25,
                        topicsPt = listOf("Botão vs Link", "aria-label e aria-live", "Ordem de tabulação"),
                        topicsEn = listOf("Button vs Link", "aria-label & aria-live", "Tab order"),
                        videoRefUrl = "https://developer.mozilla.org/pt-BR/docs/Learn",
                        videoRefLabelPt = "Acessibilidade Web (MDN)",
                        videoRefLabelEn = "Web Accessibility (MDN)"
                    )
                )
            )
        )
    )

    private fun buildCssCurriculum() = TechCourseCurriculum(
        tech = Technology.CSS,
        references = listOf(
            CourseReference(
                id = "css_ref_cev_m2",
                titlePt = "Curso em Vídeo — CSS3 Módulo 2 (Cores, Fontes, Box Model)",
                titleEn = "Curso em Vídeo — CSS3 Module 2 (Colors, Fonts, Box Model)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlUpEXkY1AyVLQGcpSgVF8s",
                durationLabel = "44 aulas",
                descriptionPt = "Playlist gratuita em português focada em design web, paleta de cores, seletores e Box Model.",
                descriptionEn = "Free Portuguese playlist dedicated to web design, color palettes, selectors, and the Box Model."
            ),
            CourseReference(
                id = "css_ref_cev_m4",
                titlePt = "Curso em Vídeo — CSS3 Módulo 4 (Media Queries & Responsivo)",
                titleEn = "Curso em Vídeo — CSS3 Module 4 (Media Queries & Responsive)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkcVCk2Bn_fdVQ81Fkrh6WT",
                durationLabel = "Módulo 4",
                descriptionPt = "Domine Media Queries, Mobile-First, menus responsivos e estilização avançada com o Prof. Guanabara.",
                descriptionEn = "Master Media Queries, Mobile-First, responsive navigation, and styling with Gustavo Guanabara."
            ),
            CourseReference(
                id = "css_ref_cev_site",
                titlePt = "Curso em Vídeo — Portal Oficial de Cursos",
                titleEn = "Curso em Vídeo — Official Courses Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Portal Grátis",
                descriptionPt = "Acesse todos os módulos gratuitos de HTML5 e CSS3 direto no site oficial do Curso em Vídeo.",
                descriptionEn = "Access all free HTML5 & CSS3 course modules directly on the Curso em Vídeo website."
            ),
            CourseReference(
                id = "css_ref_fcc_dave",
                titlePt = "freeCodeCamp — CSS Tutorial Full Course (11h)",
                titleEn = "freeCodeCamp — CSS Full Course by Dave Gray (11h)",
                provider = "YouTube • freeCodeCamp.org",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=OXGznpKZ_sA",
                durationLabel = "11h 08m",
                descriptionPt = "Curso completo cobrindo seletores, especificidade, Flexbox, Grid Layout, animações e responsividade.",
                descriptionEn = "Comprehensive CSS masterclass covering selectors, specificity, Flexbox, Grid, animations, and responsive design."
            ),
            CourseReference(
                id = "css_ref_fcc_web",
                titlePt = "freeCodeCamp — Trilha Web Design Responsivo",
                titleEn = "freeCodeCamp — Responsive Web Design Curriculum",
                provider = "freecodecamp.org",
                isPortuguese = false,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.freecodecamp.org/learn",
                durationLabel = "Interativo + Cert.",
                descriptionPt = "Plataforma prática gratuita com dezenas de desafios guiados de CSS, Flexbox e Grid.",
                descriptionEn = "Interactive curriculum with hands-on projects for modern CSS, Flexbox, and CSS Grid."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "css_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Seletores, Especificidade e Box Model",
                titleEn = "Module 1: Selectors, Specificity & Box Model",
                objectivePt = "Controlar espaçamentos, bordas, cores e hierarquia de regras CSS.",
                objectiveEn = "Control spacing, borders, colors, and CSS cascade specificity.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlUpEXkY1AyVLQGcpSgVF8s",
                primaryVideoTitlePt = "Assistir Módulo 2 (Curso em Vídeo)",
                primaryVideoTitleEn = "Watch CSS Module 2 (Curso em Vídeo)",
                lessons = listOf(
                    CourseLesson(
                        id = "css_m1_l1",
                        lessonNumber = 1,
                        titlePt = "Cascata, Especificidade e Variáveis CSS (--var)",
                        titleEn = "Cascade, Specificity & CSS Variables",
                        summaryPt = "Evite !important organizando tokens de cores e fontes em :root.",
                        summaryEn = "Avoid !important by organizing color and typography tokens inside :root.",
                        durationMinutes = 25,
                        topicsPt = listOf("Classe vs ID vs Tag", ":root e var(--cor)", "Herança de propriedades"),
                        topicsEn = listOf("Class vs ID vs Element", ":root & var(--token)", "Property inheritance"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlUpEXkY1AyVLQGcpSgVF8s",
                        videoRefLabelPt = "Cores & Seletores (Curso em Vídeo)",
                        videoRefLabelEn = "Colors & Selectors Playlist"
                    ),
                    CourseLesson(
                        id = "css_m1_l2",
                        lessonNumber = 2,
                        titlePt = "Box Model e box-sizing: border-box",
                        titleEn = "Box Model & box-sizing: border-box",
                        summaryPt = "Calcule largura real somando content, padding, border e margin sem quebrar layouts.",
                        summaryEn = "Manage width, padding, border, and margin predictably with border-box.",
                        durationMinutes = 25,
                        topicsPt = listOf("margin vs padding", "border-box", "Unidades rem, em, %"),
                        topicsEn = listOf("margin vs padding", "border-box", "rem, em, % units"),
                        videoRefUrl = "https://www.youtube.com/watch?v=OXGznpKZ_sA",
                        videoRefLabelPt = "Box Model (Dave Gray)",
                        videoRefLabelEn = "Box Model (Dave Gray)"
                    )
                )
            ),
            CourseModule(
                id = "css_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Flexbox, Grid e Design Responsivo",
                titleEn = "Module 2: Flexbox, Grid & Responsive Design",
                objectivePt = "Construir layouts modernos que se adaptam de smartphones a monitores ultrawide.",
                objectiveEn = "Build modern fluid layouts adapting smoothly from mobile to desktop.",
                levelLabelPt = "Intermediário",
                levelLabelEn = "Intermediate",
                primaryVideoUrl = "https://www.youtube.com/watch?v=OXGznpKZ_sA",
                primaryVideoTitlePt = "Assistir Flexbox & Grid",
                primaryVideoTitleEn = "Watch Flexbox & Grid",
                lessons = listOf(
                    CourseLesson(
                        id = "css_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Flexbox Completo: Alinhamento em 1 Dimensão",
                        titleEn = "Complete Flexbox: 1D Alignment",
                        summaryPt = "Domine flex-direction, justify-content, align-items, gap e flex-wrap.",
                        summaryEn = "Master flex-direction, justify-content, align-items, gap, and flex-wrap.",
                        durationMinutes = 35,
                        topicsPt = listOf("display: flex", "justify-content & align-items", "gap e flex-grow"),
                        topicsEn = listOf("display: flex", "justify-content & align-items", "gap & flex-grow"),
                        videoRefUrl = "https://www.youtube.com/watch?v=OXGznpKZ_sA",
                        videoRefLabelPt = "Capítulo Flexbox (freeCodeCamp)",
                        videoRefLabelEn = "Flexbox Chapter (freeCodeCamp)"
                    ),
                    CourseLesson(
                        id = "css_m2_l2",
                        lessonNumber = 2,
                        titlePt = "CSS Grid Layout: Grades Bidimensionais",
                        titleEn = "CSS Grid Layout: 2D Grids",
                        summaryPt = "Crie galerias automáticas com repeat(auto-fit, minmax(240px, 1fr)).",
                        summaryEn = "Build auto-flowing responsive galleries using repeat(auto-fit, minmax(...)).",
                        durationMinutes = 35,
                        topicsPt = listOf("grid-template-columns", "Unidade fr e minmax()", "grid-template-areas"),
                        topicsEn = listOf("grid-template-columns", "fr unit & minmax()", "grid-template-areas"),
                        videoRefUrl = "https://www.freecodecamp.org/learn",
                        videoRefLabelPt = "Prática de Grid (freeCodeCamp)",
                        videoRefLabelEn = "CSS Grid Practice (freeCodeCamp)"
                    ),
                    CourseLesson(
                        id = "css_m2_l3",
                        lessonNumber = 3,
                        titlePt = "Media Queries, Mobile-First e Transições",
                        titleEn = "Media Queries, Mobile-First & Transitions",
                        summaryPt = "Comece pelo mobile e adicione breakpoints @media (min-width: ...) com microanimações.",
                        summaryEn = "Start mobile-first and layer @media breakpoints with smooth CSS transitions.",
                        durationMinutes = 30,
                        topicsPt = listOf("@media (min-width: 768px)", "clamp() fluido", "transition e transform"),
                        topicsEn = listOf("@media breakpoints", "Fluid clamp()", "transition & transform"),
                        videoRefUrl = "https://developer.mozilla.org/pt-BR/docs/Learn",
                        videoRefLabelPt = "Layout Responsivo (MDN)",
                        videoRefLabelEn = "Responsive Design (MDN)"
                    )
                )
            )
        )
    )

    private fun buildJavaScriptCurriculum() = TechCourseCurriculum(
        tech = Technology.JAVASCRIPT,
        references = listOf(
            CourseReference(
                id = "js_ref_cev",
                titlePt = "Curso em Vídeo — JavaScript & ECMAScript (Guanabara)",
                titleEn = "Curso em Vídeo — Modern JavaScript Course",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlsK3Nr9GVvXCbpQyHQl1o1",
                durationLabel = "40h • Playlist",
                descriptionPt = "Curso gratuito completo em português cobrindo DOM, eventos, condições, repetições, arrays e funções.",
                descriptionEn = "Complete free Portuguese playlist covering DOM manipulation, events, loops, arrays, and functions."
            ),
            CourseReference(
                id = "js_ref_cev_site",
                titlePt = "Curso em Vídeo — Site Oficial & Material de Apoio",
                titleEn = "Curso em Vídeo — Official Site & Study Materials",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Site Oficial",
                descriptionPt = "Acesse o curso gratuito de JavaScript e a trilha completa do Prof. Guanabara no site Curso em Vídeo.",
                descriptionEn = "Access Gustavo Guanabara's free JavaScript course directly on the Curso em Vídeo website."
            ),
            CourseReference(
                id = "js_ref_fcc",
                titlePt = "freeCodeCamp — JavaScript Programming Full Course (7h44)",
                titleEn = "freeCodeCamp — JavaScript Programming Full Course (7h44)",
                provider = "YouTube • freeCodeCamp / Scrimba",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=jS4aFq5-91M",
                durationLabel = "7h 44m",
                descriptionPt = "Curso interativo em vídeo construindo mini-aplicações reais passo a passo.",
                descriptionEn = "Interactive full JavaScript course building real mini-apps step by step."
            ),
            CourseReference(
                id = "js_ref_mdn",
                titlePt = "MDN Web Docs — Guia Oficial de JavaScript",
                titleEn = "MDN Web Docs — Official JavaScript Guide",
                provider = "developer.mozilla.org",
                isPortuguese = true,
                type = CourseResourceType.OFFICIAL_DOCS,
                url = "https://developer.mozilla.org/pt-BR/docs/Learn",
                durationLabel = "Referência PT-BR",
                descriptionPt = "Documentação detalhada sobre DOM, Fetch API, Promises e recursos modernos ES6+.",
                descriptionEn = "Detailed official guides covering DOM APIs, Fetch, Promises, and ES6+ syntax."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "js_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Fundamentos ES6+ e Manipulação do DOM",
                titleEn = "Module 1: ES6+ Fundamentals & DOM Manipulation",
                objectivePt = "Conectar lógica JavaScript aos elementos HTML da página com eventos reais.",
                objectiveEn = "Connect JavaScript logic to HTML elements and browser events.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlsK3Nr9GVvXCbpQyHQl1o1",
                primaryVideoTitlePt = "Assistir Playlist Curso em Vídeo",
                primaryVideoTitleEn = "Watch Curso em Vídeo Playlist",
                lessons = listOf(
                    CourseLesson(
                        id = "js_m1_l1",
                        lessonNumber = 1,
                        titlePt = "let, const, Tipos e Template Literals",
                        titleEn = "let, const, Data Types & Template Literals",
                        summaryPt = "Prefira const por padrão, use === estrito e interpole strings com crases `...`.",
                        summaryEn = "Default to const, enforce strict equality ===, and use template literals.",
                        durationMinutes = 25,
                        topicsPt = listOf("const vs let", "Igualdade estrita ===", "Template strings `\${}`"),
                        topicsEn = listOf("const vs let", "Strict equality ===", "Template literals"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlsK3Nr9GVvXCbpQyHQl1o1",
                        videoRefLabelPt = "Módulo B (Curso em Vídeo)",
                        videoRefLabelEn = "Module B (Curso em Vídeo)"
                    ),
                    CourseLesson(
                        id = "js_m1_l2",
                        lessonNumber = 2,
                        titlePt = "DOM: querySelector e addEventListener",
                        titleEn = "DOM: querySelector & addEventListener",
                        summaryPt = "Capture cliques e formulários alterando conteúdo e classes CSS dinamicamente.",
                        summaryEn = "Handle user clicks and form submits updating DOM text and CSS classes.",
                        durationMinutes = 30,
                        topicsPt = listOf("document.querySelector", "addEventListener('click')", "classList.toggle()"),
                        topicsEn = listOf("document.querySelector", "addEventListener", "classList.toggle()"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlsK3Nr9GVvXCbpQyHQl1o1",
                        videoRefLabelPt = "Módulo C — DOM (Curso em Vídeo)",
                        videoRefLabelEn = "Module C — DOM (Curso em Vídeo)"
                    )
                )
            ),
            CourseModule(
                id = "js_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Arrays Funcionais, Objetos e Async/Await",
                titleEn = "Module 2: Functional Arrays, Objects & Async/Await",
                objectivePt = "Transformar listas com map/filter/reduce e consumir APIs REST assíncronas.",
                objectiveEn = "Transform lists with map/filter/reduce and consume async REST APIs.",
                levelLabelPt = "Intermediário",
                levelLabelEn = "Intermediate",
                primaryVideoUrl = "https://www.youtube.com/watch?v=jS4aFq5-91M",
                primaryVideoTitlePt = "Assistir JavaScript Prático",
                primaryVideoTitleEn = "Watch Practical JavaScript",
                lessons = listOf(
                    CourseLesson(
                        id = "js_m2_l1",
                        lessonNumber = 1,
                        titlePt = "map, filter, reduce e Desestruturação",
                        titleEn = "map, filter, reduce & Destructuring",
                        summaryPt = "Manipule coleções imutáveis e extraia propriedades de objetos com { nome, idade }.",
                        summaryEn = "Process arrays immutably and unpack objects cleanly with destructuring.",
                        durationMinutes = 35,
                        topicsPt = listOf("array.map() e filter()", "reduce() acumulativo", "Spread ... e Destructuring"),
                        topicsEn = listOf("map() & filter()", "reduce() accumulator", "Spread & Destructuring"),
                        videoRefUrl = "https://www.youtube.com/watch?v=PkZNo7MFNFg",
                        videoRefLabelPt = "ES6 Moderno (freeCodeCamp)",
                        videoRefLabelEn = "Modern ES6 (freeCodeCamp)"
                    ),
                    CourseLesson(
                        id = "js_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Promises, Fetch API e async/await",
                        titleEn = "Promises, Fetch API & async/await",
                        summaryPt = "Busque dados JSON de APIs externas tratando erros de rede com try/catch.",
                        summaryEn = "Fetch external JSON APIs cleanly while handling network errors with try/catch.",
                        durationMinutes = 35,
                        topicsPt = listOf("fetch(url)", "async / await", "response.ok e try/catch"),
                        topicsEn = listOf("fetch(url)", "async / await", "Error handling with try/catch"),
                        videoRefUrl = "https://developer.mozilla.org/pt-BR/docs/Learn",
                        videoRefLabelPt = "JS Assíncrono na MDN",
                        videoRefLabelEn = "Async JS on MDN"
                    )
                )
            )
        )
    )

    private fun buildTypeScriptCurriculum() = TechCourseCurriculum(
        tech = Technology.TYPESCRIPT,
        references = listOf(
            CourseReference(
                id = "ts_ref_cfb",
                titlePt = "CFBCursos — Curso Completo de TypeScript (PT-BR)",
                titleEn = "CFBCursos — Complete TypeScript Course (PT-BR)",
                provider = "YouTube • CFBCursos",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLx4x_zx8csUGLbxiUPH-kPq4q-vQjHkbc",
                durationLabel = "37 aulas",
                descriptionPt = "Playlist didática em português indo de tipos básicos e interfaces até generics, classes e módulos.",
                descriptionEn = "Step-by-step Portuguese playlist from basic types and interfaces to generics and modules."
            ),
            CourseReference(
                id = "ts_ref_cev_js",
                titlePt = "Curso em Vídeo — Base ECMAScript / JS (Guanabara)",
                titleEn = "Curso em Vídeo — JS/ECMAScript Foundation",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dlsK3Nr9GVvXCbpQyHQl1o1",
                durationLabel = "40h • Base JS",
                descriptionPt = "Reforce toda a base de JavaScript moderno com o Prof. Guanabara antes ou durante seus estudos de TypeScript.",
                descriptionEn = "Strengthen modern JavaScript fundamentals with Gustavo Guanabara alongside TypeScript."
            ),
            CourseReference(
                id = "ts_ref_cev_site",
                titlePt = "Curso em Vídeo — Portal de Cursos Gratuitos",
                titleEn = "Curso em Vídeo — Free Courses Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Site Oficial",
                descriptionPt = "Acesse os cursos gratuitos de JavaScript, Lógica e Web no portal oficial do Curso em Vídeo.",
                descriptionEn = "Access free JavaScript, Logic, and Web tracks on the Curso em Vídeo portal."
            ),
            CourseReference(
                id = "ts_ref_fcc",
                titlePt = "freeCodeCamp — Learn TypeScript Full Tutorial (5h03)",
                titleEn = "freeCodeCamp — Learn TypeScript Full Tutorial (5h03)",
                provider = "YouTube • freeCodeCamp / Hitesh",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=30LWjhZzg50",
                durationLabel = "5h 03m",
                descriptionPt = "Curso prático completo ensinando tipagem segura, narrowing, classes e generics.",
                descriptionEn = "Full TypeScript course teaching strict typing, narrowing, classes, and generics."
            ),
            CourseReference(
                id = "ts_ref_docs",
                titlePt = "TypeScript Docs — TypeScript em 5 Minutos & Handbook",
                titleEn = "TypeScript Docs — TS in 5 Minutes & Handbook",
                provider = "typescriptlang.org",
                isPortuguese = false,
                type = CourseResourceType.OFFICIAL_DOCS,
                url = "https://www.typescriptlang.org/docs/handbook/typescript-in-5-minutes.html",
                durationLabel = "Guia Oficial",
                descriptionPt = "Documentação oficial da Microsoft com playground e guia de boas práticas.",
                descriptionEn = "Official Microsoft TypeScript handbook with live examples and strict mode tips."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "ts_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Sistema de Tipos e Interfaces",
                titleEn = "Module 1: Type System & Interfaces",
                objectivePt = "Evitar bugs em tempo de compilação com anotações, unions e contratos.",
                objectiveEn = "Catch bugs at compile time with annotations, unions, and interfaces.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLx4x_zx8csUGLbxiUPH-kPq4q-vQjHkbc",
                primaryVideoTitlePt = "Assistir Playlist CFBCursos",
                primaryVideoTitleEn = "Watch CFBCursos Playlist",
                lessons = listOf(
                    CourseLesson(
                        id = "ts_m1_l1",
                        lessonNumber = 1,
                        titlePt = "Tipos Primitivos, Arrays, Tuplas e Enums",
                        titleEn = "Primitives, Arrays, Tuples & Enums",
                        summaryPt = "Configure tsconfig strict e declare contratos explícitos para parâmetros e retornos.",
                        summaryEn = "Enable strict tsconfig and annotate function parameters and return types.",
                        durationMinutes = 25,
                        topicsPt = listOf("string, number, boolean", "Tuplas [string, number]", "Union Types A | B"),
                        topicsEn = listOf("Primitives", "Tuples [string, number]", "Union Types A | B"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLx4x_zx8csUGLbxiUPH-kPq4q-vQjHkbc",
                        videoRefLabelPt = "Aulas Iniciais (CFBCursos)",
                        videoRefLabelEn = "Intro Lessons (CFBCursos)"
                    ),
                    CourseLesson(
                        id = "ts_m1_l2",
                        lessonNumber = 2,
                        titlePt = "Interface vs Type Alias e Propriedades Opcionais",
                        titleEn = "Interface vs Type Alias & Optional Fields",
                        summaryPt = "Modele objetos de API com readonly, campos opcionais (?) e interseção (&).",
                        summaryEn = "Model API payloads using readonly, optional fields (?), and intersections.",
                        durationMinutes = 30,
                        topicsPt = listOf("interface User {}", "type Status = 'ok' | 'err'", "readonly e optional ?"),
                        topicsEn = listOf("interface vs type", "Literal unions", "readonly & optional ?"),
                        videoRefUrl = "https://www.typescriptlang.org/docs/handbook/typescript-in-5-minutes.html",
                        videoRefLabelPt = "Handbook TypeScript.org",
                        videoRefLabelEn = "TypeScript.org Handbook"
                    )
                )
            ),
            CourseModule(
                id = "ts_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Generics, Narrowing e Utility Types",
                titleEn = "Module 2: Generics, Narrowing & Utility Types",
                objectivePt = "Escrever bibliotecas e serviços reutilizáveis mantendo segurança total de tipos.",
                objectiveEn = "Build reusable services with full static type safety.",
                levelLabelPt = "Intermediário",
                levelLabelEn = "Intermediate",
                primaryVideoUrl = "https://www.youtube.com/watch?v=30LWjhZzg50",
                primaryVideoTitlePt = "Assistir TypeScript Full Course",
                primaryVideoTitleEn = "Watch TypeScript Full Course",
                lessons = listOf(
                    CourseLesson(
                        id = "ts_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Type Narrowing e Type Guards",
                        titleEn = "Type Narrowing & Type Guards",
                        summaryPt = "Use typeof, in, instanceof e predicados customizados no lugar de type assertions.",
                        summaryEn = "Use typeof, in, instanceof, and custom guards instead of unsafe casts.",
                        durationMinutes = 30,
                        topicsPt = listOf("typeof e instanceof", "Discriminated Unions", "Evitando any com unknown"),
                        topicsEn = listOf("typeof & instanceof", "Discriminated Unions", "unknown over any"),
                        videoRefUrl = "https://www.youtube.com/watch?v=30LWjhZzg50",
                        videoRefLabelPt = "Narrowing (freeCodeCamp)",
                        videoRefLabelEn = "Narrowing (freeCodeCamp)"
                    ),
                    CourseLesson(
                        id = "ts_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Generics <T> e Utility Types (Partial, Pick, Omit)",
                        titleEn = "Generics <T> & Utility Types",
                        summaryPt = "Crie respostas genéricas ApiResponse<T> e derive DTOs com Pick/Omit/Partial.",
                        summaryEn = "Create generic ApiResponse<T> wrappers and derive DTOs with Pick, Omit, Partial.",
                        durationMinutes = 35,
                        topicsPt = listOf("function wrap<T>(v: T)", "Partial<T> e Omit<T, K>", "Record<K, V>"),
                        topicsEn = listOf("Generic functions & types", "Partial & Omit", "Record<K, V>"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLx4x_zx8csUGLbxiUPH-kPq4q-vQjHkbc",
                        videoRefLabelPt = "Generics (CFBCursos)",
                        videoRefLabelEn = "Generics (CFBCursos)"
                    )
                )
            )
        )
    )
}
