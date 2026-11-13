package com.example.data.repository

import com.example.model.ActivityDifficulty
import com.example.model.CheatsheetItem
import com.example.model.PracticalActivity
import com.example.model.QuizQuestion
import com.example.model.RoadmapStep
import com.example.model.Technology

object TechContentRepository {

    fun getOverview(tech: Technology, isPt: Boolean): String = when (tech) {
        Technology.PYTHON -> if (isPt) {
            "Python é uma linguagem de alto nível, interpretada e com tipagem dinâmica, famosa por sua legibilidade ('The Zen of Python'). É amplamente utilizada em automação, inteligência artificial, análise de dados e desenvolvimento backend (Django, FastAPI, Flask)."
        } else {
            "Python is a high-level, interpreted, dynamically-typed language renowned for readability ('The Zen of Python'). Widely used for automation, AI/ML, data science, and backend APIs (FastAPI, Django, Flask)."
        }
        Technology.SQL -> if (isPt) {
            "SQL (Structured Query Language) é o padrão declarativo para consultar, inserir, atualizar e gerenciar dados estruturados em bancos relacionais. O domínio de filtros, junções (JOINs) e agregações é indispensável para qualquer desenvolvedor."
        } else {
            "SQL (Structured Query Language) is the declarative standard to query, insert, update, and manage structured data across relational databases. Mastering filtering, JOINs, and aggregations is a core developer skill."
        }
        Technology.HTML -> if (isPt) {
            "HTML5 é a espinha dorsal de toda página e aplicação web. Fornece estrutura semântica (<header>, <main>, <article>), acessibilidade (WAI-ARIA), formulários interativos e suporte nativo a multimídia."
        } else {
            "HTML5 is the backbone of the web. It delivers semantic structure (<header>, <main>, <article>), accessibility (WAI-ARIA), interactive form controls, and native audio/video support."
        }
        Technology.CSS -> if (isPt) {
            "CSS3 transforma marcação bruta em interfaces atraentes e responsivas. Abrange Box Model, Flexbox (unidimensional), CSS Grid (bidimensional), variáveis nativas, media queries e transições fluidas."
        } else {
            "CSS3 turns raw markup into attractive, responsive interfaces. It covers the Box Model, Flexbox (1D), CSS Grid (2D), native variables, media queries, and fluid transitions."
        }
        Technology.JAVA -> if (isPt) {
            "Java é uma linguagem orientada a objetos fortemente tipada que roda sobre a JVM (Java Virtual Machine). É o padrão em sistemas corporativos de grande porte, microserviços (Spring Boot) e processamento em escala."
        } else {
            "Java is a strongly typed, object-oriented language that runs on the JVM. It is the enterprise standard for high-throughput distributed microservices (Spring Boot) and large-scale architectures."
        }
        Technology.MYSQL -> if (isPt) {
            "MySQL é um sistema de gerenciamento de banco de dados relacional (SGBD) open-source extremamente veloz e maduro. Usa a engine InnoDB com suporte a transações ACID, chaves estrangeiras e índices B-Tree."
        } else {
            "MySQL is a mature, high-performance open-source relational database management system. Powered by InnoDB with ACID transaction support, foreign key constraints, and B-Tree indexing."
        }
        Technology.POSTGRESQL -> if (isPt) {
            "PostgreSQL é o banco relacional objeto-relacional mais avançado do ecossistema open-source. Destaca-se por suporte poderoso a JSONB, índices GIN/GiST, Common Table Expressions (CTEs), extensões como PostGIS e confiabilidade extrema."
        } else {
            "PostgreSQL is the world's most advanced open-source object-relational database. Renowned for first-class JSONB querying, GIN/GiST indexing, recursive CTEs, extensions like PostGIS, and data integrity."
        }
        Technology.MONGODB -> if (isPt) {
            "MongoDB é o banco de dados NoSQL orientado a documentos mais popular. Armazena dados no formato BSON (JSON binário flexível), oferecendo escalabilidade horizontal nativa, sharding e um poderoso framework de agregação."
        } else {
            "MongoDB is the premier document-oriented NoSQL database. It stores records in flexible BSON (binary JSON), providing native horizontal sharding, indexing, and an expressive aggregation pipeline."
        }
        Technology.JAVASCRIPT -> if (isPt) {
            "JavaScript é a linguagem essencial da web moderna, executando no navegador e no servidor (Node.js/Bun). Possui modelo assíncrono baseado em Event Loop, Promises, ES6+ e o maior ecossistema de bibliotecas do mundo (npm)."
        } else {
            "JavaScript is the foundational language of the modern web, running in both browsers and servers (Node.js/Bun). It features an event-driven, non-blocking I/O model, Promises, ES6+, and the world's largest package ecosystem (npm)."
        }
        Technology.TYPESCRIPT -> if (isPt) {
            "TypeScript é um superset fortemente tipado de JavaScript desenvolvido pela Microsoft. Adiciona checagem estática de tipos, interfaces, generics, enums e autocomplete avançado, compilando para JavaScript portátil e seguro."
        } else {
            "TypeScript is a strongly typed superset of JavaScript developed by Microsoft. It adds static type checking, interfaces, generics, enums, and advanced tooling, compiling down to clean, portable JavaScript."
        }
        Technology.KOTLIN -> if (isPt) {
            "Kotlin é uma linguagem multiplataforma moderna desenvolvida pela JetBrains e recomendada oficialmente pelo Google para Android. Destaca-se por segurança contra nulos (null safety), concisão, coroutines assíncronas e 100% de interoperabilidade com Java."
        } else {
            "Kotlin is a modern multiplatform language created by JetBrains and the official preferred choice for Android by Google. Renowned for null safety, conciseness, coroutines for asynchronous concurrency, and 100% Java interoperability."
        }
        Technology.GO -> if (isPt) {
            "Go (Golang) é uma linguagem compilada criada pelo Google focada em simplicidade, altíssima velocidade e concorrência nativa. Com goroutines, channels e binários estáticos, domina infraestrutura cloud (Docker, Kubernetes) e microserviços."
        } else {
            "Go (Golang) is a compiled systems & cloud language built at Google emphasizing simplicity, blazing compilation/execution speed, and CSP concurrency via goroutines and channels."
        }
    }

    fun getCheatsheet(tech: Technology): List<CheatsheetItem> = when (tech) {
        Technology.PYTHON -> listOf(
            CheatsheetItem(
                titlePt = "List Comprehensions & Filtros",
                titleEn = "List Comprehensions & Filtering",
                codeSnippet = "numbers = [1, 2, 3, 4, 5, 6]\n# Quadrados dos pares\neven_squares = [x**2 for x in numbers if x % 2 == 0]\nprint(even_squares) # [4, 16, 36]",
                explanationPt = "Forma idiomática e concisa de transformar e filtrar coleções sem loops for explícitos.",
                explanationEn = "Idiomatic, concise way to transform and filter collections in Python without explicit for loops."
            ),
            CheatsheetItem(
                titlePt = "Dicionários & Tratamento com Get",
                titleEn = "Dictionaries & Safe Access",
                codeSnippet = "user = {\"name\": \"Gabriel\", \"role\": \"developer\"}\n# Evita KeyError com valor padrão\nlevel = user.get(\"level\", \"Junior\")\nprint(f\"{user['name']} ({level})\")",
                explanationPt = "O método .get() retorna um valor padrão em vez de lançar KeyError quando a chave não existe.",
                explanationEn = ".get() provides a fallback value instead of raising a KeyError when a key is missing."
            ),
            CheatsheetItem(
                titlePt = "Funções com Type Hints e Args",
                titleEn = "Functions with Type Hints",
                codeSnippet = "def calculate_tax(amount: float, rate: float = 0.15) -> float:\n    \"\"\"Calcula o imposto sobre um montante.\"\"\"\n    return round(amount * rate, 2)\n\nprint(calculate_tax(100.0)) # 15.0",
                explanationPt = "Type hints aumentam a segurança, autocompletes e clareza do código moderno em Python.",
                explanationEn = "Type hints enhance code clarity, safety, and IDE auto-completion in modern Python."
            )
        )
        Technology.SQL -> listOf(
            CheatsheetItem(
                titlePt = "INNER JOIN e LEFT JOIN",
                titleEn = "INNER JOIN and LEFT JOIN",
                codeSnippet = "SELECT u.name, o.total_amount, o.created_at\nFROM users u\nINNER JOIN orders o ON u.id = o.user_id\nWHERE o.status = 'COMPLETED'\nORDER BY o.created_at DESC;",
                explanationPt = "INNER JOIN combina registros com correspondência em ambas as tabelas; LEFT JOIN mantém todos da tabela da esquerda.",
                explanationEn = "INNER JOIN matches rows in both tables; LEFT JOIN keeps all rows from the left table even without matching right rows."
            ),
            CheatsheetItem(
                titlePt = "Agrupamento com GROUP BY & HAVING",
                titleEn = "GROUP BY & HAVING Filtering",
                codeSnippet = "SELECT department, COUNT(*) AS total_employees, AVG(salary) AS avg_sal\nFROM employees\nGROUP BY department\nHAVING COUNT(*) >= 5;",
                explanationPt = "WHERE filtra linhas antes do agrupamento; HAVING filtra grupos agregados resultantes.",
                explanationEn = "WHERE filters individual rows before grouping; HAVING filters aggregated groups."
            ),
            CheatsheetItem(
                titlePt = "Subconsultas e Cláusula IN",
                titleEn = "Subqueries & IN Operator",
                codeSnippet = "SELECT name, email FROM users\nWHERE id IN (\n    SELECT user_id FROM subscriptions WHERE active = TRUE\n);",
                explanationPt = "Subqueries permitem relacionar resultados dinâmicos sem múltiplos comandos separados.",
                explanationEn = "Subqueries allow correlating dynamic queries without requiring multiple disconnected queries."
            )
        )
        Technology.HTML -> listOf(
            CheatsheetItem(
                titlePt = "Estrutura Semântica Base",
                titleEn = "Semantic Skeleton",
                codeSnippet = "<header>\n  <nav aria-label=\"Menu Principal\">\n    <a href=\"#home\">Início</a>\n  </nav>\n</header>\n<main>\n  <article>\n    <h2>Título do Post</h2>\n    <p>Conteúdo relevante...</p>\n  </article>\n</main>",
                explanationPt = "Tags semânticas melhoram SEO e leitura por tecnologias assistivas de acessibilidade (TalkBack).",
                explanationEn = "Semantic tags greatly boost SEO and assistive screen reader navigation (TalkBack)."
            ),
            CheatsheetItem(
                titlePt = "Formulário Acessível com Validação",
                titleEn = "Accessible Form with Native Validation",
                codeSnippet = "<form action=\"/api/submit\" method=\"POST\">\n  <label for=\"email\">E-mail Corporativo:</label>\n  <input type=\"email\" id=\"email\" name=\"email\" required placeholder=\"nome@exemplo.com\">\n  <button type=\"submit\">Cadastrar</button>\n</form>",
                explanationPt = "Associação explícita via 'for' e 'id' garante foco e contexto para leitores de tela.",
                explanationEn = "Explicit association via 'for' and 'id' ensures screen readers speak the input label correctly."
            )
        )
        Technology.CSS -> listOf(
            CheatsheetItem(
                titlePt = "Flexbox Centralizado & Espaçado",
                titleEn = "Flexbox Centering & Gap",
                codeSnippet = ".container {\n  display: flex;\n  justify-content: space-between;\n  align-items: center;\n  gap: 1.5rem;\n  flex-wrap: wrap;\n}",
                explanationPt = "Flexbox simplifica alinhamentos dinâmicos unidimensionais com a propriedade gap moderna.",
                explanationEn = "Flexbox simplifies 1D dynamic alignment with modern gap spacing."
            ),
            CheatsheetItem(
                titlePt = "CSS Grid Responsivo Automático",
                titleEn = "Responsive Auto-fit CSS Grid",
                codeSnippet = ".cards-grid {\n  display: grid;\n  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));\n  gap: 16px;\n}",
                explanationPt = "Cria um grid adaptável que se reorganiza sem precisar de dezenas de media queries.",
                explanationEn = "Builds an adaptive grid that rearranges cards automatically without cluttered media queries."
            )
        )
        Technology.JAVA -> listOf(
            CheatsheetItem(
                titlePt = "Record e Streams no Java Moderno",
                titleEn = "Records & Streams in Modern Java",
                codeSnippet = "public record Developer(String name, int experienceYears) {}\n\nList<Developer> devs = List.of(\n    new Developer(\"Ana\", 4),\n    new Developer(\"Carlos\", 8)\n);\n\nList<String> seniors = devs.stream()\n    .filter(d -> d.experienceYears() >= 5)\n    .map(Developer::name)\n    .toList();",
                explanationPt = "Records eliminam boilerplate e Streams trazem operações funcionais seguras e legíveis.",
                explanationEn = "Records eliminate boilerplate for immutable data, while Streams provide readable functional pipelines."
            ),
            CheatsheetItem(
                titlePt = "Tratamento de Exceções com Try-with-Resources",
                titleEn = "Try-with-Resources Auto-Close",
                codeSnippet = "try (var reader = new BufferedReader(new FileReader(\"data.txt\"))) {\n    String line;\n    while ((line = reader.readLine()) != null) {\n        System.out.println(line);\n    }\n} catch (IOException e) {\n    System.err.println(\"Erro ao ler arquivo: \" + e.getMessage());\n}",
                explanationPt = "Garante o fechamento automático de streams e conexões, prevenindo vazamentos de memória.",
                explanationEn = "Guarantees automatic cleanup of streams and socket/database resources, preventing resource leaks."
            )
        )
        Technology.MYSQL -> listOf(
            CheatsheetItem(
                titlePt = "Criação de Tabela com Chave Estrangeira",
                titleEn = "Creating Table with Foreign Key",
                codeSnippet = "CREATE TABLE orders (\n    id INT AUTO_INCREMENT PRIMARY KEY,\n    user_id INT NOT NULL,\n    total DECIMAL(10,2) NOT NULL,\n    status VARCHAR(20) DEFAULT 'PENDING',\n    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n    CONSTRAINT fk_orders_user\n        FOREIGN KEY (user_id) REFERENCES users(id)\n        ON DELETE CASCADE\n) ENGINE=InnoDB;",
                explanationPt = "A engine InnoDB aplica integridade referencial com cascata e suporte total a transações.",
                explanationEn = "InnoDB engine enforces referential integrity with cascading updates/deletes and ACID transactions."
            ),
            CheatsheetItem(
                titlePt = "Transação Segura (ACID)",
                titleEn = "Safe ACID Transaction",
                codeSnippet = "START TRANSACTION;\nUPDATE accounts SET balance = balance - 150.00 WHERE id = 1;\nUPDATE accounts SET balance = balance + 150.00 WHERE id = 2;\nCOMMIT; -- Ou ROLLBACK em caso de falha",
                explanationPt = "Transações garantem atomicidade: todas as operações são salvas ou revertidas por completo.",
                explanationEn = "Transactions enforce atomicity: all operations succeed together or rollback completely."
            )
        )
        Technology.POSTGRESQL -> listOf(
            CheatsheetItem(
                titlePt = "Consultas Avançadas em Colunas JSONB",
                titleEn = "Advanced JSONB Column Queries",
                codeSnippet = "-- Tabela com coluna jsonb\nSELECT id, metadata->>'plan' AS current_plan\nFROM accounts\nWHERE metadata @> '{\"verified\": true}';\n\n-- Índice GIN para busca ultra-rápida\nCREATE INDEX idx_accounts_metadata ON accounts USING GIN (metadata);",
                explanationPt = "JSONB armazena JSON em binário decomposto, permitindo buscas indexadas de alta velocidade.",
                explanationEn = "JSONB decomposes JSON into indexed binary structures, delivering high-speed search via GIN indexes."
            ),
            CheatsheetItem(
                titlePt = "CTEs Recursivas (Common Table Expressions)",
                titleEn = "Recursive CTEs (Common Table Expressions)",
                codeSnippet = "WITH RECURSIVE subordinates AS (\n    SELECT id, name, manager_id FROM employees WHERE id = 1\n    UNION ALL\n    SELECT e.id, e.name, e.manager_id\n    FROM employees e\n    INNER JOIN subordinates s ON s.id = e.manager_id\n)\nSELECT * FROM subordinates;",
                explanationPt = "CTEs recursivas resolvem hierarquias em árvores e organogramas de forma elegante e performática.",
                explanationEn = "Recursive CTEs elegantly traverse hierarchical structures and graphs in a single query."
            )
        )
        Technology.MONGODB -> listOf(
            CheatsheetItem(
                titlePt = "Inserção e Busca com Filtros BSON",
                titleEn = "Insert & Filter with BSON Operators",
                codeSnippet = "// Inserção\ndb.products.insertOne({\n  title: \"Teclado Mecânico\",\n  price: 299.90,\n  tags: [\"hardware\", \"setup\"],\n  stock: 15\n});\n\n// Busca com operador \$gte e \$in\ndb.products.find({\n  price: { \$gte: 100 },\n  tags: { \$in: [\"hardware\"] }\n}).sort({ price: -1 });",
                explanationPt = "Documentos BSON suportam estruturas aninhadas e arrays nativamente com consultas expressivas.",
                explanationEn = "BSON documents natively support nested objects and arrays with expressive operator queries."
            ),
            CheatsheetItem(
                titlePt = "Pipeline de Agregação (\$match e \$group)",
                titleEn = "Aggregation Pipeline (\$match & \$group)",
                codeSnippet = "db.orders.aggregate([\n  { \$match: { status: \"DELIVERED\" } },\n  { \$group: {\n      _id: \"\$customerId\",\n      totalSpent: { \$sum: \"\$amount\" },\n      ordersCount: { \$sum: 1 }\n  }},\n  { \$sort: { totalSpent: -1 } }\n]);",
                explanationPt = "O Aggregation Framework atua como processamento ETL em memória distribuída do MongoDB.",
                explanationEn = "The Aggregation Framework executes multi-stage data transformations directly on the database engine."
            )
        )
        Technology.JAVASCRIPT -> listOf(
            CheatsheetItem(
                titlePt = "Async / Await e Requisições com Fetch",
                titleEn = "Async / Await & Fetch Requests",
                codeSnippet = "async function buscarDev(username) {\n  try {\n    const res = await fetch(`https://api.github.com/users/\${username}`);\n    if (!res.ok) throw new Error(`HTTP \${res.status}`);\n    const data = await res.json();\n    return data;\n  } catch (err) {\n    console.error(\"Falha na requisição:\", err.message);\n  }\n}",
                explanationPt = "Sintaxe assíncrona idiomática que combina Promises com tratamento estruturado de erros via try/catch.",
                explanationEn = "Idiomatic asynchronous syntax pairing Promises with structured try/catch error handling."
            ),
            CheatsheetItem(
                titlePt = "Desestruturação e Operador Spread (...)",
                titleEn = "Destructuring & Spread Operator (...)",
                codeSnippet = "const dev = { nome: \"Mariana\", stack: \"Frontend\", exp: 3 };\nconst { nome, ...resto } = dev;\n\nconst novoDev = {\n  ...dev,\n  stack: \"Fullstack\",\n  skills: [\"React\", \"Node.js\"]\n};",
                explanationPt = "Permite extração limpa de propriedades e clonagem/atualização imutável de objetos e arrays.",
                explanationEn = "Enables clean extraction of properties alongside immutable cloning and merging of objects and arrays."
            ),
            CheatsheetItem(
                titlePt = "Operações Funcionais de Array (.map, .filter, .reduce)",
                titleEn = "Functional Array Operations (.map, .filter, .reduce)",
                codeSnippet = "const produtos = [\n  { id: 1, preco: 100, ativo: true },\n  { id: 2, preco: 250, ativo: false },\n  { id: 3, preco: 150, ativo: true }\n];\n\nconst totalAtivos = produtos\n  .filter(p => p.ativo)\n  .map(p => p.preco)\n  .reduce((acc, preco) => acc + preco, 0); // 250",
                explanationPt = "Encadeamento declarativo para filtrar, mapear e totalizar dados sem loops imperativos.",
                explanationEn = "Declarative chaining for filtering, mapping, and aggregating records without imperative loops."
            )
        )
        Technology.TYPESCRIPT -> listOf(
            CheatsheetItem(
                titlePt = "Interfaces com Generics e Opcionais",
                titleEn = "Interfaces with Generics & Optionals",
                codeSnippet = "interface RespostaApi<T> {\n  status: number;\n  sucesso: boolean;\n  dados: T;\n  erro?: string;\n}\n\ninterface PerfilDev {\n  id: string;\n  nome: string;\n  senioridade: \"Junior\" | \"Pleno\" | \"Senior\";\n}\n\ntype RespostaDev = RespostaApi<PerfilDev>;",
                explanationPt = "Combina contratos de tipos com generics parametrizados e union types estritos para máxima segurança.",
                explanationEn = "Pairs type contracts with parameterized generics and strict union types for compile-time safety."
            ),
            CheatsheetItem(
                titlePt = "Utility Types (Partial, Pick, Omit, Record)",
                titleEn = "Utility Types (Partial, Pick, Omit, Record)",
                codeSnippet = "interface Post {\n  id: number;\n  titulo: string;\n  conteudo: string;\n  curtidas: number;\n}\n\n// Apenas campos para preview\ntype PostPreview = Pick<Post, \"id\" | \"titulo\">;\n\n// Payload para atualização parcial (patch)\ntype PostUpdate = Partial<Omit<Post, \"id\">>;",
                explanationPt = "Utilitários nativos para derivar e transformar novos tipos de interfaces sem duplicação de código.",
                explanationEn = "Built-in utilities to transform and derive new types from existing shapes without duplication."
            ),
            CheatsheetItem(
                titlePt = "Type Guards & Afunilamento (Narrowing)",
                titleEn = "Type Guards & Type Narrowing",
                codeSnippet = "interface Sucesso { status: \"sucesso\"; dados: string[] }\ninterface Falha { status: \"falha\"; mensagem: string }\ntype Resultado = Sucesso | Falha;\n\nfunction processar(res: Resultado) {\n  if (res.status === \"sucesso\") {\n    console.log(res.dados.length); // TypeScript sabe que é Sucesso!\n  } else {\n    console.error(res.mensagem);\n  }\n}",
                explanationPt = "Discriminated Unions permitem que o compilador deduza com precisão as propriedades no bloco condicional.",
                explanationEn = "Discriminated unions enable the TypeScript compiler to precisely narrow branch types within conditionals."
            )
        )
        Technology.KOTLIN -> listOf(
            CheatsheetItem(
                titlePt = "Null Safety com Safe Calls (?.) e Elvis (?:)",
                titleEn = "Null Safety with Safe Calls (?.) & Elvis (?:)",
                codeSnippet = "val apelido: String? = null\n// Se nulo, usa valor padrão seguro\nval tamanho = apelido?.length ?: 0\n\n// Executa bloco seguro apenas se não for nulo\napelido?.let {\n    println(\"Nome em maiúsculo: \${it.uppercase()}\")\n}",
                explanationPt = "O sistema de tipos do Kotlin impede NullPointerExceptions garantindo verificações estritas.",
                explanationEn = "Kotlin's type system prevents NullPointerExceptions through strict compile-time nullability enforcement."
            ),
            CheatsheetItem(
                titlePt = "Data Classes e Copy Imutável",
                titleEn = "Data Classes & Immutable Copy",
                codeSnippet = "data class Usuario(\n    val id: Long,\n    val nome: String,\n    val cargo: String = \"Developer\"\n)\n\nval u1 = Usuario(1, \"Carlos\")\n// Cria nova cópia alterando apenas campos necessários\nval u2 = u1.copy(cargo = \"Tech Lead\")",
                explanationPt = "Gera automaticamente equals(), hashCode(), toString() e copy() para manipulação imutável idiomática.",
                explanationEn = "Auto-generates equals(), hashCode(), toString(), and copy() enabling clean immutable state updates."
            ),
            CheatsheetItem(
                titlePt = "Coroutines & StateFlow Reativo",
                titleEn = "Coroutines & Reactive StateFlow",
                codeSnippet = "class RepositorioViewModel : ViewModel() {\n    private val _dados = MutableStateFlow<List<String>>(emptyList())\n    val dados: StateFlow<List<String>> = _dados.asStateFlow()\n\n    fun carregar() = viewModelScope.launch {\n        val resposta = withContext(Dispatchers.IO) {\n            apiService.buscarItens()\n        }\n        _dados.value = resposta\n    }\n}",
                explanationPt = "Padrão moderno de concorrência assíncrona leve no Android e backend sem travamento de UI.",
                explanationEn = "Modern lightweight asynchronous concurrency standard for Android and backend without UI blocking."
            )
        )
        Technology.GO -> listOf(
            CheatsheetItem(
                titlePt = "Goroutines, Channels e WaitGroup",
                titleEn = "Goroutines, Channels & WaitGroup",
                codeSnippet = "func processar(id int, ch chan<- string, wg *sync.WaitGroup) {\n    defer wg.Done()\n    ch <- fmt.Sprintf(\"Worker %d concluído\", id)\n}\n\nvar wg sync.WaitGroup\nch := make(chan string, 2)\nwg.Add(2)\ngo processar(1, ch, &wg)\ngo processar(2, ch, &wg)\nwg.Wait()\nclose(ch)",
                explanationPt = "Goroutines executam funções concorrentemente enquanto channels sincronizam mensagens sem mutex manual.",
                explanationEn = "Goroutines execute functions concurrently while typed channels communicate safely without manual locks."
            ),
            CheatsheetItem(
                titlePt = "Tratamento Explícito de Erros com Wrap (%w)",
                titleEn = "Explicit Error Handling with %w Wrapping",
                codeSnippet = "func buscarUsuario(id string) (User, error) {\n    u, err := repo.Find(id)\n    if err != nil {\n        return User{}, fmt.Errorf(\"buscarUsuario(%s): %w\", id, err)\n    }\n    return u, nil\n}",
                explanationPt = "Go trata erros como valores de retorno explícitos e usa %w para preservar a causa original.",
                explanationEn = "Go handles errors as explicit return values and uses %w to wrap underlying errors cleanly."
            )
        )
    }

    fun getRoadmap(tech: Technology): List<RoadmapStep> = when (tech) {
        Technology.PYTHON -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Fundamentos da Sintaxe",
                titleEn = "Syntax Fundamentals",
                descPt = "Variáveis, tipos primitivos, estruturas condicionais e laços de repetição.",
                descEn = "Variables, primitive types, conditional branching, and loops.",
                estimatedHours = 8,
                topicsPt = listOf("Tipos de dados", "Operadores", "Estruturas if/elif/else", "Loops for & while"),
                topicsEn = listOf("Data types", "Operators", "Conditional statements", "Loops for & while")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "Estruturas de Dados & Coleções",
                titleEn = "Data Structures & Collections",
                descPt = "Manipulação idiomática de listas, tuplas, dicionários e conjuntos.",
                descEn = "Idiomatic usage of lists, tuples, dictionaries, and sets.",
                estimatedHours = 12,
                topicsPt = listOf("List comprehensions", "Dicionários aninhados", "Conjuntos (Sets)", "Fatiamento de listas"),
                topicsEn = listOf("List comprehensions", "Nested dictionaries", "Sets", "List slicing")
            ),
            RoadmapStep(
                stepNumber = 3,
                titlePt = "POO e Módulos",
                titleEn = "OOP and Modules",
                descPt = "Criação de classes, métodos mágicos (__init__, __str__), herança e pacotes.",
                descEn = "Classes, dunder methods, inheritance, composition, and package management.",
                estimatedHours = 14,
                topicsPt = listOf("Classes e Objetos", "Herança", "Tratamento de Exceções", "Ambientes virtuais (venv)"),
                topicsEn = listOf("Classes & Objects", "Inheritance", "Exception handling", "Virtual environments (venv)")
            )
        )
        Technology.SQL -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "DQL Básico (Data Query Language)",
                titleEn = "Basic DQL (Data Query Language)",
                descPt = "Seleção, projeção, ordenação e filtros com operadores lógicos.",
                descEn = "Selection, projection, sorting, and conditional filters with boolean operators.",
                estimatedHours = 6,
                topicsPt = listOf("SELECT e DISTINCT", "Filtro WHERE", "ORDER BY e LIMIT", "LIKE e BETWEEN"),
                topicsEn = listOf("SELECT & DISTINCT", "WHERE filter", "ORDER BY & LIMIT", "LIKE & BETWEEN")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "Relacionamentos & JOINs",
                titleEn = "Relationships & JOINs",
                descPt = "Combinação de múltiplas tabelas normalizadas através de chaves.",
                descEn = "Combining multiple normalized tables through primary/foreign keys.",
                estimatedHours = 10,
                topicsPt = listOf("INNER JOIN", "LEFT e RIGHT JOIN", "CROSS JOIN", "Auto-relacionamento (Self JOIN)"),
                topicsEn = listOf("INNER JOIN", "LEFT & RIGHT JOIN", "CROSS JOIN", "Self JOIN")
            ),
            RoadmapStep(
                stepNumber = 3,
                titlePt = "Funções de Agregação & DDL",
                titleEn = "Aggregations & DDL/DML",
                descPt = "Cálculos agrupados e comandos de criação e modificação de schemas.",
                descEn = "Grouped calculations and schema creation/manipulation commands.",
                estimatedHours = 12,
                topicsPt = listOf("GROUP BY & HAVING", "COUNT, SUM, AVG", "CREATE TABLE", "INSERT, UPDATE, DELETE"),
                topicsEn = listOf("GROUP BY & HAVING", "COUNT, SUM, AVG", "CREATE TABLE", "INSERT, UPDATE, DELETE")
            )
        )
        Technology.HTML -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Semântica & Estruturação Web",
                titleEn = "Semantics & Web Structure",
                descPt = "Tags estruturais modernas do HTML5 e boas práticas para acessibilidade.",
                descEn = "Modern HTML5 semantic elements and accessibility best practices.",
                estimatedHours = 6,
                topicsPt = listOf("Tags de cabeçalho", "<main>, <article>, <section>", "Acessibilidade e alt", "Metatags essenciais"),
                topicsEn = listOf("Header tags", "<main>, <article>, <section>", "Accessibility & alt tags", "Essential metatags")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "Formulários Interativos",
                titleEn = "Interactive Forms",
                descPt = "Coleta de dados, validação nativa e elementos acessíveis para o usuário.",
                descEn = "Data collection, native validation, and accessible interactive inputs.",
                estimatedHours = 8,
                topicsPt = listOf("Labels e Inputs", "Validações com regex/pattern", "Select, Textarea e Checkbox", "Envio via POST/GET"),
                topicsEn = listOf("Labels & Inputs", "Regex/pattern validation", "Select, Textarea & Checkbox", "Submitting via POST/GET")
            )
        )
        Technology.CSS -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Box Model & Seletores",
                titleEn = "Box Model & Selectors",
                descPt = "Compreensão de margin, border, padding, dimensões e especificidade.",
                descEn = "Mastering margin, border, padding, sizing models, and specificity.",
                estimatedHours = 8,
                topicsPt = listOf("Box-sizing: border-box", "Seletores de classe e ID", "Pseudo-classes (:hover, :focus)", "Cores e gradientes"),
                topicsEn = listOf("Box-sizing: border-box", "Class & ID selectors", "Pseudo-classes (:hover, :focus)", "Colors & gradients")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "Flexbox & Grid Layout",
                titleEn = "Flexbox & Grid Layouts",
                descPt = "Construção de layouts modernos, adaptáveis e livres de floats.",
                descEn = "Constructing modern, adaptive layouts without legacy float hacks.",
                estimatedHours = 14,
                topicsPt = listOf("Eixos Flexbox (main/cross)", "Justify-content & Align-items", "Grid template columns", "Variáveis CSS (--var)"),
                topicsEn = listOf("Flexbox axes (main/cross)", "Justify-content & Align-items", "Grid template columns", "CSS variables (--var)")
            )
        )
        Technology.JAVA -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Sintaxe & Tipagem Estática",
                titleEn = "Syntax & Static Typing",
                descPt = "Compilação na JVM, tipos primitivos, referências e controle de fluxo.",
                descEn = "JVM compilation, primitive types, references, and control structures.",
                estimatedHours = 10,
                topicsPt = listOf("Estrutura da classe Main", "Tipos primitivos vs Wrappers", "Arrays e loops", "Métodos estáticos"),
                topicsEn = listOf("Main class structure", "Primitives vs Wrappers", "Arrays & loops", "Static methods")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "Orientação a Objetos Avançada",
                titleEn = "Advanced Object-Oriented Design",
                descPt = "Encapsulamento, herança, interfaces, polimorfismo e Records.",
                descEn = "Encapsulation, inheritance, interfaces, polymorphism, and Java Records.",
                estimatedHours = 16,
                topicsPt = listOf("Classes e Construtores", "Interfaces e Default Methods", "Polimorfismo dinâmico", "Generics e Collections"),
                topicsEn = listOf("Classes & Constructors", "Interfaces & Default Methods", "Dynamic polymorphism", "Generics & Collections")
            )
        )
        Technology.MYSQL -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Arquitetura & Configuração",
                titleEn = "Architecture & Setup",
                descPt = "Conceitos de cliente-servidor, tipos de engines e configuração de instâncias.",
                descEn = "Client-server architecture, storage engines, and instance configuration.",
                estimatedHours = 6,
                topicsPt = listOf("Instalação e Workbench", "Engine InnoDB vs MyISAM", "Charsets e Collations", "Usuários e privilégios"),
                topicsEn = listOf("Installation & Workbench", "InnoDB vs MyISAM engines", "Charsets & Collations", "User permissions")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "Modelagem Relacional & Índices",
                titleEn = "Relational Modeling & Indexing",
                descPt = "Normalização de dados, chaves estrangeiras e índices para alta performance.",
                descEn = "Data normalization, foreign key constraints, and indexing for performance.",
                estimatedHours = 12,
                topicsPt = listOf("Formas Normais (1FN, 2FN, 3FN)", "Índices B-Tree", "Plano de Execução (EXPLAIN)", "Transactions e Locks"),
                topicsEn = listOf("Normal Forms (1NF, 2NF, 3NF)", "B-Tree Indexes", "Execution Plans (EXPLAIN)", "Transactions & Locks")
            )
        )
        Technology.POSTGRESQL -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Recursos Avançados do Postgres",
                titleEn = "Advanced Postgres Features",
                descPt = "Tipos especiais, arrays nativos, domínios e robustez relacional.",
                descEn = "Specialized data types, native arrays, domains, and relational integrity.",
                estimatedHours = 8,
                topicsPt = listOf("Tipos UUID, TIMESTAMPZ e Arrays", "Constraints CHECK e UNIQUE", "Views e Materialized Views", "psql CLI"),
                topicsEn = listOf("UUID, TIMESTAMPZ & Arrays", "CHECK & UNIQUE constraints", "Views & Materialized Views", "psql CLI")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "JSONB, GIN e CTEs",
                titleEn = "JSONB, GIN & CTEs",
                descPt = "Banco híbrido relacional + NoSQL, índices GIN e consultas analíticas.",
                descEn = "Hybrid relational + NoSQL data modeling, GIN indexes, and analytics.",
                estimatedHours = 14,
                topicsPt = listOf("Operadores JSONB (->, ->>, @>)", "Índices GIN para JSON", "Window Functions (RANK, OVER)", "CTEs Recursivas"),
                topicsEn = listOf("JSONB operators (->, ->>, @>)", "GIN indexes for JSON", "Window Functions (RANK, OVER)", "Recursive CTEs")
            )
        )
        Technology.MONGODB -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Modelo de Documentos & CRUD",
                titleEn = "Document Model & CRUD Operations",
                descPt = "Coleções, BSON, inserção, consulta, atualização e exclusão.",
                descEn = "Collections, BSON specification, insert, query, update, and delete.",
                estimatedHours = 8,
                topicsPt = listOf("BSON vs JSON", "insertOne e insertMany", "Operadores \$set, \$push, \$inc", "Indexação simples e composta"),
                topicsEn = listOf("BSON vs JSON", "insertOne & insertMany", "Operators \$set, \$push, \$inc", "Single & compound indexes")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "Framework de Agregação",
                titleEn = "Aggregation Framework",
                descPt = "Pipelines analíticos para agrupamento, projeção e junções (\$lookup).",
                descEn = "Analytical pipelines for grouping, projection, filtering, and \$lookup joins.",
                estimatedHours = 12,
                topicsPt = listOf("Pipeline \$match, \$group, \$sort", "\$project e expressões calculadas", "\$lookup para junção de coleções", "\$unwind para desnormalizar arrays"),
                topicsEn = listOf("Pipeline \$match, \$group, \$sort", "\$project & computed expressions", "\$lookup for multi-collection joins", "\$unwind for array destructuring")
            )
        )
        Technology.JAVASCRIPT -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Fundamentos do JavaScript & ES6+",
                titleEn = "JavaScript Fundamentals & ES6+",
                descPt = "Declarações const/let, arrow functions, template literals, destructuring e manipulação de arrays.",
                descEn = "const/let declarations, arrow functions, template literals, destructuring, and array operations.",
                estimatedHours = 8,
                topicsPt = listOf("Escopo de variáveis e let/const", "Arrow functions e retorno implícito", "Destructuring e Spread (...)", "Métodos de Array (.map, .filter, .reduce)"),
                topicsEn = listOf("Variable scoping & let/const", "Arrow functions & implicit returns", "Destructuring & Spread (...)", "Array methods (.map, .filter, .reduce)")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "JavaScript Assíncrono & Event Loop",
                titleEn = "Asynchronous JavaScript & Event Loop",
                descPt = "Entendimento profundo do Event Loop, Callbacks, Promises, async/await e Fetch API.",
                descEn = "Deep dive into the Event Loop, Callbacks, Promises, async/await, and Fetch API.",
                estimatedHours = 12,
                topicsPt = listOf("Event Loop e Microtasks", "Promises e encadeamento .then()", "async/await e try/catch", "Fetch API e integração HTTP"),
                topicsEn = listOf("Event Loop & Microtasks", "Promises & .then() chaining", "async/await & try/catch", "Fetch API & HTTP integration")
            ),
            RoadmapStep(
                stepNumber = 3,
                titlePt = "POO Moderna, Módulos & Node.js",
                titleEn = "Modern OOP, Modules & Node.js",
                descPt = "Classes ES6, herança com super, módulos ESM e desenvolvimento fullstack com Node.js.",
                descEn = "ES6 classes, super inheritance, ESM modules, and fullstack backend development with Node.js.",
                estimatedHours = 14,
                topicsPt = listOf("Classes ES6 e herança", "Closures e escopo léxico", "Módulos import/export ESM", "Node.js e gerenciamento npm"),
                topicsEn = listOf("ES6 classes & inheritance", "Closures & lexical scope", "ESM import/export modules", "Node.js & npm ecosystem")
            )
        )
        Technology.TYPESCRIPT -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Tipagem Estática Básica & Interfaces",
                titleEn = "Static Typing Basics & Interfaces",
                descPt = "Anotações de tipos primitivos, interfaces de contratos, type aliases e union types.",
                descEn = "Primitive type annotations, contract interfaces, type aliases, and union types.",
                estimatedHours = 8,
                topicsPt = listOf("Tipos primitivos e inferência", "Interfaces e campos opcionais (?)", "Union Types e Type Aliases", "Tipagem de parâmetros de funções"),
                topicsEn = listOf("Primitive types & inference", "Interfaces & optional fields (?)", "Union Types & Type Aliases", "Function parameter typing")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "Generics & Utility Types",
                titleEn = "Generics & Utility Types",
                descPt = "Criação de código reutilizável com generics, restrições com extends e utility types do TypeScript.",
                descEn = "Reusable code with generics, extends constraints, and built-in TypeScript utility types.",
                estimatedHours = 12,
                topicsPt = listOf("Funções genéricas <T>", "Generic constraints (T extends)", "Partial, Pick, Omit, Record", "Type Assertions (as)"),
                topicsEn = listOf("Generic functions <T>", "Generic constraints (T extends)", "Partial, Pick, Omit, Record", "Type Assertions (as)")
            ),
            RoadmapStep(
                stepNumber = 3,
                titlePt = "Tipos Avançados & Configuração de Projetos",
                titleEn = "Advanced Types & Project Configuration",
                descPt = "Type guards com predicates, mapped types, conditional types e configuração tsconfig.json.",
                descEn = "Custom type predicates, mapped types, conditional types, and tsconfig.json configuration.",
                estimatedHours = 14,
                topicsPt = listOf("Type Predicates (val is Type)", "Conditional Types (T extends U ? X : Y)", "Mapped Types ([K in keyof T])", "Configurações estritas no tsconfig.json"),
                topicsEn = listOf("Type Predicates (val is Type)", "Conditional Types (T extends U ? X : Y)", "Mapped Types ([K in keyof T])", "Strict tsconfig.json configurations")
            )
        )
        Technology.KOTLIN -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Fundamentos do Kotlin & Null Safety",
                titleEn = "Kotlin Fundamentals & Null Safety",
                descPt = "val vs var, interpolação de strings, sistema de tipos anuláveis, safe call e expressão when.",
                descEn = "val vs var, string templates, nullable type system, safe call, and when expressions.",
                estimatedHours = 8,
                topicsPt = listOf("val (imutável) e var (mutável)", "Null Safety (?., ?:, !!.)", "Expressão when exaustiva", "Valores padrão em funções"),
                topicsEn = listOf("val (immutable) vs var (mutable)", "Null Safety (?., ?:, !!.)", "Exhaustive when expressions", "Default function arguments")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "POO Idiomática & Funções de Escopo",
                titleEn = "Idiomatic OOP & Scope Functions",
                descPt = "Data classes, extension functions, coleções funcionais (map, filter) e funções de escopo (let, apply).",
                descEn = "Data classes, extension functions, functional collections (map, filter), and scope functions (let, apply).",
                estimatedHours = 12,
                topicsPt = listOf("Data classes e método copy()", "Extension functions", "Funções de escopo (let, apply, run)", "Operações em coleções e groupBy"),
                topicsEn = listOf("Data classes & copy() method", "Extension functions", "Scope functions (let, apply, run)", "Collection operations & groupBy")
            ),
            RoadmapStep(
                stepNumber = 3,
                titlePt = "Coroutines, StateFlow & Jetpack Compose",
                titleEn = "Coroutines, StateFlow & Jetpack Compose",
                descPt = "Concorrência estruturada com coroutines, Dispatchers, StateFlow reativo e UI moderna em Compose.",
                descEn = "Structured concurrency with coroutines, Dispatchers, reactive StateFlow, and modern Compose UI.",
                estimatedHours = 16,
                topicsPt = listOf("Coroutines e Dispatchers.IO/Main", "withContext, async e await", "Flow e StateFlow reativo", "Integração com ViewModel e Jetpack Compose"),
                topicsEn = listOf("Coroutines & Dispatchers.IO/Main", "withContext, async & await", "Flow & reactive StateFlow", "ViewModel & Jetpack Compose integration")
            )
        )
        Technology.GO -> listOf(
            RoadmapStep(
                stepNumber = 1,
                titlePt = "Sintaxe Essencial, Slices & Structs",
                titleEn = "Core Syntax, Slices & Structs",
                descPt = "Declaração curta :=, slices dinâmicos, maps, structs e retorno múltiplo.",
                descEn = "Short declarations :=, dynamic slices, maps, structs, and multiple returns.",
                estimatedHours = 8,
                topicsPt = listOf("Operador := e tipos básicos", "Slices e make/append", "Structs e Pointer Receivers (*T)", "Tratamento de erro (if err != nil)"),
                topicsEn = listOf(":= operator & primitives", "Slices & make/append", "Structs & Pointer Receivers (*T)", "Error handling (if err != nil)")
            ),
            RoadmapStep(
                stepNumber = 2,
                titlePt = "Interfaces, Pacotes & APIs Web",
                titleEn = "Interfaces, Packages & Web APIs",
                descPt = "Polimorfismo implícito, encoding/json, net/http e módulos go.mod.",
                descEn = "Implicit interface satisfaction, encoding/json, net/http, and go.mod modules.",
                estimatedHours = 12,
                topicsPt = listOf("Interfaces implícitas", "Struct tags & JSON Marshaling", "Servidores net/http", "defer e errors.Is/As"),
                topicsEn = listOf("Implicit interfaces", "Struct tags & JSON Marshaling", "net/http servers", "defer & errors.Is/As")
            ),
            RoadmapStep(
                stepNumber = 3,
                titlePt = "Concorrência com Goroutines, Channels & Context",
                titleEn = "Concurrency with Goroutines, Channels & Context",
                descPt = "Goroutines, channels buffered/unbuffered, select, Mutex, WaitGroup e context timeouts.",
                descEn = "Goroutines, buffered/unbuffered channels, select, Mutex, WaitGroup, and context deadlines.",
                estimatedHours = 14,
                topicsPt = listOf("Goroutines e sync.WaitGroup", "Channels e multiplexação select", "context.WithTimeout e cancelamento", "Worker Pools e testes nativos"),
                topicsEn = listOf("Goroutines & sync.WaitGroup", "Channels & select multiplexing", "context.WithTimeout & cancellation", "Worker Pools & native testing")
            )
        )
    }

    fun getQuizQuestions(tech: Technology): List<QuizQuestion> = when (tech) {
        Technology.PYTHON -> listOf(
            QuizQuestion(
                id = 101,
                tech = Technology.PYTHON,
                questionPt = "Qual é o resultado da expressão [x * 2 for x in range(4) if x % 2 == 0] em Python?",
                questionEn = "What is the result of [x * 2 for x in range(4) if x % 2 == 0] in Python?",
                optionsPt = listOf("[0, 4]", "[0, 2, 4]", "[2, 4]", "[0, 2, 4, 6]"),
                optionsEn = listOf("[0, 4]", "[0, 2, 4]", "[2, 4]", "[0, 2, 4, 6]"),
                correctIndex = 0,
                explanationPt = "range(4) produz 0, 1, 2, 3. Os pares são 0 e 2. Multiplicados por 2 resultam em 0 e 4: [0, 4].",
                explanationEn = "range(4) produces 0, 1, 2, 3. Even numbers are 0 and 2. Multiplied by 2 they yield 0 and 4: [0, 4]."
            ),
            QuizQuestion(
                id = 102,
                tech = Technology.PYTHON,
                questionPt = "Qual método em dicionários Python busca uma chave e retorna um fallback sem lançar KeyError?",
                questionEn = "Which Python dictionary method retrieves a key with a default fallback without raising a KeyError?",
                optionsPt = listOf(".get()", ".find()", ".lookup()", ".fetch()"),
                optionsEn = listOf(".get()", ".find()", ".lookup()", ".fetch()"),
                correctIndex = 0,
                explanationPt = "dict.get(key, default) retorna o valor da chave se ela existir, ou o valor padrão definido caso não exista.",
                explanationEn = "dict.get(key, default) safely returns the key value or the default fallback if the key is missing."
            )
        )
        Technology.SQL -> listOf(
            QuizQuestion(
                id = 201,
                tech = Technology.SQL,
                questionPt = "Qual é a principal diferença entre WHERE e HAVING no SQL?",
                questionEn = "What is the primary difference between WHERE and HAVING in SQL?",
                optionsPt = listOf(
                    "WHERE filtra linhas individuais antes da agregação; HAVING filtra grupos agregados.",
                    "WHERE só funciona com números; HAVING só com texto.",
                    "HAVING é executado antes do FROM; WHERE depois do ORDER BY.",
                    "Não há diferença, são sinônimos."
                ),
                optionsEn = listOf(
                    "WHERE filters individual rows before aggregation; HAVING filters aggregated groups.",
                    "WHERE only works with numbers; HAVING with text.",
                    "HAVING runs before FROM; WHERE runs after ORDER BY.",
                    "There is no difference, they are identical."
                ),
                correctIndex = 0,
                explanationPt = "WHERE filtra as linhas de entrada antes do agrupamento; HAVING filtra os grupos calculados pelo GROUP BY.",
                explanationEn = "WHERE filters input rows prior to grouping; HAVING filters the groups generated by GROUP BY."
            ),
            QuizQuestion(
                id = 202,
                tech = Technology.SQL,
                questionPt = "Qual tipo de JOIN preserva todos os registros da tabela à esquerda, mesmo sem correspondência na direita?",
                questionEn = "Which JOIN type retains all rows from the left table, even if there is no match on the right?",
                optionsPt = listOf("LEFT JOIN", "INNER JOIN", "FULL JOIN", "RIGHT JOIN"),
                optionsEn = listOf("LEFT JOIN", "INNER JOIN", "FULL JOIN", "RIGHT JOIN"),
                correctIndex = 0,
                explanationPt = "O LEFT JOIN (ou LEFT OUTER JOIN) garante que todas as linhas da tabela esquerda permaneçam no resultado.",
                explanationEn = "LEFT JOIN (or LEFT OUTER JOIN) guarantees all left-table rows are preserved in the result."
            )
        )
        Technology.HTML -> listOf(
            QuizQuestion(
                id = 301,
                tech = Technology.HTML,
                questionPt = "Qual elemento semântico do HTML5 deve conter o conteúdo central e único da página?",
                questionEn = "Which HTML5 semantic element should contain the central, unique page content?",
                optionsPt = listOf("<main>", "<article>", "<content>", "<section>"),
                optionsEn = listOf("<main>", "<article>", "<content>", "<section>"),
                correctIndex = 0,
                explanationPt = "A tag <main> deve conter o conteúdo principal da página, não devendo ser repetida em outros documentos.",
                explanationEn = "<main> wraps the dominant unique content of the document, distinct from repeated headers/navbars."
            )
        )
        Technology.CSS -> listOf(
            QuizQuestion(
                id = 401,
                tech = Technology.CSS,
                questionPt = "No CSS Flexbox, qual propriedade alinha os itens no eixo principal (main axis)?",
                questionEn = "In CSS Flexbox, which property aligns items along the main axis?",
                optionsPt = listOf("justify-content", "align-items", "align-content", "flex-direction"),
                optionsEn = listOf("justify-content", "align-items", "align-content", "flex-direction"),
                correctIndex = 0,
                explanationPt = "justify-content distribui o espaço ao longo do eixo principal (por padrão horizontal se row).",
                explanationEn = "justify-content distributes space along the main axis (horizontal by default when flex-direction is row)."
            )
        )
        Technology.JAVA -> listOf(
            QuizQuestion(
                id = 501,
                tech = Technology.JAVA,
                questionPt = "Qual estrutura introduzida nas versões modernas do Java cria classes de dados imutáveis de forma concisa?",
                questionEn = "Which feature introduced in modern Java provides concise, immutable data classes?",
                optionsPt = listOf("record", "data class", "struct", "sealed bean"),
                optionsEn = listOf("record", "data class", "struct", "sealed bean"),
                correctIndex = 0,
                explanationPt = "Java 'record' cria automaticamente construtores, getters, equals(), hashCode() e toString() imutáveis.",
                explanationEn = "Java 'record' automatically provides constructors, accessors, equals(), hashCode(), and toString() as immutable containers."
            )
        )
        Technology.MYSQL -> listOf(
            QuizQuestion(
                id = 601,
                tech = Technology.MYSQL,
                questionPt = "Qual é a storage engine padrão do MySQL que suporta transações ACID e foreign keys?",
                questionEn = "What is MySQL's default storage engine supporting ACID transactions and foreign keys?",
                optionsPt = listOf("InnoDB", "MyISAM", "Memory", "Archive"),
                optionsEn = listOf("InnoDB", "MyISAM", "Memory", "Archive"),
                correctIndex = 0,
                explanationPt = "InnoDB é a engine padrão e recomendada do MySQL, provendo integridade referencial e conformidade ACID.",
                explanationEn = "InnoDB is MySQL's default and robust engine providing referential integrity and ACID compliance."
            )
        )
        Technology.POSTGRESQL -> listOf(
            QuizQuestion(
                id = 701,
                tech = Technology.POSTGRESQL,
                questionPt = "Qual tipo de índice no PostgreSQL é mais indicado para acelerar buscas dentro de colunas JSONB?",
                questionEn = "Which PostgreSQL index type is best suited to accelerate queries on JSONB columns?",
                optionsPt = listOf("GIN (Generalized Inverted Index)", "B-Tree simples", "Hash", "BRIN"),
                optionsEn = listOf("GIN (Generalized Inverted Index)", "Simple B-Tree", "Hash", "BRIN"),
                correctIndex = 0,
                explanationPt = "Índices GIN indexam cada chave e valor interno de documentos JSONB, permitindo consultas instantâneas com operadores como @>.",
                explanationEn = "GIN indexes index subkeys and values in JSONB documents, providing fast searches with operators like @>."
            )
        )
        Technology.MONGODB -> listOf(
            QuizQuestion(
                id = 801,
                tech = Technology.MONGODB,
                questionPt = "No Aggregation Pipeline do MongoDB, qual estágio é equivalente à cláusula WHERE do SQL?",
                questionEn = "In MongoDB's Aggregation Pipeline, which stage corresponds to SQL's WHERE clause?",
                optionsPt = listOf("\$match", "\$filter", "\$group", "\$where"),
                optionsEn = listOf("\$match", "\$filter", "\$group", "\$where"),
                correctIndex = 0,
                explanationPt = "O estágio \$match filtra os documentos no início do pipeline, similar ao WHERE no SQL.",
                explanationEn = "The \$match stage filters documents before passing them to downstream stages, matching SQL's WHERE."
            )
        )
        Technology.JAVASCRIPT -> listOf(
            QuizQuestion(
                id = 901,
                tech = Technology.JAVASCRIPT,
                questionPt = "Qual é o resultado da expressão 'typeof NaN' em JavaScript?",
                questionEn = "What is the result of 'typeof NaN' in JavaScript?",
                optionsPt = listOf("\"number\"", "\"NaN\"", "\"undefined\"", "\"object\""),
                optionsEn = listOf("\"number\"", "\"NaN\"", "\"undefined\"", "\"object\""),
                correctIndex = 0,
                explanationPt = "Apesar de significar 'Not-a-Number', a especificação ECMAScript define NaN como um valor numérico especial (tipo 'number').",
                explanationEn = "Even though it stands for 'Not-a-Number', IEEE 754 and ECMAScript classify NaN as a numeric value."
            ),
            QuizQuestion(
                id = 902,
                tech = Technology.JAVASCRIPT,
                questionPt = "Qual método resolve múltiplas Promises em paralelo e retorna um array com todos os resultados resolvidos?",
                questionEn = "Which method executes multiple Promises concurrently and returns an array with all resolved results?",
                optionsPt = listOf("Promise.all()", "Promise.race()", "Promise.wait()", "Promise.sync()"),
                optionsEn = listOf("Promise.all()", "Promise.race()", "Promise.wait()", "Promise.sync()"),
                correctIndex = 0,
                explanationPt = "Promise.all() aguarda a resolução de todas as promises em paralelo e falha imediatamente se qualquer uma for rejeitada.",
                explanationEn = "Promise.all() aggregates concurrent promises, fulfilling with an array of values when all succeed."
            )
        )
        Technology.TYPESCRIPT -> listOf(
            QuizQuestion(
                id = 1001,
                tech = Technology.TYPESCRIPT,
                questionPt = "Qual utility type em TypeScript transforma todas as propriedades de uma interface em campos opcionais?",
                questionEn = "Which TypeScript utility type transforms all interface properties into optional fields?",
                optionsPt = listOf("Partial<T>", "Required<T>", "Readonly<T>", "Optional<T>"),
                optionsEn = listOf("Partial<T>", "Required<T>", "Readonly<T>", "Optional<T>"),
                correctIndex = 0,
                explanationPt = "Partial<T> percorre todas as propriedades de T e adiciona o modificador de opcional (?).",
                explanationEn = "Partial<T> iterates over all properties of T adding the optional modifier (?)."
            ),
            QuizQuestion(
                id = 1002,
                tech = Technology.TYPESCRIPT,
                questionPt = "Qual palavra-chave é usada para restringir os tipos permitidos em um parâmetro genérico <T>?",
                questionEn = "Which keyword is used to constrain allowed types in a generic parameter <T>?",
                optionsPt = listOf("extends", "implements", "where", "with"),
                optionsEn = listOf("extends", "implements", "where", "with"),
                correctIndex = 0,
                explanationPt = "A cláusula 'extends' em generics impõe que o tipo genérico deva herdar ou possuir o formato do tipo especificado.",
                explanationEn = "The 'extends' generic constraint ensures that the type argument inherits from or satisfies the given shape."
            )
        )
        Technology.KOTLIN -> listOf(
            QuizQuestion(
                id = 1101,
                tech = Technology.KOTLIN,
                questionPt = "Em Kotlin, como o operador Elvis (?:) funciona na expressão 'val x = a ?: b'?",
                questionEn = "In Kotlin, how does the Elvis operator (?:) evaluate 'val x = a ?: b'?",
                optionsPt = listOf(
                    "Retorna 'a' se não for nulo; caso contrário retorna 'b'.",
                    "Retorna sempre 'b'.",
                    "Executa uma comparação ternária booleana.",
                    "Lança NullPointerException se 'a' for nulo."
                ),
                optionsEn = listOf(
                    "Returns 'a' if not null; otherwise returns 'b'.",
                    "Always returns 'b'.",
                    "Executes a boolean ternary comparison.",
                    "Throws NullPointerException if 'a' is null."
                ),
                correctIndex = 0,
                explanationPt = "O operador Elvis (?:) avalia o lado esquerdo; se não for nulo o utiliza, senão executa o lado direito como fallback.",
                explanationEn = "The Elvis operator (?:) yields the left expression if non-null, evaluating the right operand as a fallback."
            ),
            QuizQuestion(
                id = 1102,
                tech = Technology.KOTLIN,
                questionPt = "Qual recurso em Kotlin permite adicionar novos métodos a uma classe existente sem herança ou modificação de código fonte?",
                questionEn = "Which Kotlin feature allows adding new methods to an existing class without inheritance or source modification?",
                optionsPt = listOf(
                    "Extension Functions (fun Tipo.metodo())",
                    "Modificador 'open'",
                    "Override de construtor",
                    "Companion Objects dinâmicos"
                ),
                optionsEn = listOf(
                    "Extension Functions (fun Type.method())",
                    "Modifier 'open'",
                    "Constructor override",
                    "Dynamic companion objects"
                ),
                correctIndex = 0,
                explanationPt = "Extension functions estendem classes pré-existentes resolvendo chamadas estaticamente pelo compilador.",
                explanationEn = "Extension functions extend classes with new methods resolved statically by the compiler."
            )
        )
        Technology.GO -> listOf(
            QuizQuestion(
                id = 1201,
                tech = Technology.GO,
                questionPt = "Qual instrução agenda a execução de uma chamada (como fechar um arquivo) imediatamente antes da função retornar?",
                questionEn = "Which statement schedules a call (such as closing a file) to run right before the enclosing function returns?",
                optionsPt = listOf("defer", "finally", "atexit", "post"),
                optionsEn = listOf("defer", "finally", "atexit", "post"),
                correctIndex = 0,
                explanationPt = "A instrução 'defer' adia a execução da chamada até o retorno da função envolvente, garantindo limpeza segura.",
                explanationEn = "The 'defer' statement defers execution until the surrounding function returns, ensuring cleanup."
            ),
            QuizQuestion(
                id = 1202,
                tech = Technology.GO,
                questionPt = "Como uma struct satisfaz uma interface na linguagem Go?",
                questionEn = "How does a struct satisfy an interface in Go?",
                optionsPt = listOf(
                    "Implicitamente, bastando implementar todos os métodos definidos pela interface.",
                    "Usando a palavra-chave 'implements' na declaração da struct.",
                    "Registrando em um arquivo go.interfaces.",
                    "Herdando de interfaceBase com 'extends'."
                ),
                optionsEn = listOf(
                    "Implicitly, simply by implementing all methods declared in the interface.",
                    "Using the 'implements' keyword on the struct.",
                    "Registering in a go.interfaces manifest.",
                    "Extending a base interface with 'extends'."
                ),
                correctIndex = 0,
                explanationPt = "Go utiliza tipagem estrutural implícita (duck typing estático): não existe palavra-chave 'implements'.",
                explanationEn = "Go uses implicit structural typing for interfaces without any explicit 'implements' keyword."
            )
        )
    }

    fun getPracticalActivities(tech: Technology): List<PracticalActivity> =
        com.example.data.challenges.TechChallengeBank.getChallenges(tech)
}
