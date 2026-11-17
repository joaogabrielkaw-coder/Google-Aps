package com.example.data.repository

import com.example.model.CourseLesson
import com.example.model.CourseModule
import com.example.model.CourseReference
import com.example.model.CourseResourceType
import com.example.model.TechCourseCurriculum
import com.example.model.Technology

/**
 * Complemento do catálogo curado de Módulos e Referências Gratuitas:
 * Java, Kotlin, Go, MySQL, PostgreSQL e MongoDB.
 */
object TechCourseModulesDataPart2 {

    fun buildJavaCurriculum() = TechCourseCurriculum(
        tech = Technology.JAVA,
        references = listOf(
            CourseReference(
                id = "java_ref_cev",
                titlePt = "Curso em Vídeo — Java para Iniciantes (Guanabara)",
                titleEn = "Curso em Vídeo — Java for Beginners",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkI2ZdjTwZA4mPMxWTfNSpR",
                durationLabel = "30+ aulas",
                descriptionPt = "Playlist clássica em português cobrindo JDK, lógica, estruturas condicionais, laços e métodos em Java.",
                descriptionEn = "Classic free Portuguese playlist covering JDK setup, conditionals, loops, and Java methods."
            ),
            CourseReference(
                id = "java_ref_cev_poo",
                titlePt = "Curso em Vídeo — Java POO (Orientação a Objetos)",
                titleEn = "Curso em Vídeo — Java OOP (Object-Oriented)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkqe2aR0tQK74m8SFe-aGsY",
                durationLabel = "30 aulas • POO",
                descriptionPt = "Curso completo do Prof. Guanabara focado em Classes, Objetos, Encapsulamento, Herança e Polimorfismo em Java.",
                descriptionEn = "Complete Java OOP playlist covering classes, encapsulation, inheritance, and polymorphism."
            ),
            CourseReference(
                id = "java_ref_cev_site",
                titlePt = "Curso em Vídeo — Portal Oficial Java & Lógica",
                titleEn = "Curso em Vídeo — Official Java & Logic Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Portal Grátis",
                descriptionPt = "Acesse os cursos completos de Java Básico, Java POO e Algoritmos no site oficial do Curso em Vídeo.",
                descriptionEn = "Access free Java Basics, Java OOP, and Algorithms tracks on the Curso em Vídeo website."
            ),
            CourseReference(
                id = "java_ref_devdojo",
                titlePt = "DevDojo — Maratona Java Virado no Jiraya",
                titleEn = "DevDojo — Maratona Java Bootcamp",
                provider = "YouTube • DevDojo",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PL62G310vn6nFIsPC0Hqyv9c5H6h2kO0sE",
                durationLabel = "280+ aulas",
                descriptionPt = "Curso livre super completo do básico ao avançado: POO, Collections, Lambdas, Streams, JDBC e Threads.",
                descriptionEn = "Comprehensive free bootcamp from Java basics to OOP, Collections, Streams, JDBC, and concurrency."
            ),
            CourseReference(
                id = "java_ref_devjava",
                titlePt = "Dev.java — Portal Oficial de Aprendizado Oracle",
                titleEn = "Dev.java — Official Oracle Learning Portal",
                provider = "dev.java",
                isPortuguese = false,
                type = CourseResourceType.OFFICIAL_DOCS,
                url = "https://dev.java/learn/",
                durationLabel = "Oficial Oracle",
                descriptionPt = "Tutoriais oficiais modernos da linguagem Java (Java 17/21+, Records, Streams e Pattern Matching).",
                descriptionEn = "Official modern Java learning tracks covering Java 17/21+, Records, Streams, and Pattern Matching."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "java_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Sintaxe Forte, Controle de Fluxo e Arrays",
                titleEn = "Module 1: Strong Typing, Flow Control & Arrays",
                objectivePt = "Compreender JVM, tipos primitivos e estruturas básicas de decisão e repetição.",
                objectiveEn = "Understand the JVM, primitive types, and basic branching/looping constructs.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkI2ZdjTwZA4mPMxWTfNSpR",
                primaryVideoTitlePt = "Assistir Java Iniciantes (YouTube)",
                primaryVideoTitleEn = "Watch Java Beginners (YouTube)",
                lessons = listOf(
                    CourseLesson(
                        id = "java_m1_l1",
                        lessonNumber = 1,
                        titlePt = "JDK, Método main e Tipos Primitivos",
                        titleEn = "JDK, main Method & Primitive Types",
                        summaryPt = "Diferencie int, double, boolean e String e use Scanner para entrada de dados.",
                        summaryEn = "Distinguish int, double, boolean, and String, and read console input with Scanner.",
                        durationMinutes = 30,
                        topicsPt = listOf("public static void main", "Primitivos vs Wrapper", "Scanner e System.out"),
                        topicsEn = listOf("main method", "Primitives vs Wrappers", "Scanner & System.out"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkI2ZdjTwZA4mPMxWTfNSpR",
                        videoRefLabelPt = "Curso em Vídeo Java",
                        videoRefLabelEn = "Curso em Vídeo Java"
                    ),
                    CourseLesson(
                        id = "java_m1_l2",
                        lessonNumber = 2,
                        titlePt = "Condicionais, Switch e Laços for/while",
                        titleEn = "Conditionals, Switch & Loops",
                        summaryPt = "Construa menus e iterações sobre arrays com for tradicional e enhanced for-each.",
                        summaryEn = "Build branching logic and iterate arrays using standard and enhanced for-each loops.",
                        durationMinutes = 30,
                        topicsPt = listOf("if/else e switch", "for (int x : arr)", "Operador ternário"),
                        topicsEn = listOf("if/else & switch", "Enhanced for-each", "Ternary operator"),
                        videoRefUrl = "https://www.youtube.com/watch?v=eIrMbAQSU34",
                        videoRefLabelPt = "Java Tutorial (YouTube)",
                        videoRefLabelEn = "Java Tutorial (YouTube)"
                    )
                )
            ),
            CourseModule(
                id = "java_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Programação Orientada a Objetos (POO)",
                titleEn = "Module 2: Object-Oriented Programming (OOP)",
                objectivePt = "Aplicar encapsulamento, herança, polimorfismo e interfaces em sistemas Java.",
                objectiveEn = "Apply encapsulation, inheritance, polymorphism, and interfaces in Java.",
                levelLabelPt = "Intermediário",
                levelLabelEn = "Intermediate",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PL62G310vn6nFIsPC0Hqyv9c5H6h2kO0sE",
                primaryVideoTitlePt = "Assistir Maratona Java DevDojo",
                primaryVideoTitleEn = "Watch DevDojo Java Bootcamp",
                lessons = listOf(
                    CourseLesson(
                        id = "java_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Classes, Construtores e Encapsulamento",
                        titleEn = "Classes, Constructors & Encapsulation",
                        summaryPt = "Proteja atributos com private e exponha comportamentos consistentes via métodos.",
                        summaryEn = "Protect fields with private access and expose safe methods and constructors.",
                        durationMinutes = 35,
                        topicsPt = listOf("private, public, protected", "this e construtores", "Records do Java moderno"),
                        topicsEn = listOf("Access modifiers", "Constructors & this", "Modern Java Records"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PL62G310vn6nFIsPC0Hqyv9c5H6h2kO0sE",
                        videoRefLabelPt = "POO na Maratona Java",
                        videoRefLabelEn = "OOP in Maratona Java"
                    ),
                    CourseLesson(
                        id = "java_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Herança, Interfaces e Polimorfismo",
                        titleEn = "Inheritance, Interfaces & Polymorphism",
                        summaryPt = "Programe voltado para interfaces desacoplando implementações concretas.",
                        summaryEn = "Program to interfaces so components remain decoupled and testable.",
                        durationMinutes = 35,
                        topicsPt = listOf("extends vs implements", "@Override", "Polimorfismo dinâmico"),
                        topicsEn = listOf("extends vs implements", "@Override", "Dynamic polymorphism"),
                        videoRefUrl = "https://dev.java/learn/",
                        videoRefLabelPt = "Guia POO no Dev.java",
                        videoRefLabelEn = "OOP Guide on Dev.java"
                    )
                )
            ),
            CourseModule(
                id = "java_mod_3",
                moduleNumber = 3,
                titlePt = "Módulo 3: Collections, Exceções e Streams",
                titleEn = "Module 3: Collections, Exceptions & Streams",
                objectivePt = "Processar listas, mapas e fluxos de dados com Streams API e Optional.",
                objectiveEn = "Process collections and data pipelines using the Streams API and Optional.",
                levelLabelPt = "Prático",
                levelLabelEn = "Practical",
                primaryVideoUrl = "https://dev.java/learn/",
                primaryVideoTitlePt = "Explorar Streams no Dev.java",
                primaryVideoTitleEn = "Explore Streams on Dev.java",
                lessons = listOf(
                    CourseLesson(
                        id = "java_m3_l1",
                        lessonNumber = 1,
                        titlePt = "List, Set, Map e Generics",
                        titleEn = "List, Set, Map & Generics",
                        summaryPt = "Escolha entre ArrayList, HashSet e HashMap de acordo com busca e unicidade.",
                        summaryEn = "Choose between ArrayList, HashSet, and HashMap based on lookup and uniqueness.",
                        durationMinutes = 30,
                        topicsPt = listOf("ArrayList<T>", "HashMap<K, V>", "equals() e hashCode()"),
                        topicsEn = listOf("ArrayList<T>", "HashMap<K, V>", "equals() & hashCode()"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PL62G310vn6nFIsPC0Hqyv9c5H6h2kO0sE",
                        videoRefLabelPt = "Collections (DevDojo)",
                        videoRefLabelEn = "Collections (DevDojo)"
                    ),
                    CourseLesson(
                        id = "java_m3_l2",
                        lessonNumber = 2,
                        titlePt = "Lambdas, Stream API e Tratamento de Exceções",
                        titleEn = "Lambdas, Stream API & Exception Handling",
                        summaryPt = "Encadeie filter().map().toList() e proteja fluxos com try-with-resources.",
                        summaryEn = "Chain filter().map().toList() pipelines and manage resources with try-with-resources.",
                        durationMinutes = 35,
                        topicsPt = listOf("stream().filter().map()", "Optional<T>", "try-with-resources"),
                        topicsEn = listOf("Streams filter & map", "Optional<T>", "try-with-resources"),
                        videoRefUrl = "https://dev.java/learn/",
                        videoRefLabelPt = "Streams & Lambdas (Dev.java)",
                        videoRefLabelEn = "Streams & Lambdas (Dev.java)"
                    )
                )
            )
        )
    )

    fun buildKotlinCurriculum() = TechCourseCurriculum(
        tech = Technology.KOTLIN,
        references = listOf(
            CourseReference(
                id = "kt_ref_fcc",
                titlePt = "freeCodeCamp — Kotlin Course for Beginners (2h38)",
                titleEn = "freeCodeCamp — Kotlin Course for Beginners (2h38)",
                provider = "YouTube • freeCodeCamp.org",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=F9UC9DY-vIU",
                durationLabel = "2h 38m",
                descriptionPt = "Curso completo de Kotlin ensinando variáveis, null safety, coleções, classes, lambdas e extensões.",
                descriptionEn = "Full Kotlin course covering variables, null safety, collections, classes, lambdas, and extensions."
            ),
            CourseReference(
                id = "kt_ref_tour",
                titlePt = "Kotlin Tour Oficial (Interativo no Navegador)",
                titleEn = "Official Kotlin Tour (Interactive in Browser)",
                provider = "kotlinlang.org",
                isPortuguese = false,
                type = CourseResourceType.OFFICIAL_DOCS,
                url = "https://kotlinlang.org/docs/kotlin-tour-welcome.html",
                durationLabel = "Tour Oficial",
                descriptionPt = "Curso oficial da JetBrains com editor interativo em cada capítulo para testar Kotlin online.",
                descriptionEn = "JetBrains official interactive tour with runnable code playgrounds in every chapter."
            ),
            CourseReference(
                id = "kt_ref_android",
                titlePt = "Android Developers — Cursos Gratuitos de Kotlin & Compose",
                titleEn = "Android Developers — Free Kotlin & Compose Courses",
                provider = "developer.android.com",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://developer.android.com/courses",
                durationLabel = "Trilhas Google",
                descriptionPt = "Formação oficial gratuita do Google com módulos práticos de Kotlin, Jetpack Compose e arquitetura.",
                descriptionEn = "Official Google training pathways for Kotlin, Jetpack Compose, and modern Android architecture."
            ),
            CourseReference(
                id = "kt_ref_cev_algo",
                titlePt = "Curso em Vídeo — Algoritmos & POO (Base para Kotlin)",
                titleEn = "Curso em Vídeo — Algorithms & OOP Foundation",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dmSj0MHol_aoNYCSGFqvfXV",
                durationLabel = "Base PT-BR",
                descriptionPt = "Aulas gratuitas de Lógica e Algoritmos com o Prof. Guanabara + acesso ao portal cursoemvideo.com.",
                descriptionEn = "Free Portuguese Algorithms & Logic foundations by Gustavo Guanabara on YouTube."
            ),
            CourseReference(
                id = "kt_ref_cev_site",
                titlePt = "Curso em Vídeo — Site Oficial de Cursos Gratuitos",
                titleEn = "Curso em Vídeo — Official Free Courses Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Site Oficial",
                descriptionPt = "Explore todos os cursos livres de lógica, Java POO e programação do Curso em Vídeo.",
                descriptionEn = "Explore free logic, Java OOP, and programming courses on Curso em Vídeo."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "kt_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Sintaxe Moderna e Null Safety",
                titleEn = "Module 1: Modern Syntax & Null Safety",
                objectivePt = "Escrever código conciso eliminando NullPointerException em tempo de compilação.",
                objectiveEn = "Write concise code while eliminating NullPointerExceptions at compile time.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/watch?v=F9UC9DY-vIU",
                primaryVideoTitlePt = "Assistir Kotlin Full Course",
                primaryVideoTitleEn = "Watch Kotlin Full Course",
                lessons = listOf(
                    CourseLesson(
                        id = "kt_m1_l1",
                        lessonNumber = 1,
                        titlePt = "val vs var, Null Safety (?., ?:) e when",
                        titleEn = "val vs var, Null Safety (?., ?:) & when",
                        summaryPt = "Use chamadas seguras ?. e operador Elvis ?: no lugar de verificações manuais.",
                        summaryEn = "Use safe calls ?. and the Elvis operator ?: cleanly across nullable types.",
                        durationMinutes = 25,
                        topicsPt = listOf("val imutável vs var", "String? e operador ?:", "Expressão when"),
                        topicsEn = listOf("val vs var", "Nullable types & ?:", "when expression"),
                        videoRefUrl = "https://kotlinlang.org/docs/kotlin-tour-welcome.html",
                        videoRefLabelPt = "Capítulos 1–4 no Kotlin Tour",
                        videoRefLabelEn = "Chapters 1–4 on Kotlin Tour"
                    ),
                    CourseLesson(
                        id = "kt_m1_l2",
                        lessonNumber = 2,
                        titlePt = "Funções, Lambdas e Scope Functions",
                        titleEn = "Functions, Lambdas & Scope Functions",
                        summaryPt = "Domine parâmetros nomeados, extension functions e let / apply / run.",
                        summaryEn = "Master named parameters, extension functions, and let / apply / run.",
                        durationMinutes = 30,
                        topicsPt = listOf("Extension functions", "Lambdas { it -> ... }", "let, apply, also"),
                        topicsEn = listOf("Extension functions", "Trailing lambdas", "let, apply, also"),
                        videoRefUrl = "https://www.youtube.com/watch?v=F9UC9DY-vIU",
                        videoRefLabelPt = "Funções & Lambdas (YouTube)",
                        videoRefLabelEn = "Functions & Lambdas (YouTube)"
                    )
                )
            ),
            CourseModule(
                id = "kt_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Data Classes, Sealed Hierarchies e Coroutines",
                titleEn = "Module 2: Data Classes, Sealed Types & Coroutines",
                objectivePt = "Modelar estados de UI previsíveis e executar operações assíncronas com Flow.",
                objectiveEn = "Model predictable UI state and run structured async code with Coroutines & Flow.",
                levelLabelPt = "Intermediário",
                levelLabelEn = "Intermediate",
                primaryVideoUrl = "https://developer.android.com/courses",
                primaryVideoTitlePt = "Abrir Cursos Android Kotlin",
                primaryVideoTitleEn = "Open Android Kotlin Courses",
                lessons = listOf(
                    CourseLesson(
                        id = "kt_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Data Class, Sealed Interface e Coleções",
                        titleEn = "Data Classes, Sealed Interfaces & Collections",
                        summaryPt = "Modele estados Loading/Success/Error com sealed interface e copy() imutável.",
                        summaryEn = "Model Loading/Success/Error UI states with sealed interfaces and immutable copy().",
                        durationMinutes = 30,
                        topicsPt = listOf("data class + copy()", "sealed interface", "map, filter, groupBy"),
                        topicsEn = listOf("data class + copy()", "sealed interface", "Collection operators"),
                        videoRefUrl = "https://kotlinlang.org/docs/kotlin-tour-welcome.html",
                        videoRefLabelPt = "Classes & Sealed (Kotlin Tour)",
                        videoRefLabelEn = "Classes & Sealed (Kotlin Tour)"
                    ),
                    CourseLesson(
                        id = "kt_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Coroutines, suspend fun e StateFlow",
                        titleEn = "Coroutines, suspend fun & StateFlow",
                        summaryPt = "Execute chamadas assíncronas sem travar a Main Thread usando viewModelScope.",
                        summaryEn = "Run async tasks off the main thread cleanly with viewModelScope and StateFlow.",
                        durationMinutes = 35,
                        topicsPt = listOf("suspend fun e launch", "Dispatchers.IO / Main", "MutableStateFlow"),
                        topicsEn = listOf("suspend fun & launch", "Dispatchers.IO / Main", "MutableStateFlow"),
                        videoRefUrl = "https://developer.android.com/courses",
                        videoRefLabelPt = "Coroutines na Android Academy",
                        videoRefLabelEn = "Coroutines on Android Developers"
                    )
                )
            )
        )
    )

    fun buildGoCurriculum() = TechCourseCurriculum(
        tech = Technology.GO,
        references = listOf(
            CourseReference(
                id = "go_ref_aprendago",
                titlePt = "Aprenda Go — Curso Completo em Português (Ellen Körbes)",
                titleEn = "Aprenda Go — Full Portuguese Course (Ellen Körbes)",
                provider = "YouTube • Aprenda Go",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLCKpcjBB_VlBsxJ9IseNxFllf-UFEXOdg",
                durationLabel = "170+ vídeos",
                descriptionPt = "O curso livre mais famoso de Go em português: do zero até concorrência, testes e exercícios práticos.",
                descriptionEn = "Renowned free Portuguese Go playlist from fundamentals to concurrency, channels, and testing."
            ),
            CourseReference(
                id = "go_ref_fcc",
                titlePt = "freeCodeCamp — Learn Go Programming (6h40)",
                titleEn = "freeCodeCamp — Learn Go Programming (6h40)",
                provider = "YouTube • freeCodeCamp.org",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=YS4e4q9oBaU",
                durationLabel = "6h 40m",
                descriptionPt = "Curso prático passo a passo cobrindo slices, structs, ponteiros, interfaces, goroutines e channels.",
                descriptionEn = "Step-by-step full course covering slices, structs, pointers, interfaces, goroutines, and channels."
            ),
            CourseReference(
                id = "go_ref_tour",
                titlePt = "A Tour of Go — Tutorial Oficial Interativo",
                titleEn = "A Tour of Go — Official Interactive Tour",
                provider = "go.dev",
                isPortuguese = false,
                type = CourseResourceType.OFFICIAL_DOCS,
                url = "https://go.dev/tour/welcome/1",
                durationLabel = "Interativo Oficial",
                descriptionPt = "Ambiente oficial do Google para rodar código Go direto no navegador módulo por módulo.",
                descriptionEn = "Official interactive Go playground organized into structured learning modules."
            ),
            CourseReference(
                id = "go_ref_cev_algo",
                titlePt = "Curso em Vídeo — Algoritmos & Lógica (Guanabara)",
                titleEn = "Curso em Vídeo — Algorithms & Logic (Guanabara)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dmSj0MHol_aoNYCSGFqvfXV",
                durationLabel = "Base PT-BR",
                descriptionPt = "Fundamentos de algoritmos, laços e lógica com o Prof. Guanabara para consolidar a base antes de Go.",
                descriptionEn = "Foundational algorithms and logic playlist by Gustavo Guanabara."
            ),
            CourseReference(
                id = "go_ref_cev_site",
                titlePt = "Curso em Vídeo — Site Oficial de Cursos Gratuitos",
                titleEn = "Curso em Vídeo — Official Free Courses Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Site Oficial",
                descriptionPt = "Portal gratuito do Curso em Vídeo com dezenas de trilhas e tutoriais em português.",
                descriptionEn = "Free Curso em Vídeo portal with dozens of structured Portuguese tracks."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "go_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Sintaxe Limpa, Slices, Maps e Structs",
                titleEn = "Module 1: Clean Syntax, Slices, Maps & Structs",
                objectivePt = "Escrever programas Go idiomáticos com inferência := e estruturas de dados nativas.",
                objectiveEn = "Write idiomatic Go using := declarations, slices, maps, and structs.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLCKpcjBB_VlBsxJ9IseNxFllf-UFEXOdg",
                primaryVideoTitlePt = "Assistir Aprenda Go (PT-BR)",
                primaryVideoTitleEn = "Watch Aprenda Go Playlist",
                lessons = listOf(
                    CourseLesson(
                        id = "go_m1_l1",
                        lessonNumber = 1,
                        titlePt = "Pacotes, := e Único Laço for",
                        titleEn = "Packages, := & Go's Single for Loop",
                        summaryPt = "Domine declaração curta :=, retornos múltiplos e iteração com for range.",
                        summaryEn = "Master short declarations :=, multiple return values, and for-range loops.",
                        durationMinutes = 25,
                        topicsPt = listOf("package main & fmt", "Operador marmota :=", "for i := range slice"),
                        topicsEn = listOf("package main & fmt", "Short declaration :=", "for range loops"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLCKpcjBB_VlBsxJ9IseNxFllf-UFEXOdg",
                        videoRefLabelPt = "Capítulos 1–6 (Aprenda Go)",
                        videoRefLabelEn = "Chapters 1–6 (Aprenda Go)"
                    ),
                    CourseLesson(
                        id = "go_m1_l2",
                        lessonNumber = 2,
                        titlePt = "Slices, Maps, Structs e Tratamento de Erros",
                        titleEn = "Slices, Maps, Structs & Explicit Errors",
                        summaryPt = "Use append(), make(), structs com métodos e checagem explícita if err != nil.",
                        summaryEn = "Work with append(), make(), struct methods, and idiomatic if err != nil checks.",
                        durationMinutes = 35,
                        topicsPt = listOf("Slice vs Array", "map[string]int", "if err != nil"),
                        topicsEn = listOf("Slices & append()", "Maps", "Idiomatic error handling"),
                        videoRefUrl = "https://go.dev/tour/welcome/1",
                        videoRefLabelPt = "Praticar no A Tour of Go",
                        videoRefLabelEn = "Practice on A Tour of Go"
                    )
                )
            ),
            CourseModule(
                id = "go_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Interfaces, Goroutines e Channels",
                titleEn = "Module 2: Interfaces, Goroutines & Channels",
                objectivePt = "Construir sistemas concorrentes leves e APIs HTTP rápidas em Go.",
                objectiveEn = "Build lightweight concurrent pipelines and fast HTTP services in Go.",
                levelLabelPt = "Intermediário / Avançado",
                levelLabelEn = "Intermediate / Advanced",
                primaryVideoUrl = "https://www.youtube.com/watch?v=YS4e4q9oBaU",
                primaryVideoTitlePt = "Assistir Concorrência em Go",
                primaryVideoTitleEn = "Watch Go Concurrency",
                lessons = listOf(
                    CourseLesson(
                        id = "go_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Ponteiros e Interfaces Implícitas",
                        titleEn = "Pointers & Implicit Interfaces",
                        summaryPt = "Entenda receivers por ponteiro (*T) e polimorfismo automático sem keyword implements.",
                        summaryEn = "Use pointer receivers (*T) and implicit interface satisfaction cleanly.",
                        durationMinutes = 30,
                        topicsPt = listOf("Ponteiros & e *", "Receiver (u *User)", "Interfaces pequenas"),
                        topicsEn = listOf("Pointers & and *", "Pointer receivers", "Small interfaces"),
                        videoRefUrl = "https://www.youtube.com/watch?v=YS4e4q9oBaU",
                        videoRefLabelPt = "Interfaces (freeCodeCamp)",
                        videoRefLabelEn = "Interfaces (freeCodeCamp)"
                    ),
                    CourseLesson(
                        id = "go_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Goroutines, Channels e sync.WaitGroup",
                        titleEn = "Goroutines, Channels & sync.WaitGroup",
                        summaryPt = "Dispare rotinas concorrentes com go func() e comunique dados com segurança via chan.",
                        summaryEn = "Spawn concurrent workers with go func() and synchronize safely via channels.",
                        durationMinutes = 40,
                        topicsPt = listOf("go minhaFunc()", "chan e select", "sync.WaitGroup"),
                        topicsEn = listOf("go keyword", "Channels & select", "sync.WaitGroup"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLCKpcjBB_VlBsxJ9IseNxFllf-UFEXOdg",
                        videoRefLabelPt = "Concorrência (Aprenda Go)",
                        videoRefLabelEn = "Concurrency (Aprenda Go)"
                    )
                )
            )
        )
    )

    fun buildMySqlCurriculum() = TechCourseCurriculum(
        tech = Technology.MYSQL,
        references = listOf(
            CourseReference(
                id = "mysql_ref_cev",
                titlePt = "Curso em Vídeo — Banco de Dados MySQL (Guanabara)",
                titleEn = "Curso em Vídeo — MySQL Database Course (Full)",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkBsANmRuSNnETnERnGRvAM",
                durationLabel = "16 aulas",
                descriptionPt = "Playlist completa em português cobrindo modelagem relacional, DDL, DML, SELECT avançado e JOINs no MySQL.",
                descriptionEn = "Full Portuguese playlist covering relational modeling, DDL, DML, advanced SELECT, and JOINs in MySQL."
            ),
            CourseReference(
                id = "mysql_ref_cev_site",
                titlePt = "Curso em Vídeo — Portal Oficial de Cursos MySQL",
                titleEn = "Curso em Vídeo — Official MySQL Course Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Site Oficial",
                descriptionPt = "Acesse o curso gratuito de Banco de Dados MySQL com material de apoio no site oficial Curso em Vídeo.",
                descriptionEn = "Access the free MySQL Database course and study notes on the Curso em Vídeo website."
            ),
            CourseReference(
                id = "mysql_ref_fcc",
                titlePt = "freeCodeCamp — SQL & MySQL Full Course (4h20)",
                titleEn = "freeCodeCamp — SQL & MySQL Full Course (4h20)",
                provider = "YouTube • freeCodeCamp.org",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=HXV3zeQKqGY",
                durationLabel = "4h 20m",
                descriptionPt = "Tutorial prático usando MySQL passo a passo com modelagem de empresa real.",
                descriptionEn = "Hands-on MySQL full database tutorial building and querying a real company schema."
            ),
            CourseReference(
                id = "mysql_ref_w3",
                titlePt = "W3Schools — MySQL Tutorial Interativo",
                titleEn = "W3Schools — Interactive MySQL Tutorial",
                provider = "w3schools.com",
                isPortuguese = false,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.w3schools.com/mysql/",
                durationLabel = "Guia + Editor",
                descriptionPt = "Referência rápida e gratuita de comandos MySQL com exemplos executáveis.",
                descriptionEn = "Fast, free MySQL command reference with live interactive try-it examples."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "mysql_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Modelagem, DDL e DML no MySQL",
                titleEn = "Module 1: Modeling, DDL & DML in MySQL",
                objectivePt = "Criar bancos, tabelas InnoDB com UTF8MB4 e popular registros corretamente.",
                objectiveEn = "Create InnoDB UTF8MB4 schemas and populate tables safely.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkBsANmRuSNnETnERnGRvAM",
                primaryVideoTitlePt = "Assistir Curso de MySQL",
                primaryVideoTitleEn = "Watch MySQL Course",
                lessons = listOf(
                    CourseLesson(
                        id = "mysql_m1_l1",
                        lessonNumber = 1,
                        titlePt = "CREATE DATABASE, Tipos e AUTO_INCREMENT",
                        titleEn = "CREATE DATABASE, Types & AUTO_INCREMENT",
                        summaryPt = "Defina chaves primárias INT AUTO_INCREMENT, VARCHAR, DECIMAL e UTF8MB4.",
                        summaryEn = "Configure primary keys with AUTO_INCREMENT, VARCHAR, DECIMAL, and UTF8MB4.",
                        durationMinutes = 25,
                        topicsPt = listOf("utf8mb4_general_ci", "PRIMARY KEY AUTO_INCREMENT", "ALTER TABLE"),
                        topicsEn = listOf("UTF8MB4 encoding", "AUTO_INCREMENT keys", "ALTER TABLE"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkBsANmRuSNnETnERnGRvAM",
                        videoRefLabelPt = "Aulas 1–6 (Curso em Vídeo)",
                        videoRefLabelEn = "Lessons 1–6 (Curso em Vídeo)"
                    ),
                    CourseLesson(
                        id = "mysql_m1_l2",
                        lessonNumber = 2,
                        titlePt = "Relacionamentos 1:N, N:N e FOREIGN KEY",
                        titleEn = "1:N, N:N Relationships & FOREIGN KEY",
                        summaryPt = "Implemente chaves estrangeiras e tabelas associativas para relacionamentos muitos-para-muitos.",
                        summaryEn = "Implement foreign keys and junction tables for many-to-many schemas.",
                        durationMinutes = 30,
                        topicsPt = listOf("Cardinais 1:N e N:N", "CONSTRAINT FOREIGN KEY", "INNER / LEFT JOIN"),
                        topicsEn = listOf("1:N & N:N cardinality", "FOREIGN KEY constraints", "INNER / LEFT JOIN"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkBsANmRuSNnETnERnGRvAM",
                        videoRefLabelPt = "Aulas de JOIN (Curso em Vídeo)",
                        videoRefLabelEn = "JOIN Lessons (Curso em Vídeo)"
                    )
                )
            ),
            CourseModule(
                id = "mysql_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Índices, Otimização e Backups",
                titleEn = "Module 2: Indexes, Tuning & Backups",
                objectivePt = "Acelerar buscas em produção usando EXPLAIN e índices compostos.",
                objectiveEn = "Accelerate production queries using EXPLAIN and composite indexes.",
                levelLabelPt = "Prático",
                levelLabelEn = "Practical",
                primaryVideoUrl = "https://www.w3schools.com/mysql/",
                primaryVideoTitlePt = "Praticar Comandos no W3Schools",
                primaryVideoTitleEn = "Practice Commands on W3Schools",
                lessons = listOf(
                    CourseLesson(
                        id = "mysql_m2_l1",
                        lessonNumber = 1,
                        titlePt = "CREATE INDEX e Análise com EXPLAIN",
                        titleEn = "CREATE INDEX & Query Analysis with EXPLAIN",
                        summaryPt = "Identifique Full Table Scans e otimize filtros frequentes com índices adequados.",
                        summaryEn = "Spot full table scans and optimize frequent filters using targeted indexes.",
                        durationMinutes = 30,
                        topicsPt = listOf("EXPLAIN SELECT", "Índices compostos", "Paginação eficiente"),
                        topicsEn = listOf("EXPLAIN SELECT", "Composite indexes", "Efficient pagination"),
                        videoRefUrl = "https://www.w3schools.com/mysql/",
                        videoRefLabelPt = "Referência MySQL W3Schools",
                        videoRefLabelEn = "W3Schools MySQL Guide"
                    ),
                    CourseLesson(
                        id = "mysql_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Views, Stored Procedures e Dump Seguro",
                        titleEn = "Views, Stored Procedures & Safe Backups",
                        summaryPt = "Encapsule relatórios em VIEWs e automatize backups consistentes com mysqldump.",
                        summaryEn = "Encapsulate reusable reports in VIEWs and run consistent mysqldump backups.",
                        durationMinutes = 25,
                        topicsPt = listOf("CREATE VIEW", "Transações InnoDB", "mysqldump"),
                        topicsEn = listOf("CREATE VIEW", "InnoDB transactions", "mysqldump"),
                        videoRefUrl = "https://www.youtube.com/watch?v=HXV3zeQKqGY",
                        videoRefLabelPt = "Curso Completo (freeCodeCamp)",
                        videoRefLabelEn = "Full Course (freeCodeCamp)"
                    )
                )
            )
        )
    )

    fun buildPostgresCurriculum() = TechCourseCurriculum(
        tech = Technology.POSTGRESQL,
        references = listOf(
            CourseReference(
                id = "pg_ref_cev_sql",
                titlePt = "Curso em Vídeo — Fundamentos SQL & Relacional (Guanabara)",
                titleEn = "Curso em Vídeo — Relational SQL Foundations",
                provider = "YouTube • Curso em Vídeo (Guanabara)",
                isPortuguese = true,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PLHz_AreHm4dkBsANmRuSNnETnERnGRvAM",
                durationLabel = "16 aulas • PT-BR",
                descriptionPt = "Base relacional completa em português com o Prof. Guanabara (SELECT, JOIN, chaves estrangeiras e modelagem).",
                descriptionEn = "Relational SQL fundamentals in Portuguese with Gustavo Guanabara before diving into PostgreSQL."
            ),
            CourseReference(
                id = "pg_ref_cev_site",
                titlePt = "Curso em Vídeo — Site Oficial de Cursos Gratuitos",
                titleEn = "Curso em Vídeo — Official Free Courses Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Site Oficial",
                descriptionPt = "Acesse gratuitamente os cursos de Banco de Dados e programação no site do Curso em Vídeo.",
                descriptionEn = "Browse free Database and programming courses on the Curso em Vídeo site."
            ),
            CourseReference(
                id = "pg_ref_fcc",
                titlePt = "freeCodeCamp — Learn PostgreSQL Full Course (4h19)",
                titleEn = "freeCodeCamp — Learn PostgreSQL Full Course (4h19)",
                provider = "YouTube • freeCodeCamp / Amigoscode",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=qw--VYLpxG4",
                durationLabel = "4h 19m",
                descriptionPt = "Curso completo do zero ao avançado cobrindo psql, constraints, UUID, joins, agregações e datas no Postgres.",
                descriptionEn = "Full beginner-to-advanced course covering psql, constraints, UUIDs, joins, aggregates, and dates."
            ),
            CourseReference(
                id = "pg_ref_tutorial",
                titlePt = "PostgreSQL Tutorial — Guia Passo a Passo Gratuito",
                titleEn = "PostgreSQL Tutorial — Free Step-by-Step Guide",
                provider = "postgresqltutorial.com",
                isPortuguese = false,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.postgresqltutorial.com/",
                durationLabel = "Site Completo",
                descriptionPt = "Portal gratuito super organizado com exemplos práticos de JSONB, CTEs, Window Functions e índices.",
                descriptionEn = "Well-structured free portal with practical guides for JSONB, CTEs, Window Functions, and indexes."
            ),
            CourseReference(
                id = "pg_ref_sqlbolt",
                titlePt = "SQLBolt — Treino Interativo de SQL Padrão",
                titleEn = "SQLBolt — Standard SQL Interactive Practice",
                provider = "sqlbolt.com",
                isPortuguese = false,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://sqlbolt.com/",
                durationLabel = "Interativo",
                descriptionPt = "Pratique imediatamente a base SQL padrão que alimenta todas as consultas do PostgreSQL.",
                descriptionEn = "Practice the core standard SQL syntax powering all PostgreSQL queries."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "pg_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Fundamentos Postgres, psql e Tipos Ricos",
                titleEn = "Module 1: Postgres Basics, psql & Rich Types",
                objectivePt = "Trabalhar com SERIAL/IDENTITY, UUID, TIMESTAMPTZ e retorno RETURNING.",
                objectiveEn = "Use IDENTITY/SERIAL, UUID, TIMESTAMPTZ, and RETURNING clauses.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/watch?v=qw--VYLpxG4",
                primaryVideoTitlePt = "Assistir Curso PostgreSQL",
                primaryVideoTitleEn = "Watch PostgreSQL Course",
                lessons = listOf(
                    CourseLesson(
                        id = "pg_m1_l1",
                        lessonNumber = 1,
                        titlePt = "psql CLI, Schemas e Tipos UUID / TIMESTAMPTZ",
                        titleEn = "psql CLI, Schemas & UUID / TIMESTAMPTZ",
                        summaryPt = "Armazene datas com fuso horário e recupere IDs inseridos com INSERT ... RETURNING *.",
                        summaryEn = "Store timezone-aware timestamps and fetch inserted rows via RETURNING *.",
                        durationMinutes = 30,
                        topicsPt = listOf("Comandos \\dt e \\d", "UUID & GENERATED ALWAYS", "RETURNING id"),
                        topicsEn = listOf("\\dt & \\d commands", "UUID & IDENTITY", "RETURNING id"),
                        videoRefUrl = "https://www.youtube.com/watch?v=qw--VYLpxG4",
                        videoRefLabelPt = "Introdução Postgres (YouTube)",
                        videoRefLabelEn = "Postgres Intro (YouTube)"
                    ),
                    CourseLesson(
                        id = "pg_m1_l2",
                        lessonNumber = 2,
                        titlePt = "ILIKE, ON CONFLICT (Upsert) e Joins",
                        titleEn = "ILIKE, ON CONFLICT (Upsert) & Joins",
                        summaryPt = "Faça buscas case-insensitive com ILIKE e atualize conflitos sem erro usando UPSERT.",
                        summaryEn = "Run case-insensitive searches with ILIKE and handle duplicates with ON CONFLICT.",
                        durationMinutes = 30,
                        topicsPt = listOf("ILIKE case-insensitive", "ON CONFLICT DO UPDATE", "COALESCE"),
                        topicsEn = listOf("ILIKE search", "ON CONFLICT DO UPDATE", "COALESCE"),
                        videoRefUrl = "https://www.postgresqltutorial.com/",
                        videoRefLabelPt = "Lições PostgresTutorial.com",
                        videoRefLabelEn = "PostgresTutorial.com Lessons"
                    )
                )
            ),
            CourseModule(
                id = "pg_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: JSONB, Window Functions e EXPLAIN ANALYZE",
                titleEn = "Module 2: JSONB, Window Functions & EXPLAIN ANALYZE",
                objectivePt = "Combinar poder relacional com documentos JSONB e análises avançadas OVER().",
                objectiveEn = "Combine relational rigor with JSONB documents and analytical Window Functions.",
                levelLabelPt = "Avançado",
                levelLabelEn = "Advanced",
                primaryVideoUrl = "https://www.postgresqltutorial.com/",
                primaryVideoTitlePt = "Explorar JSONB & Window Functions",
                primaryVideoTitleEn = "Explore JSONB & Window Functions",
                lessons = listOf(
                    CourseLesson(
                        id = "pg_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Armazenamento e Consulta JSONB + Índices GIN",
                        titleEn = "JSONB Queries & GIN Indexing",
                        summaryPt = "Consulte chaves internas com ->, ->> e @> mantendo velocidade com índices GIN.",
                        summaryEn = "Query nested JSON keys with ->, ->>, and @> accelerated by GIN indexes.",
                        durationMinutes = 35,
                        topicsPt = listOf("Operadores -> e ->>", "Contenção @>", "CREATE INDEX USING GIN"),
                        topicsEn = listOf("-> and ->> operators", "@> containment", "GIN Indexes"),
                        videoRefUrl = "https://www.postgresqltutorial.com/",
                        videoRefLabelPt = "Guia JSONB Postgres",
                        videoRefLabelEn = "Postgres JSONB Guide"
                    ),
                    CourseLesson(
                        id = "pg_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Window Functions (ROW_NUMBER, RANK, OVER)",
                        titleEn = "Window Functions (ROW_NUMBER, RANK, OVER)",
                        summaryPt = "Calcule rankings e médias móveis por partição sem colapsar as linhas originais.",
                        summaryEn = "Compute rankings and running averages per partition without collapsing rows.",
                        durationMinutes = 35,
                        topicsPt = listOf("OVER (PARTITION BY)", "ROW_NUMBER() e DENSE_RANK()", "EXPLAIN ANALYZE"),
                        topicsEn = listOf("OVER (PARTITION BY)", "ROW_NUMBER & DENSE_RANK", "EXPLAIN ANALYZE"),
                        videoRefUrl = "https://www.youtube.com/watch?v=qw--VYLpxG4",
                        videoRefLabelPt = "Módulo Avançado (freeCodeCamp)",
                        videoRefLabelEn = "Advanced Module (freeCodeCamp)"
                    )
                )
            )
        )
    )

    fun buildMongoCurriculum() = TechCourseCurriculum(
        tech = Technology.MONGODB,
        references = listOf(
            CourseReference(
                id = "mongo_ref_ninja",
                titlePt = "The Net Ninja — Playlist Completa de MongoDB",
                titleEn = "The Net Ninja — Complete MongoDB Tutorial",
                provider = "YouTube • Net Ninja",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_PLAYLIST,
                url = "https://www.youtube.com/playlist?list=PL4cUxeGkcC9h77dJ-QJlwGlZlTd4ecZOA",
                durationLabel = "16 aulas",
                descriptionPt = "Playlist muito clara e prática cobrindo coleções, documentos, CRUD, operadores e índices no MongoDB.",
                descriptionEn = "Clear, practical playlist covering collections, documents, CRUD, query operators, and indexes."
            ),
            CourseReference(
                id = "mongo_ref_university",
                titlePt = "MongoDB University — Cursos Oficiais Gratuitos",
                titleEn = "MongoDB University — Free Official Courses",
                provider = "learn.mongodb.com",
                isPortuguese = false,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://learn.mongodb.com/",
                durationLabel = "Gratuito + Cert.",
                descriptionPt = "Plataforma oficial da MongoDB com trilhas gratuitas de modelagem de documentos, Aggregation e Atlas.",
                descriptionEn = "Official MongoDB learning platform with free courses on schema design, Aggregation, and Atlas."
            ),
            CourseReference(
                id = "mongo_ref_fcc",
                titlePt = "freeCodeCamp — MongoDB Crash Course",
                titleEn = "freeCodeCamp — MongoDB Crash Course",
                provider = "YouTube • freeCodeCamp.org",
                isPortuguese = false,
                type = CourseResourceType.YOUTUBE_VIDEO,
                url = "https://www.youtube.com/watch?v=c2hNfwjwAZI",
                durationLabel = "Aula Completa",
                descriptionPt = "Guia prático direto ao ponto para modelar documentos NoSQL e integrá-los em aplicações reais.",
                descriptionEn = "Hands-on crash course on NoSQL document modeling and real application integration."
            ),
            CourseReference(
                id = "mongo_ref_cev_site",
                titlePt = "Curso em Vídeo — Base de Dados & Lógica (Portal Oficial)",
                titleEn = "Curso em Vídeo — Database & Logic Free Portal",
                provider = "cursoemvideo.com • Gustavo Guanabara",
                isPortuguese = true,
                type = CourseResourceType.FREE_COURSE_SITE,
                url = "https://www.cursoemvideo.com/cursos/",
                durationLabel = "Site Oficial",
                descriptionPt = "Reforce modelagem de dados, lógica e Python/JS no site gratuito do Curso em Vídeo para integrar com NoSQL.",
                descriptionEn = "Strengthen data modeling, logic, and backend basics on the free Curso em Vídeo portal."
            )
        ),
        modules = listOf(
            CourseModule(
                id = "mongo_mod_1",
                moduleNumber = 1,
                titlePt = "Módulo 1: Documentos BSON e CRUD no mongosh",
                titleEn = "Module 1: BSON Documents & CRUD in mongosh",
                objectivePt = "Modelar documentos flexíveis e executar buscas com operadores NoSQL.",
                objectiveEn = "Model flexible JSON/BSON documents and run NoSQL CRUD queries.",
                levelLabelPt = "Iniciante",
                levelLabelEn = "Beginner",
                primaryVideoUrl = "https://www.youtube.com/playlist?list=PL4cUxeGkcC9h77dJ-QJlwGlZlTd4ecZOA",
                primaryVideoTitlePt = "Assistir Playlist MongoDB",
                primaryVideoTitleEn = "Watch MongoDB Playlist",
                lessons = listOf(
                    CourseLesson(
                        id = "mongo_m1_l1",
                        lessonNumber = 1,
                        titlePt = "Coleções, ObjectId e insertOne / find",
                        titleEn = "Collections, ObjectId & insertOne / find",
                        summaryPt = "Compare tabelas SQL vs coleções de documentos e filtre com operadores \$gt e \$in.",
                        summaryEn = "Compare SQL tables vs document collections and filter with \$gt, \$lt, and \$in.",
                        durationMinutes = 25,
                        topicsPt = listOf("insertOne / insertMany", "find() com projeção", "\$gt, \$gte, \$in"),
                        topicsEn = listOf("insertOne / insertMany", "find() & projections", "\$gt, \$gte, \$in"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PL4cUxeGkcC9h77dJ-QJlwGlZlTd4ecZOA",
                        videoRefLabelPt = "Aulas 1–8 (Net Ninja)",
                        videoRefLabelEn = "Lessons 1–8 (Net Ninja)"
                    ),
                    CourseLesson(
                        id = "mongo_m1_l2",
                        lessonNumber = 2,
                        titlePt = "updateOne (\$set, \$push) e Embedding vs Referencing",
                        titleEn = "updateOne (\$set, \$push) & Data Modeling",
                        summaryPt = "Decida quando embutir subdocumentos e quando referenciar por ObjectId.",
                        summaryEn = "Decide when to embed subdocuments vs reference across collections.",
                        durationMinutes = 30,
                        topicsPt = listOf("Operadores \$set e \$inc", "Arrays com \$push/\$pull", "Modelagem Embedding vs Ref"),
                        topicsEn = listOf("\$set & \$inc updates", "Array \$push/\$pull", "Embed vs Reference"),
                        videoRefUrl = "https://learn.mongodb.com/",
                        videoRefLabelPt = "Modelagem na MongoDB University",
                        videoRefLabelEn = "Data Modeling on MongoDB Univ."
                    )
                )
            ),
            CourseModule(
                id = "mongo_mod_2",
                moduleNumber = 2,
                titlePt = "Módulo 2: Aggregation Pipeline e Índices",
                titleEn = "Module 2: Aggregation Pipeline & Indexes",
                objectivePt = "Construir relatórios analíticos e otimizar leituras em escala.",
                objectiveEn = "Build analytical pipelines and optimize read scalability.",
                levelLabelPt = "Intermediário",
                levelLabelEn = "Intermediate",
                primaryVideoUrl = "https://learn.mongodb.com/",
                primaryVideoTitlePt = "Estudar Aggregation (Grátis)",
                primaryVideoTitleEn = "Study Aggregation (Free)",
                lessons = listOf(
                    CourseLesson(
                        id = "mongo_m2_l1",
                        lessonNumber = 1,
                        titlePt = "Pipeline: \$match, \$group, \$sort e \$lookup",
                        titleEn = "Pipeline: \$match, \$group, \$sort & \$lookup",
                        summaryPt = "Filtre antes de agrupar para performance máxima e junte coleções com \$lookup.",
                        summaryEn = "Filter early with \$match, aggregate with \$group, and join with \$lookup.",
                        durationMinutes = 35,
                        topicsPt = listOf("Estágio \$match inicial", "\$group com \$sum/\$avg", "\$lookup e \$unwind"),
                        topicsEn = listOf("Early \$match stage", "\$group \$sum/\$avg", "\$lookup & \$unwind"),
                        videoRefUrl = "https://learn.mongodb.com/",
                        videoRefLabelPt = "Curso Aggregation (MongoDB)",
                        videoRefLabelEn = "Aggregation Course (MongoDB)"
                    ),
                    CourseLesson(
                        id = "mongo_m2_l2",
                        lessonNumber = 2,
                        titlePt = "Índices Simples, Compostos e explain('executionStats')",
                        titleEn = "Single, Compound Indexes & explain()",
                        summaryPt = "Elimine COLLSCAN criando índices alinhados à regra ESR (Equality, Sort, Range).",
                        summaryEn = "Eliminate COLLSCAN using compound indexes following the ESR rule.",
                        durationMinutes = 30,
                        topicsPt = listOf("createIndex({campo: 1})", "Regra ESR", "IXSCAN vs COLLSCAN"),
                        topicsEn = listOf("createIndex", "ESR indexing rule", "IXSCAN vs COLLSCAN"),
                        videoRefUrl = "https://www.youtube.com/playlist?list=PL4cUxeGkcC9h77dJ-QJlwGlZlTd4ecZOA",
                        videoRefLabelPt = "Índices na Playlist MongoDB",
                        videoRefLabelEn = "Indexes in MongoDB Playlist"
                    )
                )
            )
        )
    )
}
