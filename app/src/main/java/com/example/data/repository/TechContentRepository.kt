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
    }

    fun getPracticalActivities(tech: Technology): List<PracticalActivity> = when (tech) {
        Technology.PYTHON -> listOf(
            PracticalActivity(
                id = "py_act_1",
                tech = Technology.PYTHON,
                titlePt = "Interpolação de Strings com F-Strings",
                titleEn = "String Formatting with F-Strings",
                difficulty = ActivityDifficulty.BEGINNER,
                pointsValue = 30,
                instructionsPt = "Formate e imprima uma mensagem usando f-strings que combine o nome do desenvolvedor e o tempo de estudo em minutos.",
                instructionsEn = "Format and print a message using Python f-strings combining developer name and study minutes.",
                starterCode = "nome = \"Gabriel\"\nminutos = 45\n# Use f-string para exibir: 'Gabriel estudou por 45 minutos.'\nmensagem = f\"{___} estudou por {___} minutos.\"\nprint(mensagem)",
                solutionCode = "nome = \"Gabriel\"\nminutos = 45\nmensagem = f\"{nome} estudou por {minutos} minutos.\"\nprint(mensagem)",
                hintsPt = listOf("Insira a variável 'nome' na primeira interpolação.", "Insira a variável 'minutos' na segunda."),
                hintsEn = listOf("Insert variable 'nome' in first bracket.", "Insert 'minutos' in the second bracket."),
                requiredKeywords = listOf("f\"", "nome", "minutos"),
                explanationPt = "F-strings (PEP 498) são a forma mais rápida, legível e moderna de interpolar expressões em Python.",
                explanationEn = "F-strings provide modern, concise, and performant string interpolation in Python."
            ),
            PracticalActivity(
                id = "py_act_2",
                tech = Technology.PYTHON,
                titlePt = "Filtragem com List Comprehension",
                titleEn = "Filtering with List Comprehension",
                difficulty = ActivityDifficulty.INTERMEDIATE,
                pointsValue = 60,
                instructionsPt = "Crie uma List Comprehension chamada 'quadrados_pares' que receba uma lista e retorne apenas o quadrado dos números pares (onde x % 2 == 0).",
                instructionsEn = "Write a List Comprehension named 'quadrados_pares' that returns the squares of even numbers (x % 2 == 0).",
                starterCode = "numeros = [1, 2, 3, 4, 5, 6, 7, 8]\nquadrados_pares = [___ for x in numeros if ___]\nprint(quadrados_pares)",
                solutionCode = "numeros = [1, 2, 3, 4, 5, 6, 7, 8]\nquadrados_pares = [x**2 for x in numeros if x % 2 == 0]\nprint(quadrados_pares)",
                hintsPt = listOf("Use 'x**2' para o quadrado.", "A condição do filtro é 'if x % 2 == 0'."),
                hintsEn = listOf("Use 'x**2' for the square.", "Filter condition is 'if x % 2 == 0'."),
                requiredKeywords = listOf("for", "if", "%", "2"),
                explanationPt = "List Comprehensions combinam transformação e filtragem em uma única instrução limpa e de alto desempenho.",
                explanationEn = "List Comprehensions unify mapping and filtering cleanly with high performance."
            ),
            PracticalActivity(
                id = "py_act_3",
                tech = Technology.PYTHON,
                titlePt = "Funções Geradoras com Yield & Dicionários",
                titleEn = "Generator Functions with Yield & Dictionaries",
                difficulty = ActivityDifficulty.ADVANCED,
                pointsValue = 90,
                instructionsPt = "Escreva uma função geradora 'filtrar_usuarios_ativos' que percorra uma lista de dicionários e use 'yield' para emitir apenas os usuários onde '.get(\"ativo\")' é True.",
                instructionsEn = "Write a generator function 'filtrar_usuarios_ativos' yielding users where '.get(\"ativo\")' is True.",
                starterCode = "def filtrar_usuarios_ativos(usuarios: list):\n    for u in usuarios:\n        if u.get(\"ativo\", False):\n            ___ u\n\n# Teste:\nlista = [{\"nome\": \"Leo\", \"ativo\": True}, {\"nome\": \"Bia\", \"ativo\": False}]\nfor user in filtrar_usuarios_ativos(lista):\n    print(user[\"nome\"])",
                solutionCode = "def filtrar_usuarios_ativos(usuarios: list):\n    for u in usuarios:\n        if u.get(\"ativo\", False):\n            yield u",
                hintsPt = listOf("Substitua os sublinhados pela palavra-chave 'yield'.", "Geradores economizam memória pois avaliam sob demanda (lazy evaluation)."),
                hintsEn = listOf("Use keyword 'yield'.", "Generators save memory via on-demand lazy evaluation."),
                requiredKeywords = listOf("def", "for", "yield", "get"),
                explanationPt = "Funções geradoras com yield produzem sequências sob demanda, economizando memória em processamentos de grandes volumes.",
                explanationEn = "Generator functions produce lazy streams, drastically reducing memory overhead on large datasets."
            )
        )

        Technology.SQL -> listOf(
            PracticalActivity(
                id = "sql_act_1",
                tech = Technology.SQL,
                titlePt = "Filtro Simples com WHERE & ORDER BY",
                titleEn = "Filtering with WHERE & ORDER BY",
                difficulty = ActivityDifficulty.BEGINNER,
                pointsValue = 30,
                instructionsPt = "Escreva uma consulta SELECT que traga os campos 'nome' e 'salario' da tabela 'colaboradores' onde salario > 3000, ordenado de forma decrescente por salario.",
                instructionsEn = "Write a SELECT query getting 'nome' and 'salario' from 'colaboradores' where salario > 3000, ordered descending.",
                starterCode = "SELECT nome, salario\nFROM colaboradores\nWHERE salario > ___\nORDER BY salario ___;\n",
                solutionCode = "SELECT nome, salario\nFROM colaboradores\nWHERE salario > 3000\nORDER BY salario DESC;",
                hintsPt = listOf("Preencha o valor de corte '3000'.", "Para ordenação decrescente use 'DESC'."),
                hintsEn = listOf("Use threshold '3000'.", "For descending sort use 'DESC'."),
                requiredKeywords = listOf("SELECT", "WHERE", "ORDER BY", "DESC"),
                explanationPt = "A cláusula WHERE filtra linhas de entrada e ORDER BY classifica o conjunto resultante de forma eficiente.",
                explanationEn = "WHERE filters individual input rows and ORDER BY sorts the resulting record set."
            ),
            PracticalActivity(
                id = "sql_act_2",
                tech = Technology.SQL,
                titlePt = "Junção Relacional com INNER JOIN",
                titleEn = "Relational JOIN with INNER JOIN",
                difficulty = ActivityDifficulty.INTERMEDIATE,
                pointsValue = 60,
                instructionsPt = "Escreva uma consulta SQL que una a tabela 'usuarios' com a tabela 'pedidos' através de 'usuario_id' e filtre apenas pedidos com status 'PAGO'.",
                instructionsEn = "Write a SQL query joining 'usuarios' and 'pedidos' on 'usuario_id', filtering for status 'PAGO'.",
                starterCode = "SELECT u.nome, p.id AS pedido_id, p.valor\nFROM usuarios u\n___ pedidos p ON ___\nWHERE p.status = 'PAGO';",
                solutionCode = "SELECT u.nome, p.id AS pedido_id, p.valor\nFROM usuarios u\nINNER JOIN pedidos p ON u.id = p.usuario_id\nWHERE p.status = 'PAGO';",
                hintsPt = listOf("Use a cláusula 'INNER JOIN pedidos p'.", "A condição de ligação é 'ON u.id = p.usuario_id'."),
                hintsEn = listOf("Use 'INNER JOIN pedidos p'.", "Condition is 'ON u.id = p.usuario_id'."),
                requiredKeywords = listOf("JOIN", "ON", "WHERE"),
                explanationPt = "O INNER JOIN retorna apenas os registros que possuem correspondência em ambas as tabelas envolvidas.",
                explanationEn = "INNER JOIN returns only rows that have matching records in both tables."
            ),
            PracticalActivity(
                id = "sql_act_3",
                tech = Technology.SQL,
                titlePt = "Agrupamento Analítico com GROUP BY & HAVING",
                titleEn = "Analytical Grouping with GROUP BY & HAVING",
                difficulty = ActivityDifficulty.ADVANCED,
                pointsValue = 90,
                instructionsPt = "Escreva uma consulta que agrupe por 'departamento', calcule a média salarial com AVG(salario) e filtre apenas departamentos que possuem 5 ou mais colaboradores usando HAVING.",
                instructionsEn = "Write a query grouping by 'departamento', computing AVG(salario), and filtering groups with >= 5 employees via HAVING.",
                starterCode = "SELECT departamento, AVG(salario) AS media_salarial, COUNT(*) AS total\nFROM funcionarios\nGROUP BY ___\nHAVING COUNT(*) >= ___;",
                solutionCode = "SELECT departamento, AVG(salario) AS media_salarial, COUNT(*) AS total\nFROM funcionarios\nGROUP BY departamento\nHAVING COUNT(*) >= 5;",
                hintsPt = listOf("Agrupe pelo campo 'departamento'.", "A condição na cláusula HAVING deve verificar 'COUNT(*) >= 5'."),
                hintsEn = listOf("Group by 'departamento'.", "Condition is 'COUNT(*) >= 5' inside HAVING."),
                requiredKeywords = listOf("GROUP BY", "HAVING", "COUNT", "AVG"),
                explanationPt = "A cláusula HAVING é aplicada após a agregação, permitindo filtrar métricas calculadas que o WHERE não consegue acessar.",
                explanationEn = "HAVING filters aggregate computations after grouping has been performed."
            )
        )

        Technology.HTML -> listOf(
            PracticalActivity(
                id = "html_act_1",
                tech = Technology.HTML,
                titlePt = "Estrutura Semântica Principal",
                titleEn = "Core Semantic Architecture",
                difficulty = ActivityDifficulty.BEGINNER,
                pointsValue = 30,
                instructionsPt = "Estruture um layout semântico básico contendo cabeçalho com <header>, conteúdo central com <main> e rodapé com <footer>.",
                instructionsEn = "Structure a clean semantic layout containing <header>, <main>, and <footer>.",
                starterCode = "<body>\n  <___>\n    <h1>DevRoutine</h1>\n  </___>\n  <___>\n    <p>Conteúdo principal do app.</p>\n  </___>\n  <___>Todos os direitos reservados</footer>\n</body>",
                solutionCode = "<body>\n  <header>\n    <h1>DevRoutine</h1>\n  </header>\n  <main>\n    <p>Conteúdo principal do app.</p>\n  </main>\n  <footer>Todos os direitos reservados</footer>\n</body>",
                hintsPt = listOf("O cabeçalho é a tag <header>.", "O conteúdo principal é a tag <main>."),
                hintsEn = listOf("Top banner is <header>.", "Dominant content is <main>."),
                requiredKeywords = listOf("<header>", "<main>", "<footer>"),
                explanationPt = "Tags semânticas estruturam o documento de forma que leitores de tela e motores de busca compreendam a hierarquia.",
                explanationEn = "Semantic tags explicitly declare the hierarchy of web documents for assistive tech and SEO."
            ),
            PracticalActivity(
                id = "html_act_2",
                tech = Technology.HTML,
                titlePt = "Formulário Acessível com Validação",
                titleEn = "Accessible Form with Native Validation",
                difficulty = ActivityDifficulty.INTERMEDIATE,
                pointsValue = 60,
                instructionsPt = "Construa um formulário com inputs de email e senha, associando labels via atributo 'for' e marcando ambos como 'required'.",
                instructionsEn = "Build a form with email and password inputs, associated labels via 'for'/'id', and 'required' attributes.",
                starterCode = "<form action=\"/login\" method=\"POST\">\n  <label for=\"email\">E-mail:</label>\n  <input type=\"___\" id=\"email\" name=\"email\" ___>\n  <label for=\"senha\">Senha:</label>\n  <input type=\"password\" id=\"senha\" name=\"senha\" ___>\n  <button type=\"submit\">Entrar</button>\n</form>",
                solutionCode = "<form action=\"/login\" method=\"POST\">\n  <label for=\"email\">E-mail:</label>\n  <input type=\"email\" id=\"email\" name=\"email\" required>\n  <label for=\"senha\">Senha:</label>\n  <input type=\"password\" id=\"senha\" name=\"senha\" required>\n  <button type=\"submit\">Entrar</button>\n</form>",
                hintsPt = listOf("O tipo para validação nativa de email é type=\"email\".", "Adicione a palavra 'required' nos campos obrigatórios."),
                hintsEn = listOf("Email input type is type=\"email\".", "Add 'required' keyword for mandatory fields."),
                requiredKeywords = listOf("<form", "type=\"email\"", "required", "<button"),
                explanationPt = "Validações nativas do HTML5 garantem feedback instantâneo sem necessidade de scripts pesados.",
                explanationEn = "Native HTML5 form validations deliver zero-script instant client checks."
            ),
            PracticalActivity(
                id = "html_act_3",
                tech = Technology.HTML,
                titlePt = "Tabela Acessível com Thead, Scope e Caption",
                titleEn = "Accessible Data Table with Scope and Caption",
                difficulty = ActivityDifficulty.ADVANCED,
                pointsValue = 90,
                instructionsPt = "Crie uma tabela de dados acessível utilizando <caption>, <thead>, <tbody> e cabeçalhos com o atributo scope=\"col\".",
                instructionsEn = "Create an accessible table using <caption>, <thead>, <tbody>, and headers with scope=\"col\".",
                starterCode = "<table>\n  <caption>Tabela de Pontuação</caption>\n  <___>\n    <tr>\n      <th scope=\"___\">Linguagem</th>\n      <th scope=\"___\">Pontos XP</th>\n    </tr>\n  </___>\n  <tbody>\n    <tr><td>Python</td><td>150</td></tr>\n  </tbody>\n</table>",
                solutionCode = "<table>\n  <caption>Tabela de Pontuação</caption>\n  <thead>\n    <tr>\n      <th scope=\"col\">Linguagem</th>\n      <th scope=\"col\">Pontos XP</th>\n    </tr>\n  </thead>\n  <tbody>\n    <tr><td>Python</td><td>150</td></tr>\n  </tbody>\n</table>",
                hintsPt = listOf("O cabeçalho da tabela é delimitado por <thead>.", "O atributo de escopo de coluna é scope=\"col\"."),
                hintsEn = listOf("Table header is <thead>.", "Column scope is scope=\"col\"."),
                requiredKeywords = listOf("<table>", "<caption>", "<thead>", "scope=\"col\"", "<tbody>"),
                explanationPt = "O atributo scope=\"col\" e o elemento caption são essenciais para conformidade WCAG e acessibilidade total.",
                explanationEn = "scope=\"col\" and captions are critical requirements for WCAG compliance in tabular data."
            )
        )

        Technology.CSS -> listOf(
            PracticalActivity(
                id = "css_act_1",
                tech = Technology.CSS,
                titlePt = "Box Model & Borda Arredondada",
                titleEn = "Box Model & Rounded Borders",
                difficulty = ActivityDifficulty.BEGINNER,
                pointsValue = 30,
                instructionsPt = "Configure uma classe '.card' com padding de 16px, borda arredondada de 12px e 'box-sizing: border-box'.",
                instructionsEn = "Configure a '.card' selector with 16px padding, 12px border-radius, and 'box-sizing: border-box'.",
                starterCode = ".card {\n  box-sizing: ___;\n  padding: ___;\n  border-radius: ___;\n  background-color: #2A1B3D;\n}",
                solutionCode = ".card {\n  box-sizing: border-box;\n  padding: 16px;\n  border-radius: 12px;\n  background-color: #2A1B3D;\n}",
                hintsPt = listOf("Para o box-sizing use 'border-box'.", "Preencha padding com '16px' e border-radius com '12px'."),
                hintsEn = listOf("Use 'border-box' for box-sizing.", "Set padding: 16px and border-radius: 12px."),
                requiredKeywords = listOf("box-sizing", "border-box", "padding", "border-radius"),
                explanationPt = "box-sizing: border-box garante que padding e bordas não alterem a largura total do elemento calculada no layout.",
                explanationEn = "border-box includes padding and border within the element's total width and height."
            ),
            PracticalActivity(
                id = "css_act_2",
                tech = Technology.CSS,
                titlePt = "Centralização Perfeita com Flexbox",
                titleEn = "Centering Elements with Flexbox",
                difficulty = ActivityDifficulty.INTERMEDIATE,
                pointsValue = 60,
                instructionsPt = "Escreva as propriedades Flexbox para centralizar um container perfeitamente nos eixos horizontal e vertical.",
                instructionsEn = "Write Flexbox properties to center items along both horizontal and vertical axes.",
                starterCode = ".container {\n  display: ___;\n  justify-content: ___;\n  align-items: ___;\n  min-height: 100vh;\n}",
                solutionCode = ".container {\n  display: flex;\n  justify-content: center;\n  align-items: center;\n  min-height: 100vh;\n}",
                hintsPt = listOf("Defina 'display: flex'.", "Use 'center' tanto no justify-content quanto no align-items."),
                hintsEn = listOf("Set 'display: flex'.", "Set both justify-content and align-items to 'center'."),
                requiredKeywords = listOf("flex", "justify-content", "align-items", "center"),
                explanationPt = "A combinação de display: flex com justify-content: center e align-items: center é a solução definitiva de centralização.",
                explanationEn = "Flexbox centering guarantees reliable alignment across all modern screen resolutions."
            ),
            PracticalActivity(
                id = "css_act_3",
                tech = Technology.CSS,
                titlePt = "Grid Responsivo com Minmax e Auto-Fit",
                titleEn = "Responsive Grid with Auto-Fit and Minmax",
                difficulty = ActivityDifficulty.ADVANCED,
                pointsValue = 90,
                instructionsPt = "Configure um grid responsivo moderno usando repeat, auto-fit e minmax para colunas que se auto-ajustem a no mínimo 250px.",
                instructionsEn = "Configure a modern responsive CSS Grid using repeat, auto-fit, and minmax for cards at least 250px wide.",
                starterCode = ".dashboard-grid {\n  display: grid;\n  grid-template-columns: repeat(___, minmax(250px, 1fr));\n  gap: 16px;\n}",
                solutionCode = ".dashboard-grid {\n  display: grid;\n  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n  gap: 16px;\n}",
                hintsPt = listOf("Preencha com 'auto-fit' dentro do repeat().", "Essa técnica cria grids responsivos sem nenhuma media query."),
                hintsEn = listOf("Use 'auto-fit' inside repeat().", "This technique adapts columns automatically without media queries."),
                requiredKeywords = listOf("grid", "grid-template-columns", "repeat", "auto-fit", "minmax"),
                explanationPt = "repeat(auto-fit, minmax(250px, 1fr)) calcula dinamicamente quantas colunas cabem na tela, quebrando linhas automaticamente.",
                explanationEn = "auto-fit with minmax creates fluid, intrinsically responsive grid tracks that eliminate breakpoint code."
            )
        )

        Technology.JAVA -> listOf(
            PracticalActivity(
                id = "java_act_1",
                tech = Technology.JAVA,
                titlePt = "Encapsulamento e Métodos Getters",
                titleEn = "Encapsulation & Getter Methods",
                difficulty = ActivityDifficulty.BEGINNER,
                pointsValue = 30,
                instructionsPt = "Complete a classe 'Estudante' com um atributo privado 'nome' e seu respectivo método getter público.",
                instructionsEn = "Complete 'Estudante' class with a private field 'nome' and its public getter.",
                starterCode = "public class Estudante {\n    private String nome;\n    public Estudante(String nome) { this.nome = nome; }\n    public String ___() {\n        return this.___\n    }\n}",
                solutionCode = "public class Estudante {\n    private String nome;\n    public Estudante(String nome) { this.nome = nome; }\n    public String getNome() {\n        return this.nome;\n    }\n}",
                hintsPt = listOf("O nome do método deve ser 'getNome'.", "Retorne o campo 'this.nome'."),
                hintsEn = listOf("Method name is 'getNome'.", "Return 'this.nome'."),
                requiredKeywords = listOf("public", "getNome", "return", "nome"),
                explanationPt = "Encapsulamento protege o estado interno das classes Java e padroniza o acesso a dados.",
                explanationEn = "Encapsulation protects class invariants and establishes standard object access contracts."
            ),
            PracticalActivity(
                id = "java_act_2",
                tech = Technology.JAVA,
                titlePt = "Criação de Record Imutável",
                titleEn = "Modern Java Immutable Records",
                difficulty = ActivityDifficulty.INTERMEDIATE,
                pointsValue = 60,
                instructionsPt = "Declare um 'record' em Java moderno chamado 'UsuarioDto' com os atributos 'Long id', 'String nome' e 'String email'.",
                instructionsEn = "Declare a modern Java 'record' named 'UsuarioDto' with fields 'Long id', 'String nome', and 'String email'.",
                starterCode = "// Declare o record UsuarioDto:\npublic ___ UsuarioDto(Long id, String nome, String email) {\n}",
                solutionCode = "public record UsuarioDto(Long id, String nome, String email) {\n}",
                hintsPt = listOf("A palavra-chave introduzida no Java moderno é 'record'.", "Records não necessitam de corpo com getters/setters."),
                hintsEn = listOf("Use keyword 'record'.", "Records do not require boilerplate getters."),
                requiredKeywords = listOf("record", "UsuarioDto", "Long", "String"),
                explanationPt = "Records eliminam dezenas de linhas de boilerplate gerando automaticamente equals, hashCode, toString e construtor.",
                explanationEn = "Java records synthesize canonical constructor, accessors, and value equality automatically."
            ),
            PracticalActivity(
                id = "java_act_3",
                tech = Technology.JAVA,
                titlePt = "Processamento Declarativo com Streams API",
                titleEn = "Declarative Pipelines with Streams API",
                difficulty = ActivityDifficulty.ADVANCED,
                pointsValue = 90,
                instructionsPt = "Escreva um pipeline de Stream que filtre produtos com preco > 100.0, mapeie para seus nomes em caixa alta e colete em uma List.",
                instructionsEn = "Write a Stream pipeline filtering price > 100.0, mapping to uppercase names, and collecting to List.",
                starterCode = "List<String> nomes = produtos.stream()\n    .filter(p -> p.getPreco() > 100.0)\n    .map(p -> p.getNome().toUpperCase())\n    .collect(___.toList());",
                solutionCode = "List<String> nomes = produtos.stream()\n    .filter(p -> p.getPreco() > 100.0)\n    .map(p -> p.getNome().toUpperCase())\n    .collect(Collectors.toList());",
                hintsPt = listOf("A classe utilitária do Java é 'Collectors'.", "Use 'Collectors.toList()'."),
                hintsEn = listOf("Use helper class 'Collectors'.", "Collect using 'Collectors.toList()'."),
                requiredKeywords = listOf("stream", "filter", "map", "collect", "Collectors"),
                explanationPt = "Streams permitem expressar transformações de dados de alta performance e paralelização transparente na JVM.",
                explanationEn = "Streams express functional pipelines with transparent optimization and optional parallel execution."
            )
        )

        Technology.MYSQL -> listOf(
            PracticalActivity(
                id = "mysql_act_1",
                tech = Technology.MYSQL,
                titlePt = "Inserção de Dados com INSERT INTO",
                titleEn = "Inserting Data with INSERT INTO",
                difficulty = ActivityDifficulty.BEGINNER,
                pointsValue = 30,
                instructionsPt = "Escreva o comando SQL para inserir um novo curso ('Python Pro', 120) na tabela 'cursos'.",
                instructionsEn = "Write the SQL command to insert a new course ('Python Pro', 120) into 'cursos' table.",
                starterCode = "INSERT INTO cursos (titulo, horas)\nVALUES ('___', ___);",
                solutionCode = "INSERT INTO cursos (titulo, horas)\nVALUES ('Python Pro', 120);",
                hintsPt = listOf("Preencha o título com 'Python Pro'.", "Preencha a carga horária com 120."),
                hintsEn = listOf("Set title 'Python Pro'.", "Set hours to 120."),
                requiredKeywords = listOf("INSERT INTO", "VALUES", "cursos"),
                explanationPt = "INSERT INTO adiciona novos registros com tipagem validada pelas regras da coluna no MySQL.",
                explanationEn = "INSERT INTO persists validated new records into MySQL relational tables."
            ),
            PracticalActivity(
                id = "mysql_act_2",
                tech = Technology.MYSQL,
                titlePt = "Criação de Tabela com Engine InnoDB",
                titleEn = "Table Creation with InnoDB Engine",
                difficulty = ActivityDifficulty.INTERMEDIATE,
                pointsValue = 60,
                instructionsPt = "Escreva o comando DDL CREATE TABLE para 'clientes' com 'id INT AUTO_INCREMENT PRIMARY KEY', 'nome VARCHAR(100) NOT NULL' e engine InnoDB.",
                instructionsEn = "Write the CREATE TABLE DDL for 'clientes' with AUTO_INCREMENT PRIMARY KEY and InnoDB engine.",
                starterCode = "CREATE TABLE clientes (\n  id INT ___ PRIMARY KEY,\n  nome VARCHAR(100) NOT NULL\n) ENGINE=___;",
                solutionCode = "CREATE TABLE clientes (\n  id INT AUTO_INCREMENT PRIMARY KEY,\n  nome VARCHAR(100) NOT NULL\n) ENGINE=InnoDB;",
                hintsPt = listOf("No MySQL autoincremento se escreve 'AUTO_INCREMENT'.", "Defina 'ENGINE=InnoDB'."),
                hintsEn = listOf("Use 'AUTO_INCREMENT' in MySQL.", "Specify 'ENGINE=InnoDB'."),
                requiredKeywords = listOf("CREATE TABLE", "AUTO_INCREMENT", "PRIMARY KEY", "InnoDB"),
                explanationPt = "A engine InnoDB garante suporte a transações ACID, bloqueio em nível de linha e chaves estrangeiras.",
                explanationEn = "InnoDB engine delivers row-level locking, ACID transactions, and foreign key integrity."
            ),
            PracticalActivity(
                id = "mysql_act_3",
                tech = Technology.MYSQL,
                titlePt = "Integridade Referencial com Foreign Key e Cascata",
                titleEn = "Foreign Key Constraint with Cascade Deletion",
                difficulty = ActivityDifficulty.ADVANCED,
                pointsValue = 90,
                instructionsPt = "Adicione a restrição FOREIGN KEY na tabela 'pedidos' referenciando a tabela 'clientes(id)' com a ação ON DELETE CASCADE.",
                instructionsEn = "Add the FOREIGN KEY constraint in 'pedidos' referencing 'clientes(id)' with ON DELETE CASCADE.",
                starterCode = "CREATE TABLE pedidos (\n  id INT AUTO_INCREMENT PRIMARY KEY,\n  cliente_id INT NOT NULL,\n  FOREIGN KEY (cliente_id) REFERENCES ___(id) ON DELETE ___\n) ENGINE=InnoDB;",
                solutionCode = "CREATE TABLE pedidos (\n  id INT AUTO_INCREMENT PRIMARY KEY,\n  cliente_id INT NOT NULL,\n  FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE\n) ENGINE=InnoDB;",
                hintsPt = listOf("A tabela referenciada é 'clientes'.", "A ação em cascata é 'ON DELETE CASCADE'."),
                hintsEn = listOf("Target table is 'clientes'.", "Cascade action is 'ON DELETE CASCADE'."),
                requiredKeywords = listOf("FOREIGN KEY", "REFERENCES", "CASCADE"),
                explanationPt = "Chaves estrangeiras com CASCADE evitam orfandade de dados mantendo a consistência do banco de dados relacional.",
                explanationEn = "Cascading foreign keys maintain referential consistency automatically during deletion events."
            )
        )

        Technology.POSTGRESQL -> listOf(
            PracticalActivity(
                id = "pg_act_1",
                tech = Technology.POSTGRESQL,
                titlePt = "Filtros com ILIKE e Operador BETWEEN",
                titleEn = "Filtering with ILIKE and BETWEEN",
                difficulty = ActivityDifficulty.BEGINNER,
                pointsValue = 30,
                instructionsPt = "No PostgreSQL, escreva uma consulta filtrando 'nome' insensível a maiúsculas (ILIKE '%tech%') e 'preco' BETWEEN 50 AND 200.",
                instructionsEn = "In PostgreSQL, write a query using ILIKE '%tech%' and 'preco' BETWEEN 50 AND 200.",
                starterCode = "SELECT id, nome, preco\nFROM servicos\nWHERE nome ILIKE '%tech%'\n  AND preco BETWEEN ___ AND ___;\n",
                solutionCode = "SELECT id, nome, preco\nFROM servicos\nWHERE nome ILIKE '%tech%'\n  AND preco BETWEEN 50 AND 200;",
                hintsPt = listOf("O intervalo mínimo é 50 e o máximo é 200.", "ILIKE é a cláusula nativa do PostgreSQL para buscas case-insensitive."),
                hintsEn = listOf("Values are 50 and 200.", "ILIKE is PostgreSQL's case-insensitive matching operator."),
                requiredKeywords = listOf("SELECT", "ILIKE", "BETWEEN", "AND"),
                explanationPt = "O operador ILIKE no PostgreSQL evita funções LOWER() desnecessárias e aproveita índices de texto.",
                explanationEn = "ILIKE performs case-insensitive regex pattern matching natively in PostgreSQL."
            ),
            PracticalActivity(
                id = "pg_act_2",
                tech = Technology.POSTGRESQL,
                titlePt = "Consultas Avançadas em Colunas JSONB",
                titleEn = "Querying JSONB Document Columns",
                difficulty = ActivityDifficulty.INTERMEDIATE,
                pointsValue = 60,
                instructionsPt = "Escreva uma consulta SELECT que filtre a tabela 'produtos' onde a coluna JSONB 'detalhes' contenha '{\"em_estoque\": true}' usando o operador de contenção '@>'.",
                instructionsEn = "Write a query filtering 'produtos' where JSONB column 'detalhes' has '{\"em_estoque\": true}' via '@>'.",
                starterCode = "SELECT id, nome, detalhes->>'cor' AS cor\nFROM produtos\nWHERE detalhes ___ '{\"em_estoque\": true}';",
                solutionCode = "SELECT id, nome, detalhes->>'cor' AS cor\nFROM produtos\nWHERE detalhes @> '{\"em_estoque\": true}';",
                hintsPt = listOf("No PostgreSQL, o operador de contenção JSONB é o '@>'.", "Ele verifica se o objeto da esquerda engloba o da direita."),
                hintsEn = listOf("Containment operator is '@>'.", "It verifies if the left document contains the right document."),
                requiredKeywords = listOf("SELECT", "detalhes", "@>"),
                explanationPt = "O operador @> aproveita índices GIN no PostgreSQL para realizar buscas semânticas instantâneas em JSONB.",
                explanationEn = "The @> containment operator leverages PostgreSQL GIN indexes for fast document queries."
            ),
            PracticalActivity(
                id = "pg_act_3",
                tech = Technology.POSTGRESQL,
                titlePt = "Criação de Índice GIN para JSONB",
                titleEn = "Creating GIN Index for JSONB",
                difficulty = ActivityDifficulty.ADVANCED,
                pointsValue = 90,
                instructionsPt = "Crie um índice de alto desempenho usando GIN na coluna 'dados' da tabela 'eventos'.",
                instructionsEn = "Create a high-performance GIN index on column 'dados' of table 'eventos'.",
                starterCode = "CREATE INDEX idx_eventos_dados\nON eventos USING ___ (dados);",
                solutionCode = "CREATE INDEX idx_eventos_dados\nON eventos USING GIN (dados);",
                hintsPt = listOf("O tipo de índice é GIN (Generalized Inverted Index).", "A sintaxe é: USING GIN (coluna)."),
                hintsEn = listOf("Index algorithm is GIN.", "Syntax: USING GIN (column)."),
                requiredKeywords = listOf("CREATE INDEX", "USING", "GIN"),
                explanationPt = "Índices GIN dividem o JSONB em chaves e valores indexados individualmente, acelerando filtros complexos.",
                explanationEn = "GIN indexes index individual JSONB keys and values, accelerating complex dynamic queries."
            )
        )

        Technology.MONGODB -> listOf(
            PracticalActivity(
                id = "mongo_act_1",
                tech = Technology.MONGODB,
                titlePt = "Inserção de Documento BSON",
                titleEn = "Inserting a BSON Document",
                difficulty = ActivityDifficulty.BEGINNER,
                pointsValue = 30,
                instructionsPt = "Escreva o comando para inserir um documento na coleção 'usuarios' contendo nome 'Lucas' e status 'ativo'.",
                instructionsEn = "Write the command inserting a document into 'usuarios' with name 'Lucas' and status 'ativo'.",
                starterCode = "db.usuarios.___({ \n  nome: \"Lucas\", \n  status: \"ativo\" \n});",
                solutionCode = "db.usuarios.insertOne({ \n  nome: \"Lucas\", \n  status: \"ativo\" \n});",
                hintsPt = listOf("Para inserir um único documento no MongoDB moderno use 'insertOne'.", "Passe o objeto JSON entre chaves."),
                hintsEn = listOf("Use 'insertOne' to persist a document.", "Pass the document within curly braces."),
                requiredKeywords = listOf("usuarios", "insertOne", "nome", "status"),
                explanationPt = "insertOne grava documentos com validação de esquema BSON e gera automaticamente o campo _id único.",
                explanationEn = "insertOne writes BSON documents and generates unique ObjectId identifiers."
            ),
            PracticalActivity(
                id = "mongo_act_2",
                tech = Technology.MONGODB,
                titlePt = "Filtro e Ordenação com \$gte e Sort",
                titleEn = "Filtering and Sorting with \$gte and Sort",
                difficulty = ActivityDifficulty.INTERMEDIATE,
                pointsValue = 60,
                instructionsPt = "Escreva o comando db.produtos.find() para buscar documentos onde 'preco' seja maior ou igual a 50 (usando \$gte) e ordenados de forma decrescente por data (-1).",
                instructionsEn = "Write db.produtos.find() query filtering 'preco' >= 50 (\$gte) and sorted descending (-1).",
                starterCode = "db.produtos.find({\n  preco: { ___: 50 }\n}).sort({ criado_em: ___ });",
                solutionCode = "db.produtos.find({\n  preco: { \$gte: 50 }\n}).sort({ criado_em: -1 });",
                hintsPt = listOf("O operador de maior ou igual é '\$gte'.", "Para ordenação decrescente no MongoDB use -1."),
                hintsEn = listOf("Greater-than-or-equal is '\$gte'.", "Descending order is -1."),
                requiredKeywords = listOf("find", "preco", "\$gte", "sort"),
                explanationPt = "Consultas no MongoDB utilizam operadores baseados em documentos BSON como \$gte, \$lte e \$in.",
                explanationEn = "MongoDB queries employ BSON operators such as \$gte, \$lte, and \$in to filter documents."
            ),
            PracticalActivity(
                id = "mongo_act_3",
                tech = Technology.MONGODB,
                titlePt = "Aggregation Pipeline com \$match e \$group",
                titleEn = "Aggregation Pipeline with \$match and \$group",
                difficulty = ActivityDifficulty.ADVANCED,
                pointsValue = 90,
                instructionsPt = "Construa um pipeline de agregação no MongoDB que filtre pedidos com status 'concluido' no primeiro estágio (\$match) e calcule o total de vendas por categoria (\$group com \$sum).",
                instructionsEn = "Build an aggregation pipeline filtering 'concluido' orders with \$match, then calculating total sales per category with \$group and \$sum.",
                starterCode = "db.pedidos.aggregate([\n  { ___: { status: \"concluido\" } },\n  { ___: { _id: \"\$categoria\", totalVendas: { ___: \"\$valor\" } } }\n]);",
                solutionCode = "db.pedidos.aggregate([\n  { \$match: { status: \"concluido\" } },\n  { \$group: { _id: \"\$categoria\", totalVendas: { \$sum: \"\$valor\" } } }\n]);",
                hintsPt = listOf("O primeiro estágio é '\$match'.", "O segundo estágio agrupa com '\$group' e soma com '\$sum'."),
                hintsEn = listOf("First stage is '\$match'.", "Second stage groups with '\$group' and sums with '\$sum'."),
                requiredKeywords = listOf("aggregate", "\$match", "\$group", "\$sum"),
                explanationPt = "O Aggregation Pipeline é a ferramenta nativa do MongoDB para processar e analisar dados em múltiplos estágios encadeados.",
                explanationEn = "Aggregation Pipeline is MongoDB's native multi-stage data processing and analytics engine."
            )
        )
    }
}
