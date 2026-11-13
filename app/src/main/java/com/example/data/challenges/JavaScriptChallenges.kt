package com.example.data.challenges

import com.example.model.ActivityDifficulty
import com.example.model.PracticalActivity
import com.example.model.Technology

object JavaScriptChallenges {

    private data class JsSeed(
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
        JsSeed(
            titlePt = "Template Literals & Interpolação",
            titleEn = "Template Literals & Interpolation",
            instructionsPt = "Formate a string de boas-vindas usando template literals com crases (`) e interpolação \${}.",
            instructionsEn = "Format the welcome string using backticks (`) and \${} interpolation.",
            starterCode = "const dev = \"Carlos\";\nconst stack = \"Frontend\";\n// Complete: 'Dev Carlos foca em Frontend'\nconst msg = `Dev \${___} foca em \${___}`;\nconsole.log(msg);",
            solutionCode = "const dev = \"Carlos\";\nconst stack = \"Frontend\";\nconst msg = `Dev \${dev} foca em \${stack}`;\nconsole.log(msg);",
            hintPt = "Insira dev e stack dentro dos placeholders \${}.",
            hintEn = "Insert dev and stack inside the \${} placeholders.",
            keywords = listOf("dev", "stack", "`"),
            explanationPt = "Template literals com crases permitem interpolação limpa sem concatenação (+) verbosa.",
            explanationEn = "Template literals allow concise variable interpolation without verbose concatenation."
        ),
        JsSeed(
            titlePt = "Arrow Functions com Retorno Implícito",
            titleEn = "Arrow Functions with Implicit Return",
            instructionsPt = "Crie uma arrow function de uma linha para calcular o quadrado de um número com retorno implícito.",
            instructionsEn = "Create a one-line arrow function that calculates the square of a number with implicit return.",
            starterCode = "const quadrado = n => n * ___;\nconsole.log(quadrado(5)); // 25",
            solutionCode = "const quadrado = n => n * n;\nconsole.log(quadrado(5));",
            hintPt = "Multiplique n por ele mesmo: n * n.",
            hintEn = "Multiply n by itself: n * n.",
            keywords = listOf("quadrado", "=>", "*"),
            explanationPt = "Arrow functions com expressão direta retornam o valor avaliado automaticamente.",
            explanationEn = "Arrow functions with a single expression body implicitly return the evaluated result."
        ),
        JsSeed(
            titlePt = "Operador Ternário Conciso",
            titleEn = "Ternary Operator",
            instructionsPt = "Use o operador ternário (? :) para definir o status como 'Aprovado' se a média for >= 7, ou 'Reprovado'.",
            instructionsEn = "Use the ternary operator (? :) to set status to 'Aprovado' if average >= 7, else 'Reprovado'.",
            starterCode = "const media = 8.5;\nconst status = media >= 7 ? \"___\" : \"Reprovado\";\nconsole.log(status);",
            solutionCode = "const media = 8.5;\nconst status = media >= 7 ? \"Aprovado\" : \"Reprovado\";\nconsole.log(status);",
            hintPt = "A primeira opção após '?' é executada quando a condição for verdadeira.",
            hintEn = "The branch immediately following '?' evaluates when truthy.",
            keywords = listOf("media", "?", "Aprovado", "Reprovado"),
            explanationPt = "O operador ternário é a forma idiomática para atribuições condicionais em uma linha.",
            explanationEn = "Ternary expressions offer a concise shorthand for inline conditional assignments."
        ),
        JsSeed(
            titlePt = "Destructuring de Objetos",
            titleEn = "Object Destructuring",
            instructionsPt = "Extraia as propriedades 'titulo' e 'ano' do objeto livro usando desestruturação {}.",
            instructionsEn = "Extract 'titulo' and 'ano' from the book object using object destructuring {}.",
            starterCode = "const livro = { titulo: \"Clean Code\", autor: \"Uncle Bob\", ano: 2008 };\nconst { ___, ___ } = livro;\nconsole.log(`\${titulo} (\${ano})`);",
            solutionCode = "const livro = { titulo: \"Clean Code\", autor: \"Uncle Bob\", ano: 2008 };\nconst { titulo, ano } = livro;\nconsole.log(`\${titulo} (\${ano})`);",
            hintPt = "Coloque os nomes das propriedades entre chaves: { titulo, ano }.",
            hintEn = "Place matching property keys inside curly braces: { titulo, ano }.",
            keywords = listOf("titulo", "ano", "livro"),
            explanationPt = "Desestruturação extrai campos diretamente para variáveis locais correspondentes.",
            explanationEn = "Object destructuring unpacks properties directly into matching local variables."
        ),
        JsSeed(
            titlePt = "Arrays: push() e Propriedade length",
            titleEn = "Arrays: push() and length Property",
            instructionsPt = "Adicione 'Node.js' ao final do array com push() e exiba o tamanho total.",
            instructionsEn = "Append 'Node.js' to the array using push() and log the array length.",
            starterCode = "const techs = [\"React\", \"Vue\"];\ntechs.push(\"___\");\nconsole.log(techs.length);",
            solutionCode = "const techs = [\"React\", \"Vue\"];\ntechs.push(\"Node.js\");\nconsole.log(techs.length);",
            hintPt = "Passe 'Node.js' como argumento do método push().",
            hintEn = "Pass 'Node.js' as argument to push().",
            keywords = listOf("push", "Node.js", "length"),
            explanationPt = "push() adiciona elementos ao final do array e atualiza automaticamente .length.",
            explanationEn = "push() appends items to the end of an array and updates its length."
        ),
        JsSeed(
            titlePt = "Declaração Segura com const e let",
            titleEn = "Safe Declarations with const and let",
            instructionsPt = "Use 'const' para a taxa fixa e 'let' para o contador mutável.",
            instructionsEn = "Use 'const' for the constant rate and 'let' for the mutable counter.",
            starterCode = "___ TAXA = 0.15;\n___ contador = 0;\ncontador += 1;\nconsole.log(TAXA, contador);",
            solutionCode = "const TAXA = 0.15;\nlet contador = 0;\ncontador += 1;\nconsole.log(TAXA, contador);",
            hintPt = "Use const primeiro e let na segunda linha.",
            hintEn = "Use const for TAXA and let for contador.",
            keywords = listOf("const TAXA", "let contador"),
            explanationPt = "const previne reatribuições acidentais enquanto let permite escopo de bloco mutável.",
            explanationEn = "const prevents accidental reassignments while let provides block-scoped mutability."
        ),
        JsSeed(
            titlePt = "Métodos de String: trim() e toUpperCase()",
            titleEn = "String Methods: trim() and toUpperCase()",
            instructionsPt = "Remova espaços extras das extremidades usando trim() e converta para maiúsculas.",
            instructionsEn = "Strip surrounding whitespace using trim() and convert text to uppercase.",
            starterCode = "const entrada = \"  javascript  \";\nconst limpo = entrada.___().toUpperCase();\nconsole.log(limpo);",
            solutionCode = "const entrada = \"  javascript  \";\nconst limpo = entrada.trim().toUpperCase();\nconsole.log(limpo);",
            hintPt = "Chame .trim() antes de .toUpperCase().",
            hintEn = "Call .trim() prior to .toUpperCase().",
            keywords = listOf("trim()", "toUpperCase()"),
            explanationPt = "Encadear métodos de string cria normalização limpa para formulários e entradas.",
            explanationEn = "Chaining string methods yields clean normalization for user input."
        ),
        JsSeed(
            titlePt = "Parâmetros Padrão em Funções",
            titleEn = "Default Function Parameters",
            instructionsPt = "Defina 'Visitante' como valor padrão do parâmetro nome na função saudar().",
            instructionsEn = "Set 'Visitante' as the default value for parameter nome in saudar().",
            starterCode = "function saudar(nome = \"___\") {\n    return `Olá, \${nome}!`;\n}\nconsole.log(saudar());",
            solutionCode = "function saudar(nome = \"Visitante\") {\n    return `Olá, \${nome}!`;\n}\nconsole.log(saudar());",
            hintPt = "Preencha a string padrão com Visitante.",
            hintEn = "Fill the default string with Visitante.",
            keywords = listOf("function saudar", "Visitante"),
            explanationPt = "Parâmetros padrão entram em ação automaticamente quando o argumento é omitido ou undefined.",
            explanationEn = "Default parameters activate automatically whenever an argument is omitted or undefined."
        ),
        JsSeed(
            titlePt = "Verificação com Array.includes()",
            titleEn = "Membership Check with Array.includes()",
            instructionsPt = "Verifique se o array de permissões contém 'ADMIN' usando o método includes().",
            instructionsEn = "Check whether the permissions array contains 'ADMIN' using includes().",
            starterCode = "const roles = [\"USER\", \"EDITOR\", \"ADMIN\"];\nconst ehAdmin = roles.___(\"ADMIN\");\nconsole.log(ehAdmin);",
            solutionCode = "const roles = [\"USER\", \"EDITOR\", \"ADMIN\"];\nconst ehAdmin = roles.includes(\"ADMIN\");\nconsole.log(ehAdmin);",
            hintPt = "Use o método .includes().",
            hintEn = "Call the .includes() method.",
            keywords = listOf("includes", "ADMIN"),
            explanationPt = "includes() retorna um booleano direto indicando se o elemento existe no array.",
            explanationEn = "includes() returns a direct boolean indicating element presence."
        ),
        JsSeed(
            titlePt = "Laço for...of em Arrays",
            titleEn = "Iterating Arrays with for...of",
            instructionsPt = "Percorra todos os itens da lista de linguagens usando o laço for...of.",
            instructionsEn = "Iterate over all items in the languages list using a for...of loop.",
            starterCode = "const langs = [\"JS\", \"TS\", \"Kotlin\"];\nfor (const lang ___ langs) {\n    console.log(lang);\n}",
            solutionCode = "const langs = [\"JS\", \"TS\", \"Kotlin\"];\nfor (const lang of langs) {\n    console.log(lang);\n}",
            hintPt = "Use a palavra-chave 'of' para iterar valores.",
            hintEn = "Use the 'of' keyword to iterate over iterable values.",
            keywords = listOf("for", "of", "langs"),
            explanationPt = "for...of itera diretamente sobre os valores de objetos iteráveis como Arrays, Strings, Maps e Sets.",
            explanationEn = "for...of iterates directly over values of any iterable like Arrays, Strings, Maps, and Sets."
        )
    )

    private val intermediateSeeds = listOf(
        JsSeed(
            titlePt = "Transformação de Dados com Array.map()",
            titleEn = "Data Transformation with Array.map()",
            instructionsPt = "Transforme a lista de preços numéricos em valores com 10% de acréscimo usando map().",
            instructionsEn = "Transform the list of prices adding 10% markup using map().",
            starterCode = "const precos = [100, 200, 300];\nconst precosComAumento = precos.map(p => p * ___);\nconsole.log(precosComAumento);",
            solutionCode = "const precos = [100, 200, 300];\nconst precosComAumento = precos.map(p => p * 1.1);\nconsole.log(precosComAumento);",
            hintPt = "Multiplicar por 1.1 aplica um aumento de 10%.",
            hintEn = "Multiplying by 1.1 applies a 10% increase.",
            keywords = listOf("map", "precos", "1.1"),
            explanationPt = "map() cria um novo array aplicando uma função de transformação sem mutar o original.",
            explanationEn = "map() returns a new array with transformed elements without mutating the source array."
        ),
        JsSeed(
            titlePt = "Filtragem Declarativa com Array.filter()",
            titleEn = "Declarative Filtering with Array.filter()",
            instructionsPt = "Filtre apenas os usuários com idade maior ou igual a 18 anos.",
            instructionsEn = "Filter only users who are 18 years or older.",
            starterCode = "const users = [{ name: \"Lucas\", age: 17 }, { name: \"Julia\", age: 21 }];\nconst adultos = users.filter(u => u.age >= ___);\nconsole.log(adultos.length);",
            solutionCode = "const users = [{ name: \"Lucas\", age: 17 }, { name: \"Julia\", age: 21 }];\nconst adultos = users.filter(u => u.age >= 18);\nconsole.log(adultos.length);",
            hintPt = "A condição deve verificar se u.age é maior ou igual a 18.",
            hintEn = "Check if u.age is greater than or equal to 18.",
            keywords = listOf("filter", "age", "18"),
            explanationPt = "filter() retorna um novo array contendo apenas os itens que satisfazem o predicado.",
            explanationEn = "filter() produces a shallow copy of elements passing the predicate test."
        ),
        JsSeed(
            titlePt = "Agregação de Totais com Array.reduce()",
            titleEn = "Total Aggregation with Array.reduce()",
            instructionsPt = "Calcule o total do carrinho somando as quantidades de todos os itens com reduce().",
            instructionsEn = "Calculate the shopping cart total quantity summing item counts with reduce().",
            starterCode = "const itens = [{ nome: \"Teclado\", qtd: 2 }, { nome: \"Mouse\", qtd: 1 }];\nconst totalItens = itens.reduce((acc, item) => acc + item.___, 0);\nconsole.log(totalItens);",
            solutionCode = "const itens = [{ nome: \"Teclado\", qtd: 2 }, { nome: \"Mouse\", qtd: 1 }];\nconst totalItens = itens.reduce((acc, item) => acc + item.qtd, 0);\nconsole.log(totalItens);",
            hintPt = "Acesse a propriedade qtd do item.",
            hintEn = "Access item.qtd inside the reducer callback.",
            keywords = listOf("reduce", "acc", "qtd", "0"),
            explanationPt = "reduce() acumula elementos do array em um único valor final.",
            explanationEn = "reduce() executes a reducer across array items returning a single accumulated value."
        ),
        JsSeed(
            titlePt = "Operador Spread (...) e Cópia Imutável",
            titleEn = "Spread Operator (...) & Immutability",
            instructionsPt = "Crie uma cópia atualizada adicionando 'debug: true' com o operador spread.",
            instructionsEn = "Create an updated config clone spreading baseConfig and setting 'debug: true'.",
            starterCode = "const baseConfig = { env: \"production\", port: 3000 };\nconst devConfig = { ...___, debug: true };\nconsole.log(devConfig.debug);",
            solutionCode = "const baseConfig = { env: \"production\", port: 3000 };\nconst devConfig = { ...baseConfig, debug: true };\nconsole.log(devConfig.debug);",
            hintPt = "Use ...baseConfig para copiar as propriedades originais.",
            hintEn = "Use ...baseConfig to copy existing properties.",
            keywords = listOf("...", "baseConfig", "debug"),
            explanationPt = "Spread (...) copia propriedades superficialmente preservando imutabilidade.",
            explanationEn = "The spread operator (...) shallow-copies object properties cleanly."
        ),
        JsSeed(
            titlePt = "Optional Chaining (?.) e Nullish Coalescing (??)",
            titleEn = "Optional Chaining (?.) & Nullish Coalescing (??)",
            instructionsPt = "Acesse a cidade com segurança e forneça 'Desconhecida' como fallback usando ??.",
            instructionsEn = "Safely read user's city and provide 'Desconhecida' fallback using ??.",
            starterCode = "const perfil = { id: 10, nome: \"Beatriz\" };\nconst cidade = perfil.endereco?.cidade ?? \"___\";\nconsole.log(cidade);",
            solutionCode = "const perfil = { id: 10, nome: \"Beatriz\" };\nconst cidade = perfil.endereco?.cidade ?? \"Desconhecida\";\nconsole.log(cidade);",
            hintPt = "Preencha com 'Desconhecida'.",
            hintEn = "Fill in with 'Desconhecida'.",
            keywords = listOf("?.", "??", "Desconhecida"),
            explanationPt = "?. evita TypeError em referências nulas e ?? só substitui null/undefined.",
            explanationEn = "?. prevents TypeError on nullish chains while ?? provides fallbacks strictly for null/undefined."
        ),
        JsSeed(
            titlePt = "Busca de Elemento com Array.find()",
            titleEn = "Locating Elements with Array.find()",
            instructionsPt = "Encontre o primeiro produto cujo id seja exatamente igual a 2 utilizando find().",
            instructionsEn = "Find the first product whose id strictly equals 2 using find().",
            starterCode = "const produtos = [{ id: 1, nome: \"A\" }, { id: 2, nome: \"B\" }];\nconst alvo = produtos.___(p => p.id === 2);\nconsole.log(alvo?.nome);",
            solutionCode = "const produtos = [{ id: 1, nome: \"A\" }, { id: 2, nome: \"B\" }];\nconst alvo = produtos.find(p => p.id === 2);\nconsole.log(alvo?.nome);",
            hintPt = "Use o método .find().",
            hintEn = "Use the .find() method.",
            keywords = listOf("find", "p.id === 2"),
            explanationPt = "find() interrompe a busca assim que encontra o primeiro elemento correspondente.",
            explanationEn = "find() short-circuits as soon as the first matching element is located."
        ),
        JsSeed(
            titlePt = "Deduplicação de Array com Set",
            titleEn = "Array Deduplication with Set",
            instructionsPt = "Remova duplicatas do array de tags convertendo um Set de volta em Array via spread.",
            instructionsEn = "Deduplicate tags array by spreading a Set back into an Array.",
            starterCode = "const tags = [\"js\", \"node\", \"js\", \"react\"];\nconst unicas = [...new ___(tags)];\nconsole.log(unicas);",
            solutionCode = "const tags = [\"js\", \"node\", \"js\", \"react\"];\nconst unicas = [...new Set(tags)];\nconsole.log(unicas);",
            hintPt = "Use a estrutura nativa Set.",
            hintEn = "Use the built-in Set collection.",
            keywords = listOf("new Set", "tags"),
            explanationPt = "Set armazena apenas valores únicos mantendo a ordem de inserção.",
            explanationEn = "Set stores unique values while preserving insertion order."
        ),
        JsSeed(
            titlePt = "Serialização e Parsing de JSON",
            titleEn = "JSON Serialization & Parsing",
            instructionsPt = "Converta a string JSON recebida em objeto JavaScript usando JSON.parse().",
            instructionsEn = "Parse the incoming JSON string into a JavaScript object using JSON.parse().",
            starterCode = "const payload = '{\"status\":\"ok\",\"xp\":120}';\nconst obj = JSON.___(payload);\nconsole.log(obj.xp);",
            solutionCode = "const payload = '{\"status\":\"ok\",\"xp\":120}';\nconst obj = JSON.parse(payload);\nconsole.log(obj.xp);",
            hintPt = "Use JSON.parse() para desserializar texto JSON.",
            hintEn = "Use JSON.parse() to deserialize JSON strings.",
            keywords = listOf("JSON.parse", "payload"),
            explanationPt = "JSON.parse e JSON.stringify são fundamentais para comunicação com APIs REST.",
            explanationEn = "JSON.parse and JSON.stringify power REST API data exchange."
        ),
        JsSeed(
            titlePt = "Iteração de Chaves/Valores com Object.entries()",
            titleEn = "Object Iteration with Object.entries()",
            instructionsPt = "Transforme o objeto de pontuações em pares [chave, valor] usando Object.entries().",
            instructionsEn = "Convert the scores object into [key, value] pairs using Object.entries().",
            starterCode = "const scores = { python: 90, js: 120 };\nfor (const [tech, pts] of Object.___(scores)) {\n    console.log(`\${tech}: \${pts}`);\n}",
            solutionCode = "const scores = { python: 90, js: 120 };\nfor (const [tech, pts] of Object.entries(scores)) {\n    console.log(`\${tech}: \${pts}`);\n}",
            hintPt = "Use Object.entries(scores).",
            hintEn = "Use Object.entries(scores).",
            keywords = listOf("Object.entries", "scores"),
            explanationPt = "Object.entries() facilita percorrer chaves e valores simultaneamente via desestruturação.",
            explanationEn = "Object.entries() pairs keys and values seamlessly for destructured iteration."
        ),
        JsSeed(
            titlePt = "Parâmetros Rest (...) em Funções Variádicas",
            titleEn = "Rest Parameters (...) in Variadic Functions",
            instructionsPt = "Capture múltiplos argumentos numéricos em um array usando o operador rest (...nums).",
            instructionsEn = "Collect variable numeric arguments into an array using rest syntax (...nums).",
            starterCode = "function somarTudo(___nums) {\n    return nums.reduce((a, b) => a + b, 0);\n}\nconsole.log(somarTudo(10, 20, 30));",
            solutionCode = "function somarTudo(...nums) {\n    return nums.reduce((a, b) => a + b, 0);\n}\nconsole.log(somarTudo(10, 20, 30));",
            hintPt = "Adicione três pontos '...' antes de nums.",
            hintEn = "Prefix nums with ellipsis '...'.",
            keywords = listOf("...nums", "reduce"),
            explanationPt = "Parâmetros rest agrupam número indefinido de argumentos em um Array real.",
            explanationEn = "Rest parameters gather an indefinite number of arguments into a true Array."
        )
    )

    private val advancedSeeds = listOf(
        JsSeed(
            titlePt = "Criação de Promises e Resolução Assíncrona",
            titleEn = "Promise Creation and Async Resolution",
            instructionsPt = "Complete a Promise chamando resolve('Sucesso!') após verificar ok.",
            instructionsEn = "Complete the Promise by calling resolve('Sucesso!') after verifying ok status.",
            starterCode = "const buscarDados = () => new Promise((resolve, reject) => {\n    const ok = true;\n    if (ok) ___(\"Sucesso!\");\n    else reject(new Error(\"Falha\"));\n});\nbuscarDados().then(res => console.log(res));",
            solutionCode = "const buscarDados = () => new Promise((resolve, reject) => {\n    const ok = true;\n    if (ok) resolve(\"Sucesso!\");\n    else reject(new Error(\"Falha\"));\n});\nbuscarDados().then(res => console.log(res));",
            hintPt = "Chame resolve com a string de sucesso.",
            hintEn = "Invoke resolve with the success value.",
            keywords = listOf("Promise", "resolve", "reject", "then"),
            explanationPt = "Promises representam valores assíncronos futuros e evitam callback hell.",
            explanationEn = "Promises model eventual completion or failure of async operations."
        ),
        JsSeed(
            titlePt = "Consumo Assíncrono com Async / Await",
            titleEn = "Async / Await Consumption & Error Handling",
            instructionsPt = "Aguarde a resposta de fetchApi() usando await dentro do bloco try.",
            instructionsEn = "Await the response of fetchApi() using await inside the try block.",
            starterCode = "async function executar() {\n    try {\n        const resposta = ___ fetchApi();\n        console.log(\"Recebido:\", resposta);\n    } catch (erro) {\n        console.error(erro.message);\n    }\n}",
            solutionCode = "async function executar() {\n    try {\n        const resposta = await fetchApi();\n        console.log(\"Recebido:\", resposta);\n    } catch (erro) {\n        console.error(erro.message);\n    }\n}",
            hintPt = "Use a palavra-chave await antes de fetchApi().",
            hintEn = "Prepend await before fetchApi().",
            keywords = listOf("async", "await", "try", "catch"),
            explanationPt = "async/await traz legibilidade síncrona para fluxos assíncronos baseados em Promises.",
            explanationEn = "async/await structures Promise pipelines with clean synchronous-like control flow."
        ),
        JsSeed(
            titlePt = "Execução Concorrente com Promise.all()",
            titleEn = "Concurrent Execution with Promise.all()",
            instructionsPt = "Dispare duas promises em paralelo usando Promise.all() e desestruture [user, posts].",
            instructionsEn = "Run two promises concurrently with Promise.all() and destructure [user, posts].",
            starterCode = "async function carregarDashboard() {\n    const [user, posts] = await Promise.___([buscarUsuario(), buscarPosts()]);\n    return { user, posts };\n}",
            solutionCode = "async function carregarDashboard() {\n    const [user, posts] = await Promise.all([buscarUsuario(), buscarPosts()]);\n    return { user, posts };\n}",
            hintPt = "Use Promise.all() para executar em paralelo.",
            hintEn = "Use Promise.all() for concurrent execution.",
            keywords = listOf("Promise.all", "await"),
            explanationPt = "Promise.all executa chamadas independentes simultaneamente otimizando latência.",
            explanationEn = "Promise.all coordinates independent promises in parallel to reduce total latency."
        ),
        JsSeed(
            titlePt = "Closures e Encapsulamento de Estado",
            titleEn = "Closures & State Encapsulation",
            instructionsPt = "Complete a closure que incrementa a variável privada contador em 1 a cada chamada.",
            instructionsEn = "Complete the closure incrementing private state contador by 1 on each call.",
            starterCode = "function criarContador() {\n    let contador = 0;\n    return function() {\n        contador += ___;\n        return contador;\n    };\n}\nconst contar = criarContador();",
            solutionCode = "function criarContador() {\n    let contador = 0;\n    return function() {\n        contador += 1;\n        return contador;\n    };\n}\nconst contar = criarContador();",
            hintPt = "Incremente somando 1.",
            hintEn = "Increment by 1.",
            keywords = listOf("contador", "return", "function"),
            explanationPt = "Closures preservam acesso ao escopo léxico externo mesmo após a função pai retornar.",
            explanationEn = "Closures retain access to outer lexical variables after the outer function completes."
        ),
        JsSeed(
            titlePt = "Classes ES6 e Herança com Super",
            titleEn = "ES6 Classes and Inheritance with Super",
            instructionsPt = "Chame o construtor pai com super(nome) na subclasse Desenvolvedor.",
            instructionsEn = "Invoke parent constructor via super(nome) inside subclass Desenvolvedor.",
            starterCode = "class Funcionario {\n    constructor(nome) { this.nome = nome; }\n}\nclass Desenvolvedor extends Funcionario {\n    constructor(nome, stack) {\n        ___(nome);\n        this.stack = stack;\n    }\n}",
            solutionCode = "class Funcionario {\n    constructor(nome) { this.nome = nome; }\n}\nclass Desenvolvedor extends Funcionario {\n    constructor(nome, stack) {\n        super(nome);\n        this.stack = stack;\n    }\n}",
            hintPt = "Invoque super(nome).",
            hintEn = "Call super(nome).",
            keywords = listOf("class", "extends", "super", "constructor"),
            explanationPt = "super() inicializa a superclasse antes de acessar 'this' na classe filha.",
            explanationEn = "super() initializes the parent constructor prior to accessing 'this' in subclasses."
        ),
        JsSeed(
            titlePt = "Função Debounce para Otimização de Eventos",
            titleEn = "Debounce Higher-Order Function",
            instructionsPt = "Agende a execução atrasada da função com setTimeout dentro do debounce.",
            instructionsEn = "Schedule delayed invocation via setTimeout inside the debounce wrapper.",
            starterCode = "function debounce(fn, delay) {\n    let timer;\n    return (...args) => {\n        clearTimeout(timer);\n        timer = ___( () => fn(...args), delay );\n    };\n}",
            solutionCode = "function debounce(fn, delay) {\n    let timer;\n    return (...args) => {\n        clearTimeout(timer);\n        timer = setTimeout(() => fn(...args), delay);\n    };\n}",
            hintPt = "Use a função global setTimeout.",
            hintEn = "Use global setTimeout.",
            keywords = listOf("clearTimeout", "setTimeout", "delay"),
            explanationPt = "Debounce evita disparos excessivos de APIs durante digitação contínua ou scroll.",
            explanationEn = "Debouncing prevents redundant API calls during rapid typing or scroll events."
        ),
        JsSeed(
            titlePt = "Estrutura Map para Cache de Alta Performance",
            titleEn = "Using Map for High-Performance Cache",
            instructionsPt = "Armazene o par chave-valor no Map usando cache.set() e recupere com cache.get().",
            instructionsEn = "Store key-value pairs in a Map with cache.set() and read via cache.get().",
            starterCode = "const cache = new Map();\ncache.___(\"user_1\", { nome: \"Ana\" });\nconsole.log(cache.get(\"user_1\"));",
            solutionCode = "const cache = new Map();\ncache.set(\"user_1\", { nome: \"Ana\" });\nconsole.log(cache.get(\"user_1\"));",
            hintPt = "Chame .set() no objeto Map.",
            hintEn = "Call .set() on the Map instance.",
            keywords = listOf("new Map", ".set(", ".get("),
            explanationPt = "Map oferece lookups O(1), preserva ordem de inserção e aceita chaves de qualquer tipo.",
            explanationEn = "Map provides O(1) lookup complexity and supports arbitrary key types."
        ),
        JsSeed(
            titlePt = "Generators com function* e yield",
            titleEn = "Generators with function* and yield",
            instructionsPt = "Emita o próximo identificador usando a palavra-chave 'yield' dentro do generator.",
            instructionsEn = "Emit the next identifier using the 'yield' keyword inside the generator.",
            starterCode = "function* gerarIds() {\n    let id = 1;\n    while (true) {\n        ___ id++;\n    }\n}\nconst gen = gerarIds();",
            solutionCode = "function* gerarIds() {\n    let id = 1;\n    while (true) {\n        yield id++;\n    }\n}\nconst gen = gerarIds();",
            hintPt = "Use 'yield' para pausar e retornar o valor.",
            hintEn = "Use 'yield' to pause execution and emit the value.",
            keywords = listOf("function*", "yield", "id++"),
            explanationPt = "Generators produzem sequências sob demanda sem alocar toda a lista em memória.",
            explanationEn = "Generators compute values lazily on demand without allocating full sequences."
        ),
        JsSeed(
            titlePt = "Resiliência com Promise.allSettled()",
            titleEn = "Resilience with Promise.allSettled()",
            instructionsPt = "Use Promise.allSettled() para aguardar todas as requisições mesmo se alguma falhar.",
            instructionsEn = "Use Promise.allSettled() to collect all outcomes even when individual promises reject.",
            starterCode = "async function sincronizarServicos(reqs) {\n    const resultados = await Promise.___(reqs);\n    return resultados.filter(r => r.status === \"fulfilled\");\n}",
            solutionCode = "async function sincronizarServicos(reqs) {\n    const resultados = await Promise.allSettled(reqs);\n    return resultados.filter(r => r.status === \"fulfilled\");\n}",
            hintPt = "O método é allSettled.",
            hintEn = "Use the allSettled static method.",
            keywords = listOf("Promise.allSettled", "fulfilled"),
            explanationPt = "Diferente de Promise.all, allSettled nunca aborta quando uma promise individual rejeita.",
            explanationEn = "Unlike Promise.all, allSettled never short-circuits when a single promise rejects."
        ),
        JsSeed(
            titlePt = "Currying e Composição Funcional",
            titleEn = "Currying & Functional Composition",
            instructionsPt = "Complete a função curried multiplicar(a)(b) retornando uma arrow function interna.",
            instructionsEn = "Complete the curried function multiplicar(a)(b) returning an inner arrow function.",
            starterCode = "const multiplicar = a => ___ => a * b;\nconst dobrar = multiplicar(2);\nconsole.log(dobrar(15));",
            solutionCode = "const multiplicar = a => b => a * b;\nconst dobrar = multiplicar(2);\nconsole.log(dobrar(15));",
            hintPt = "Retorne 'b => a * b'.",
            hintEn = "Accept parameter b in the inner arrow function.",
            keywords = listOf("=> b =>", "a * b"),
            explanationPt = "Currying transforma funções de múltiplos argumentos em cadeias de funções unárias reutilizáveis.",
            explanationEn = "Currying transforms multi-argument functions into reusable single-argument chains."
        )
    )

    val list: List<PracticalActivity> by lazy {
        val beginner = (1..35).map { idx ->
            val seed = beginnerSeeds[(idx - 1) % beginnerSeeds.size]
            val suffix = if (idx <= beginnerSeeds.size) "" else " ($idx)"
            PracticalActivity(
                id = "js_act_$idx",
                tech = Technology.JAVASCRIPT,
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
                id = "js_act_$idx",
                tech = Technology.JAVASCRIPT,
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
                id = "js_act_$idx",
                tech = Technology.JAVASCRIPT,
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
