package com.example.model

import android.content.SharedPreferences

enum class IdleExecutionStatus {
    READY,
    RUNNING,
    SUCCESS,
    ERROR
}

data class IdleCodeTemplate(
    val id: String,
    val titlePt: String,
    val titleEn: String,
    val code: String
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
}

data class IdleExecutionResult(
    val tech: Technology,
    val status: IdleExecutionStatus = IdleExecutionStatus.READY,
    val outputLines: List<String> = emptyList(),
    val tableColumns: List<String> = emptyList(),
    val tableRows: List<List<String>> = emptyList(),
    val htmlPreviewDocument: String? = null,
    val errorMessage: String? = null,
    val executionTimeMs: Long = 0L
)

data class LanguageIdleSpec(
    val tech: Technology,
    val idleTitlePt: String,
    val idleTitleEn: String,
    val defaultFileName: String,
    val runtimeBadge: String,
    val quickTokens: List<String>,
    val templates: List<IdleCodeTemplate>,
    val supportsHtmlPreview: Boolean = false,
    val supportsTableOutput: Boolean = false
) {
    fun idleTitle(language: Language): String = if (language == Language.PT) idleTitlePt else idleTitleEn
}

object LanguageIdleCodeStorage {
    private const val KEY_PREFIX = "idle_saved_code_"

    fun loadCode(prefs: SharedPreferences, tech: Technology): String {
        val saved = prefs.getString("$KEY_PREFIX${tech.id}", null)
        if (!saved.isNullOrBlank()) return saved
        return LanguageIdleCatalog.getSpec(tech).templates.first().code
    }

    fun saveCode(prefs: SharedPreferences, tech: Technology, code: String) {
        prefs.edit().putString("$KEY_PREFIX${tech.id}", code).apply()
    }

    fun resetToDefault(prefs: SharedPreferences, tech: Technology): String {
        prefs.edit().remove("$KEY_PREFIX${tech.id}").apply()
        return LanguageIdleCatalog.getSpec(tech).templates.first().code
    }
}

object LanguageIdleCatalog {
    val specs: Map<Technology, LanguageIdleSpec> = mapOf(
        Technology.PYTHON to LanguageIdleSpec(
            tech = Technology.PYTHON,
            idleTitlePt = "Python IDLE",
            idleTitleEn = "Python IDLE",
            defaultFileName = "main.py",
            runtimeBadge = "Python 3.12",
            quickTokens = listOf("print()", "def ", "for ", "in range()", "if ", ":", "[]", "\"\""),
            templates = listOf(
                IdleCodeTemplate(
                    id = "py_basics",
                    titlePt = "Variáveis & Loop",
                    titleEn = "Variables & Loop",
                    code = """
                        nome = "Dev"
                        metas = [10, 20, 30, 40]
                        print(f"Olá, {nome}! Bem-vindo ao Python IDLE")

                        total = 0
                        for i in range(1, 6):
                            quadrado = i * i
                            total = total + quadrado
                            print(f"{i}² = {quadrado}")

                        print(f"Soma dos quadrados: {total}")
                        print(f"Total de metas: {len(metas)}")
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "py_func",
                    titlePt = "Função Recursiva",
                    titleEn = "Function & Math",
                    code = """
                        def calcular_xp(desafios, bonus):
                            base = desafios * 30
                            return base + bonus

                        def status_nivel(xp):
                            if xp >= 250:
                                return "Bronze I desbloqueado!"
                            return "Continue praticando!"

                        meu_xp = calcular_xp(8, 40)
                        print(f"XP total: {meu_xp}")
                        print(status_nivel(meu_xp))
                    """.trimIndent()
                )
            )
        ),
        Technology.JAVASCRIPT to LanguageIdleSpec(
            tech = Technology.JAVASCRIPT,
            idleTitlePt = "JavaScript IDLE",
            idleTitleEn = "JavaScript IDLE",
            defaultFileName = "app.js",
            runtimeBadge = "ES2024 REPL",
            quickTokens = listOf("console.log()", "const ", "let ", "=>", "function ", "{}", "[]", ";"),
            templates = listOf(
                IdleCodeTemplate(
                    id = "js_basics",
                    titlePt = "ES6+ & Funções",
                    titleEn = "ES6+ & Functions",
                    code = """
                        const linguagem = "JavaScript";
                        const notas = [8, 9, 10];
                        console.log(`Executando ${'$'}{linguagem} IDLE!`);

                        function media(arr) {
                          let soma = 0;
                          for (let i = 0; i < arr.length; i++) {
                            soma = soma + arr[i];
                          }
                          return soma / arr.length;
                        }

                        const resultado = media(notas);
                        console.log("Notas:", notas);
                        console.log("Média calculada:", resultado);
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "js_loop",
                    titlePt = "Sequência Fibonacci",
                    titleEn = "Fibonacci Sequence",
                    code = """
                        let a = 0;
                        let b = 1;
                        console.log("Primeiros 8 números de Fibonacci:");
                        for (let i = 0; i < 8; i++) {
                          console.log(`F(${'$'}{i}) = ${'$'}{a}`);
                          const prox = a + b;
                          a = b;
                          b = prox;
                        }
                    """.trimIndent()
                )
            )
        ),
        Technology.TYPESCRIPT to LanguageIdleSpec(
            tech = Technology.TYPESCRIPT,
            idleTitlePt = "TypeScript IDLE",
            idleTitleEn = "TypeScript IDLE",
            defaultFileName = "index.ts",
            runtimeBadge = "TS 5.x Safe",
            quickTokens = listOf("console.log()", ": number", ": string", "interface ", "const ", "=>", "{}", ";"),
            templates = listOf(
                IdleCodeTemplate(
                    id = "ts_types",
                    titlePt = "Tipagem & Funções",
                    titleEn = "Types & Functions",
                    code = """
                        interface Estudo {
                          modulo: string;
                          minutos: number;
                        }

                        const trilha: string = "TypeScript";
                        const horas: number = 4;

                        function calcularXp(sessoes: number, bonus: number): number {
                          return sessoes * 30 + bonus;
                        }

                        const xp: number = calcularXp(horas, 60);
                        console.log(`Trilha: ${'$'}{trilha} | XP Ganho: ${'$'}{xp}`);
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "ts_loops",
                    titlePt = "Validação Condicional",
                    titleEn = "Typed Conditionals",
                    code = """
                        const limite: number = 100;
                        for (let nivel: number = 1; nivel <= 4; nivel++) {
                          const alvo: number = nivel * 50;
                          if (alvo > limite) {
                            console.log(`Nível ${'$'}{nivel}: meta avançada (${'$'}{alvo} XP)`);
                          } else {
                            console.log(`Nível ${'$'}{nivel}: meta base (${'$'}{alvo} XP)`);
                          }
                        }
                    """.trimIndent()
                )
            )
        ),
        Technology.KOTLIN to LanguageIdleSpec(
            tech = Technology.KOTLIN,
            idleTitlePt = "Kotlin IDLE",
            idleTitleEn = "Kotlin IDLE",
            defaultFileName = "Main.kt",
            runtimeBadge = "Kotlin 2.x",
            quickTokens = listOf("println()", "val ", "var ", "fun ", "for (i in 1..5)", "{}", "\"\$x\"", "->"),
            templates = listOf(
                IdleCodeTemplate(
                    id = "kt_main",
                    titlePt = "Kotlin Básico",
                    titleEn = "Kotlin Basics",
                    code = """
                        fun saudar(dev: String, nivel: Int): String {
                            return "Olá ${'$'}dev! Nível atual: ${'$'}nivel"
                        }

                        fun main() {
                            val linguagem = "Kotlin"
                            var pontos = 0
                            println(saudar("DevRoutine", 3))

                            for (i in 1..5) {
                                pontos += i * 10
                                println("Passo ${'$'}i -> ${'$'}pontos pts (${'$'}linguagem)")
                            }
                            println("Total final: ${'$'}pontos")
                        }
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "kt_when",
                    titlePt = "Funções & Condições",
                    titleEn = "Functions & Branching",
                    code = """
                        fun classificar(xp: Int): String {
                            if (xp >= 500) {
                                return "Pleno em Kotlin"
                            } else if (xp >= 200) {
                                return "Júnior em evolução"
                            }
                            return "Iniciante"
                        }

                        fun main() {
                            val xp = 320
                            println("XP = ${'$'}xp -> ${'$'}{classificar(xp)}")
                        }
                    """.trimIndent()
                )
            )
        ),
        Technology.JAVA to LanguageIdleSpec(
            tech = Technology.JAVA,
            idleTitlePt = "Java IDLE",
            idleTitleEn = "Java IDLE",
            defaultFileName = "Main.java",
            runtimeBadge = "JDK 21",
            quickTokens = listOf("System.out.println()", "int ", "String ", "public static ", "for ()", "{}", ";"),
            templates = listOf(
                IdleCodeTemplate(
                    id = "java_main",
                    titlePt = "Classe & Main",
                    titleEn = "Class & Main",
                    code = """
                        public class Main {
                            static int multiplicar(int a, int b) {
                                return a * b;
                            }

                            public static void main(String[] args) {
                                String curso = "Java OOP";
                                int fator = 7;
                                System.out.println("Iniciando IDLE: " + curso);

                                for (int i = 1; i <= 5; i++) {
                                    int res = multiplicar(i, fator);
                                    System.out.println(i + " x " + fator + " = " + res);
                                }
                            }
                        }
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "java_sum",
                    titlePt = "Acumulador & If",
                    titleEn = "Accumulator & If",
                    code = """
                        public class Main {
                            public static void main(String[] args) {
                                int somaPares = 0;
                                for (int n = 1; n <= 10; n++) {
                                    if (n % 2 == 0) {
                                        somaPares = somaPares + n;
                                    }
                                }
                                System.out.println("Soma dos pares (1..10): " + somaPares);
                            }
                        }
                    """.trimIndent()
                )
            )
        ),
        Technology.GO to LanguageIdleSpec(
            tech = Technology.GO,
            idleTitlePt = "Go IDLE",
            idleTitleEn = "Go IDLE",
            defaultFileName = "main.go",
            runtimeBadge = "Go 1.23",
            quickTokens = listOf("fmt.Println()", ":=", "func ", "for ", "if ", "{}", "\"\""),
            templates = listOf(
                IdleCodeTemplate(
                    id = "go_main",
                    titlePt = "Pacote Main & Slices",
                    titleEn = "Main & Loops",
                    code = """
                        package main

                        import "fmt"

                        func dobrar(n int) int {
                            return n * 2
                        }

                        func main() {
                            nome := "Gopher"
                            fmt.Println("Olá,", nome, "no Go IDLE!")

                            soma := 0
                            for i := 1; i <= 5; i++ {
                                v := dobrar(i)
                                soma = soma + v
                                fmt.Println("i =", i, "-> dobro =", v)
                            }
                            fmt.Println("Soma total:", soma)
                        }
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "go_branch",
                    titlePt = "Condicionais em Go",
                    titleEn = "Go Conditionals",
                    code = """
                        package main

                        func checarPorta(porta int) string {
                            if porta == 443 {
                                return "HTTPS seguro"
                            } else if porta == 80 {
                                return "HTTP padrão"
                            }
                            return "Porta customizada"
                        }

                        func main() {
                            fmt.Println("443:", checarPorta(443))
                            fmt.Println("8080:", checarPorta(8080))
                        }
                    """.trimIndent()
                )
            )
        ),
        Technology.SQL to LanguageIdleSpec(
            tech = Technology.SQL,
            idleTitlePt = "SQL IDLE Console",
            idleTitleEn = "SQL IDLE Console",
            defaultFileName = "query.sql",
            runtimeBadge = "SQLite Engine",
            quickTokens = listOf("SELECT ", "FROM ", "WHERE ", "ORDER BY ", "GROUP BY ", "COUNT(*)", "LIMIT 10", ";"),
            supportsTableOutput = true,
            templates = listOf(
                IdleCodeTemplate(
                    id = "sql_notes",
                    titlePt = "Notas de Estudo",
                    titleEn = "Study Notes Query",
                    code = "SELECT id, techId, title, createdAtMillis FROM study_notes ORDER BY createdAtMillis DESC LIMIT 10;"
                ),
                IdleCodeTemplate(
                    id = "sql_badges",
                    titlePt = "Conquistas & XP",
                    titleEn = "Badges & XP",
                    code = "SELECT id, titlePt, category, xpReward, isUnlocked FROM user_badges ORDER BY xpReward DESC LIMIT 12;"
                )
            )
        ),
        Technology.MYSQL to LanguageIdleSpec(
            tech = Technology.MYSQL,
            idleTitlePt = "MySQL IDLE Workbench",
            idleTitleEn = "MySQL IDLE Workbench",
            defaultFileName = "workbench.sql",
            runtimeBadge = "MySQL 8.0",
            quickTokens = listOf("SHOW TABLES;", "DESCRIBE ", "SELECT ", "FROM ", "GROUP BY ", "ORDER BY ", "LIMIT 10;"),
            supportsTableOutput = true,
            templates = listOf(
                IdleCodeTemplate(
                    id = "mysql_show",
                    titlePt = "SHOW TABLES & Agregação",
                    titleEn = "SHOW TABLES & Aggregation",
                    code = "SELECT techId, COUNT(*) AS total_notas FROM study_notes GROUP BY techId ORDER BY total_notas DESC;"
                ),
                IdleCodeTemplate(
                    id = "mysql_tables",
                    titlePt = "Listar Tabelas",
                    titleEn = "List Tables",
                    code = "SHOW TABLES;"
                )
            )
        ),
        Technology.POSTGRESQL to LanguageIdleSpec(
            tech = Technology.POSTGRESQL,
            idleTitlePt = "PostgreSQL psql IDLE",
            idleTitleEn = "PostgreSQL psql IDLE",
            defaultFileName = "analytics.sql",
            runtimeBadge = "PostgreSQL 16",
            quickTokens = listOf("WITH ", "SELECT ", "FROM ", "WHERE ", "EXPLAIN ", "ORDER BY ", "LIMIT 10;"),
            supportsTableOutput = true,
            templates = listOf(
                IdleCodeTemplate(
                    id = "pg_cte",
                    titlePt = "CTE & Ranking",
                    titleEn = "CTE & Ranking",
                    code = """
                        WITH ranking AS (
                            SELECT playerName, score, maxCombo, crystalsCollected, techFocusId
                            FROM game_scores
                            ORDER BY score DESC
                            LIMIT 10
                        )
                        SELECT * FROM ranking;
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "pg_explain",
                    titlePt = "EXPLAIN Plano",
                    titleEn = "EXPLAIN Plan",
                    code = "EXPLAIN QUERY PLAN SELECT * FROM study_notes WHERE techId = 'sql';"
                )
            )
        ),
        Technology.MONGODB to LanguageIdleSpec(
            tech = Technology.MONGODB,
            idleTitlePt = "MongoDB Shell IDLE",
            idleTitleEn = "MongoDB Shell IDLE",
            defaultFileName = "playground.mongodb.js",
            runtimeBadge = "mongosh 2.x",
            quickTokens = listOf("db.", ".find({})", ".sort({score: -1})", ".limit(5)", ".countDocuments()", "show collections"),
            templates = listOf(
                IdleCodeTemplate(
                    id = "mongo_find",
                    titlePt = "Consultar Documentos",
                    titleEn = "Find Documents",
                    code = """
                        db.study_notes.find({ techId: "sql" })
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "mongo_insert",
                    titlePt = "Inserir & Listar",
                    titleEn = "Insert & Query",
                    code = """
                        db.study_notes.insertOne({ techId: "mongodb", title: "Aggregation Pipeline", score: 95 })
                        db.study_notes.find({})
                    """.trimIndent()
                )
            )
        ),
        Technology.HTML to LanguageIdleSpec(
            tech = Technology.HTML,
            idleTitlePt = "HTML5 Live IDLE",
            idleTitleEn = "HTML5 Live IDLE",
            defaultFileName = "index.html",
            runtimeBadge = "HTML5 DOM",
            quickTokens = listOf("<h1></h1>", "<p></p>", "<div class=\"\"></div>", "<button></button>", "<ul><li></li></ul>", "<style></style>"),
            supportsHtmlPreview = true,
            templates = listOf(
                IdleCodeTemplate(
                    id = "html_card",
                    titlePt = "Cartão Semântico",
                    titleEn = "Semantic Card",
                    code = """
                        <!DOCTYPE html>
                        <html>
                        <body style="font-family: sans-serif; background: #141022; color: #f4f1ff; padding: 18px;">
                          <main style="border: 1px solid #8b5cf6; border-radius: 16px; padding: 16px; background: #1f1936;">
                            <span style="color: #4ade80; font-size: 12px; font-weight: bold;">HTML5 LIVE PREVIEW</span>
                            <h1 style="margin: 8px 0 6px; font-size: 20px;">Olá, DevRoutine!</h1>
                            <p style="color: #cbd5e1; font-size: 14px; line-height: 1.5;">
                              Edite este HTML em tempo real e veja a renderização imediata abaixo.
                            </p>
                            <ul>
                              <li>Tags semânticas &lt;main&gt;, &lt;article&gt;</li>
                              <li>Layout responsivo mobile-first</li>
                            </ul>
                          </main>
                        </body>
                        </html>
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "html_form",
                    titlePt = "Formulário Dev",
                    titleEn = "Dev Form",
                    code = """
                        <section style="font-family: sans-serif; background: #0f172a; color: #f8fafc; padding: 16px; border-radius: 12px;">
                          <h2 style="margin-top: 0;">Cadastro de Meta Diária</h2>
                          <label style="display:block; margin-bottom: 6px;">Linguagem:</label>
                          <input type="text" value="HTML5 + CSS3" style="width: 100%; padding: 8px; border-radius: 8px; border: 1px solid #38bdf8; margin-bottom: 10px;" />
                          <button style="background: #8b5cf6; color: white; border: none; padding: 10px 16px; border-radius: 8px; font-weight: bold;">
                            Salvar Meta
                          </button>
                        </section>
                    """.trimIndent()
                )
            )
        ),
        Technology.CSS to LanguageIdleSpec(
            tech = Technology.CSS,
            idleTitlePt = "CSS3 Live Studio",
            idleTitleEn = "CSS3 Live Studio",
            defaultFileName = "styles.css",
            runtimeBadge = "CSS3 Flexbox/Grid",
            quickTokens = listOf("display: flex;", "gap: 12px;", "border-radius: 12px;", "background: ", "color: ", "padding: ", "{}"),
            supportsHtmlPreview = true,
            templates = listOf(
                IdleCodeTemplate(
                    id = "css_flex",
                    titlePt = "Cartão Flexbox Neon",
                    titleEn = "Flexbox Neon Card",
                    code = """
                        body {
                          background: #120d1f;
                          color: #ffffff;
                          font-family: sans-serif;
                          padding: 16px;
                        }
                        .preview-card {
                          display: flex;
                          flex-direction: column;
                          gap: 10px;
                          padding: 16px;
                          border-radius: 16px;
                          background: linear-gradient(135deg, #3b1d6a, #1e293b);
                          border: 1px solid #a855f7;
                        }
                        .badge {
                          align-self: flex-start;
                          background: #22c55e;
                          color: #052e16;
                          padding: 4px 10px;
                          border-radius: 999px;
                          font-weight: bold;
                          font-size: 12px;
                        }
                    """.trimIndent()
                ),
                IdleCodeTemplate(
                    id = "css_grid",
                    titlePt = "CSS Grid 2 Colunas",
                    titleEn = "CSS Grid 2 Columns",
                    code = """
                        body {
                          background: #0f172a;
                          color: #f8fafc;
                          font-family: sans-serif;
                          padding: 16px;
                        }
                        .grid-demo {
                          display: grid;
                          grid-template-columns: 1fr 1fr;
                          gap: 10px;
                        }
                        .box {
                          background: #1e293b;
                          border: 1px solid #38bdf8;
                          border-radius: 12px;
                          padding: 12px;
                          text-align: center;
                          font-weight: bold;
                        }
                    """.trimIndent()
                )
            )
        )
    )

    fun getSpec(tech: Technology): LanguageIdleSpec =
        specs[tech] ?: specs.getValue(Technology.PYTHON)
}
