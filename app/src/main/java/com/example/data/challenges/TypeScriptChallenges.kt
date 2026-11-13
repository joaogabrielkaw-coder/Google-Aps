package com.example.data.challenges

import com.example.model.ActivityDifficulty
import com.example.model.PracticalActivity
import com.example.model.Technology

object TypeScriptChallenges {

    private data class TsSeed(
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
        TsSeed(
            titlePt = "Tipagem Estática Primitiva",
            titleEn = "Primitive Static Typing",
            instructionsPt = "Defina as anotações de tipo explícitas: nome (string), xp (number) e ativo (boolean).",
            instructionsEn = "Define explicit static type annotations: nome (string), xp (number), and ativo (boolean).",
            starterCode = "const devNome: ___ = \"Beatriz\";\nconst devXp: ___ = 1250;\nconst isAtivo: ___ = true;",
            solutionCode = "const devNome: string = \"Beatriz\";\nconst devXp: number = 1250;\nconst isAtivo: boolean = true;",
            hintPt = "Use os tipos primitivos em minúsculas: string, number e boolean.",
            hintEn = "Use lowercase primitive type keywords: string, number, boolean.",
            keywords = listOf("string", "number", "boolean"),
            explanationPt = "A tipagem estática do TypeScript captura erros de atribuição ainda no editor.",
            explanationEn = "TypeScript's static type checking catches assignment mistakes at compile time."
        ),
        TsSeed(
            titlePt = "Contratos de Dados com Interfaces",
            titleEn = "Data Contracts with Interfaces",
            instructionsPt = "Declare a interface Desenvolvedor com id (number), nome (string) e stack (string).",
            instructionsEn = "Declare Desenvolvedor interface with id (number), nome (string), and stack (string).",
            starterCode = "interface Desenvolvedor {\n    id: ___;\n    nome: ___;\n    stack: ___;\n}\nconst dev: Desenvolvedor = { id: 1, nome: \"Lucas\", stack: \"Fullstack\" };",
            solutionCode = "interface Desenvolvedor {\n    id: number;\n    nome: string;\n    stack: string;\n}\nconst dev: Desenvolvedor = { id: 1, nome: \"Lucas\", stack: \"Fullstack\" };",
            hintPt = "Preencha com number, string e string.",
            hintEn = "Fill in with number, string, and string.",
            keywords = listOf("interface", "Desenvolvedor", "number", "string"),
            explanationPt = "Interfaces modelam a forma exata de objetos garantindo consistência estrutural.",
            explanationEn = "Interfaces describe structural object shapes enforcing compile-time contracts."
        ),
        TsSeed(
            titlePt = "Propriedades Opcionais em Interfaces",
            titleEn = "Optional Properties in Interfaces",
            instructionsPt = "Torne a propriedade 'bio' opcional usando (?) na interface Perfil.",
            instructionsEn = "Make 'bio' optional using the question mark (?) modifier on Perfil interface.",
            starterCode = "interface Perfil {\n    username: string;\n    bio___: string;\n}\nconst userA: Perfil = { username: \"ana_code\" };",
            solutionCode = "interface Perfil {\n    username: string;\n    bio?: string;\n}\nconst userA: Perfil = { username: \"ana_code\" };",
            hintPt = "Adicione '?' logo após bio: bio?: string.",
            hintEn = "Add '?' immediately after bio: bio?: string.",
            keywords = listOf("interface", "Perfil", "bio?:"),
            explanationPt = "O modificador (?) marca campos que podem ser omitidos na criação do objeto.",
            explanationEn = "The optional modifier (?) marks fields that may safely be omitted."
        ),
        TsSeed(
            titlePt = "Union Types (|) em Parâmetros",
            titleEn = "Union Types (|) in Parameters",
            instructionsPt = "Aceite string ou number no parâmetro 'codigo' usando o operador pipe (|).",
            instructionsEn = "Allow either string or number for 'codigo' using the pipe union (|).",
            starterCode = "function formatarId(codigo: string ___ number): string {\n    return `ID-#\${codigo}`;\n}\nconsole.log(formatarId(404));",
            solutionCode = "function formatarId(codigo: string | number): string {\n    return `ID-#\${codigo}`;\n}\nconsole.log(formatarId(404));",
            hintPt = "Use a barra vertical '|' entre string e number.",
            hintEn = "Use the pipe '|' operator between string and number.",
            keywords = listOf("string", "|", "number"),
            explanationPt = "Union types permitem que uma variável aceite um dentre múltiplos tipos válidos.",
            explanationEn = "Union types enable variables or parameters to accept several valid types."
        ),
        TsSeed(
            titlePt = "Type Aliases e Literal Types",
            titleEn = "Type Aliases & Literal Types",
            instructionsPt = "Crie o type alias 'StatusPedido' aceitando apenas 'pendente', 'aprovado' ou 'cancelado'.",
            instructionsEn = "Declare type alias 'StatusPedido' allowing only 'pendente', 'aprovado', or 'cancelado'.",
            starterCode = "___ StatusPedido = \"pendente\" | \"aprovado\" | \"cancelado\";\nconst statusAtual: StatusPedido = \"aprovado\";",
            solutionCode = "type StatusPedido = \"pendente\" | \"aprovado\" | \"cancelado\";\nconst statusAtual: StatusPedido = \"aprovado\";",
            hintPt = "Use a palavra-chave 'type'.",
            hintEn = "Use the 'type' keyword.",
            keywords = listOf("type", "StatusPedido", "pendente", "aprovado"),
            explanationPt = "Literal union types garantem que strings inválidas sejam rejeitadas pelo compilador.",
            explanationEn = "Literal union types prevent arbitrary strings from entering business logic."
        ),
        TsSeed(
            titlePt = "Arrays Tipados (T[] e Array<T>)",
            titleEn = "Typed Arrays (T[] & Array<T>)",
            instructionsPt = "Anote corretamente a lista de pontuações como um array de números (number[]).",
            instructionsEn = "Annotate the scores list explicitly as a number array (number[]).",
            starterCode = "const notas: ___[] = [9.5, 8.0, 10.0];\nconsole.log(notas.length);",
            solutionCode = "const notas: number[] = [9.5, 8.0, 10.0];\nconsole.log(notas.length);",
            hintPt = "Use 'number' antes de [].",
            hintEn = "Use 'number' before [].",
            keywords = listOf("number[]", "notas"),
            explanationPt = "Arrays tipados garantem que todos os elementos pertençam ao mesmo tipo definido.",
            explanationEn = "Typed arrays ensure every element matches the declared item type."
        ),
        TsSeed(
            titlePt = "Tuplas Tipadas (Tuples)",
            titleEn = "Typed Tuples",
            instructionsPt = "Defina a tupla coordenada contendo exatamente [number, number].",
            instructionsEn = "Define a coordinate tuple containing exactly [number, number].",
            starterCode = "const ponto: [___, ___] = [23.55, -46.63];\nconsole.log(ponto[0], ponto[1]);",
            solutionCode = "const ponto: [number, number] = [23.55, -46.63];\nconsole.log(ponto[0], ponto[1]);",
            hintPt = "Preencha ambos os elementos da tupla com number.",
            hintEn = "Fill both tuple positions with number.",
            keywords = listOf("[number, number]", "ponto"),
            explanationPt = "Tuplas expressam arrays de tamanho fixo onde cada índice tem um tipo próprio.",
            explanationEn = "Tuples represent fixed-length arrays where each position has a known type."
        ),
        TsSeed(
            titlePt = "Retorno Explícito e void em Funções",
            titleEn = "Explicit Return & void in Functions",
            instructionsPt = "Anote a função logarEvento com retorno 'void' por não retornar nenhum valor.",
            instructionsEn = "Annotate logarEvento with return type 'void' since it returns nothing.",
            starterCode = "function logarEvento(evento: string): ___ {\n    console.log(`[LOG] \${evento}`);\n}",
            solutionCode = "function logarEvento(evento: string): void {\n    console.log(`[LOG] \${evento}`);\n}",
            hintPt = "Use a palavra-chave 'void' após os dois pontos.",
            hintEn = "Use the 'void' return type keyword.",
            keywords = listOf("void", "evento: string"),
            explanationPt = "void explicita que a função produz efeitos colaterais e não retorna valor útil.",
            explanationEn = "void signals that a function performs side effects without returning a value."
        ),
        TsSeed(
            titlePt = "Enums Numéricos e de String",
            titleEn = "String & Numeric Enums",
            instructionsPt = "Crie um enum Cargo associando ADMIN a 'ADMIN' e USER a 'USER'.",
            instructionsEn = "Create a Cargo enum mapping ADMIN to 'ADMIN' and USER to 'USER'.",
            starterCode = "___ Cargo {\n    ADMIN = \"ADMIN\",\n    USER = \"USER\"\n}\nconst acesso: Cargo = Cargo.ADMIN;",
            solutionCode = "enum Cargo {\n    ADMIN = \"ADMIN\",\n    USER = \"USER\"\n}\nconst acesso: Cargo = Cargo.ADMIN;",
            hintPt = "Use a palavra-chave 'enum'.",
            hintEn = "Use the 'enum' keyword.",
            keywords = listOf("enum Cargo", "Cargo.ADMIN"),
            explanationPt = "Enums agrupam conjuntos nomeados de constantes relacionadas com autocomplete nativo.",
            explanationEn = "Enums define named sets of constants with strong IDE autocomplete."
        ),
        TsSeed(
            titlePt = "Propriedades Somente Leitura (readonly)",
            titleEn = "Readonly Interface Properties",
            instructionsPt = "Impeça alterações no campo 'id' após a criação marcando-o como readonly.",
            instructionsEn = "Prevent mutation of 'id' after object creation by marking it readonly.",
            starterCode = "interface Conta {\n    ___ id: string;\n    saldo: number;\n}\nconst c: Conta = { id: \"acc_1\", saldo: 500 };",
            solutionCode = "interface Conta {\n    readonly id: string;\n    saldo: number;\n}\nconst c: Conta = { id: \"acc_1\", saldo: 500 };",
            hintPt = "Adicione 'readonly' antes de id.",
            hintEn = "Prepend 'readonly' before id.",
            keywords = listOf("readonly id", "interface Conta"),
            explanationPt = "readonly protege propriedades de mutações acidentais em tempo de compilação.",
            explanationEn = "readonly protects fields against unintended reassignment at compile time."
        )
    )

    private val intermediateSeeds = listOf(
        TsSeed(
            titlePt = "Generics em Funções Reutilizáveis",
            titleEn = "Generics in Reusable Functions",
            instructionsPt = "Crie a função genérica envolverEmArray<T> que recebe T e retorna T[].",
            instructionsEn = "Create generic function envolverEmArray<T> accepting T and returning T[].",
            starterCode = "function envolverEmArray<___>(item: T): T[] {\n    return [item];\n}\nconst nums = envolverEmArray(42);",
            solutionCode = "function envolverEmArray<T>(item: T): T[] {\n    return [item];\n}\nconst nums = envolverEmArray(42);",
            hintPt = "Insira o parâmetro genérico T entre < e >.",
            hintEn = "Place generic parameter T inside angle brackets <T>.",
            keywords = listOf("<T>", "item: T", "T[]"),
            explanationPt = "Generics preservam a informação exata de tipo em funções e classes reutilizáveis.",
            explanationEn = "Generics preserve exact type information across reusable utilities."
        ),
        TsSeed(
            titlePt = "Utility Type Partial<T> para Patches",
            titleEn = "Utility Type Partial<T> for Patches",
            instructionsPt = "Use Partial<Config> no argumento 'alteracoes' para atualizações parciais.",
            instructionsEn = "Use Partial<Config> on 'alteracoes' parameter for partial updates.",
            starterCode = "interface Config { tema: string; som: boolean; }\nfunction atualizarConfig(atual: Config, alteracoes: ___<Config>): Config {\n    return { ...atual, ...alteracoes };\n}",
            solutionCode = "interface Config { tema: string; som: boolean; }\nfunction atualizarConfig(atual: Config, alteracoes: Partial<Config>): Config {\n    return { ...atual, ...alteracoes };\n}",
            hintPt = "Use o utility type Partial<Config>.",
            hintEn = "Use the Partial<Config> utility type.",
            keywords = listOf("Partial", "Config", "alteracoes"),
            explanationPt = "Partial<T> torna todos os campos opcionais, ideal para payloads de atualização.",
            explanationEn = "Partial<T> sets every property of T to optional, ideal for update payloads."
        ),
        TsSeed(
            titlePt = "Utility Type Pick<T, K> para Subconjuntos",
            titleEn = "Utility Type Pick<T, K> for Subsets",
            instructionsPt = "Use Pick<Artigo, \"id\" | \"titulo\"> para selecionar apenas id e titulo.",
            instructionsEn = "Use Pick<Artigo, \"id\" | \"titulo\"> to pick only id and titulo.",
            starterCode = "interface Artigo { id: number; titulo: string; corpo: string; }\ntype ArtigoResumo = ___<Artigo, \"id\" | \"titulo\">;",
            solutionCode = "interface Artigo { id: number; titulo: string; corpo: string; }\ntype ArtigoResumo = Pick<Artigo, \"id\" | \"titulo\">;",
            hintPt = "Use Pick antes de <Artigo, ...>.",
            hintEn = "Use Pick before <Artigo, ...>.",
            keywords = listOf("Pick", "Artigo", "id", "titulo"),
            explanationPt = "Pick<T, K> deriva um novo tipo contendo apenas as chaves especificadas.",
            explanationEn = "Pick<T, K> constructs a subtype picking only specified keys K from T."
        ),
        TsSeed(
            titlePt = "Type Assertions com 'as'",
            titleEn = "Type Assertions with 'as'",
            instructionsPt = "Faça o cast do valor unknown para string usando a palavra-chave 'as'.",
            instructionsEn = "Assert the unknown value as string using the 'as' keyword.",
            starterCode = "const bruto: unknown = \"{\\\"ok\\\":true}\";\nconst texto = bruto ___ string;\nconsole.log(texto.length);",
            solutionCode = "const bruto: unknown = \"{\\\"ok\\\":true}\";\nconst texto = bruto as string;\nconsole.log(texto.length);",
            hintPt = "Insira 'as' antes de string.",
            hintEn = "Insert 'as' preceding string.",
            keywords = listOf("as", "string", "length"),
            explanationPt = "Type assertions informam ao compilador o tipo específico conhecido pelo desenvolvedor.",
            explanationEn = "Type assertions instruct the compiler about a narrower known type."
        ),
        TsSeed(
            titlePt = "Operador keyof para Acesso Tipado",
            titleEn = "keyof Operator for Typed Keys",
            instructionsPt = "Restrinja 'chave' às chaves válidas de Usuario usando keyof Usuario.",
            instructionsEn = "Constrain 'chave' to valid keys of Usuario using keyof Usuario.",
            starterCode = "interface Usuario { id: number; nome: string; }\nfunction lerCampo(u: Usuario, chave: ___ Usuario) {\n    return u[chave];\n}",
            solutionCode = "interface Usuario { id: number; nome: string; }\nfunction lerCampo(u: Usuario, chave: keyof Usuario) {\n    return u[chave];\n}",
            hintPt = "Insira 'keyof' antes de Usuario.",
            hintEn = "Use 'keyof' before Usuario.",
            keywords = listOf("keyof", "Usuario", "chave"),
            explanationPt = "keyof gera uma união literal com todas as chaves permitidas de um tipo.",
            explanationEn = "keyof yields a literal union of all valid property keys on a type."
        ),
        TsSeed(
            titlePt = "Utility Type Omit<T, K> para Exclusão",
            titleEn = "Utility Type Omit<T, K> for Exclusion",
            instructionsPt = "Crie UsuarioPublico removendo o campo sensível 'senha' com Omit.",
            instructionsEn = "Create UsuarioPublico omitting sensitive field 'senha' via Omit.",
            starterCode = "interface UsuarioFull { id: number; email: string; senha: string; }\ntype UsuarioPublico = ___<UsuarioFull, \"senha\">;",
            solutionCode = "interface UsuarioFull { id: number; email: string; senha: string; }\ntype UsuarioPublico = Omit<UsuarioFull, \"senha\">;",
            hintPt = "Use o utility type Omit.",
            hintEn = "Use the Omit utility type.",
            keywords = listOf("Omit", "UsuarioFull", "senha"),
            explanationPt = "Omit<T, K> cria um tipo excluindo campos sensíveis como senhas ou tokens.",
            explanationEn = "Omit<T, K> constructs a type without sensitive or unwanted properties."
        ),
        TsSeed(
            titlePt = "Intersection Types (&) para Composição",
            titleEn = "Intersection Types (&) Composition",
            instructionsPt = "Combine os tipos Identificavel e Auditavel usando o operador '&'.",
            instructionsEn = "Combine Identificavel and Auditavel types using the '&' intersection operator.",
            starterCode = "type Identificavel = { id: string };\ntype Auditavel = { criadoEm: Date };\ntype Entidade = Identificavel ___ Auditavel;",
            solutionCode = "type Identificavel = { id: string };\ntype Auditavel = { criadoEm: Date };\ntype Entidade = Identificavel & Auditavel;",
            hintPt = "Use o operador '&'.",
            hintEn = "Use the '&' operator.",
            keywords = listOf("Identificavel & Auditavel", "type Entidade"),
            explanationPt = "Intersection types combinam múltiplas definições exigindo todas as propriedades juntas.",
            explanationEn = "Intersection types merge multiple shapes requiring all combined members."
        ),
        TsSeed(
            titlePt = "Utility Type Required<T>",
            titleEn = "Utility Type Required<T>",
            instructionsPt = "Torne todas as propriedades opcionais de Rascunho obrigatórias com Required.",
            instructionsEn = "Convert all optional properties of Rascunho into required ones with Required.",
            starterCode = "interface Rascunho { titulo?: string; autor?: string; }\ntype Publicacao = ___<Rascunho>;",
            solutionCode = "interface Rascunho { titulo?: string; autor?: string; }\ntype Publicacao = Required<Rascunho>;",
            hintPt = "Use Required<Rascunho>.",
            hintEn = "Use Required<Rascunho>.",
            keywords = listOf("Required<Rascunho>"),
            explanationPt = "Required<T> é o inverso de Partial<T>, garantindo preenchimento completo.",
            explanationEn = "Required<T> reverses Partial<T>, enforcing all fields to be provided."
        ),
        TsSeed(
            titlePt = "Type Narrowing com Operador 'in'",
            titleEn = "Type Narrowing with 'in' Operator",
            instructionsPt = "Verifique se a propriedade 'token' existe em credencial usando 'token' in credencial.",
            instructionsEn = "Check whether 'token' exists on credencial using the 'in' operator.",
            starterCode = "type AuthJwt = { token: string };\ntype AuthKey = { apiKey: string };\nfunction autenticar(cred: AuthJwt | AuthKey) {\n    if (\"token\" ___ cred) console.log(cred.token);\n}",
            solutionCode = "type AuthJwt = { token: string };\ntype AuthKey = { apiKey: string };\nfunction autenticar(cred: AuthJwt | AuthKey) {\n    if (\"token\" in cred) console.log(cred.token);\n}",
            hintPt = "Use a palavra-chave 'in'.",
            hintEn = "Use the 'in' operator.",
            keywords = listOf("\"token\" in cred", "cred.token"),
            explanationPt = "O operador 'in' afunila unions verificando a presença da chave em tempo de execução.",
            explanationEn = "The 'in' check narrows discriminated or structural union branches cleanly."
        ),
        TsSeed(
            titlePt = "Assinaturas de Índice (Index Signatures)",
            titleEn = "Typed Index Signatures",
            instructionsPt = "Defina um dicionário de traduções aceitando qualquer chave string mapeada para string.",
            instructionsEn = "Define a translations dictionary accepting arbitrary string keys mapped to string.",
            starterCode = "interface Dicionario {\n    [chave: ___]: string;\n}\nconst pt: Dicionario = { hello: \"olá\", bye: \"tchau\" };",
            solutionCode = "interface Dicionario {\n    [chave: string]: string;\n}\nconst pt: Dicionario = { hello: \"olá\", bye: \"tchau\" };",
            hintPt = "O tipo da chave na assinatura de índice é string.",
            hintEn = "Index key type should be string.",
            keywords = listOf("[chave: string]: string"),
            explanationPt = "Index signatures modelam objetos dinâmicos usados como mapas de chaves arbitrárias.",
            explanationEn = "Index signatures type dynamic objects acting as string/number-indexed dictionaries."
        )
    )

    private val advancedSeeds = listOf(
        TsSeed(
            titlePt = "Type Guards com Type Predicates",
            titleEn = "Custom Type Guards with Type Predicates",
            instructionsPt = "Defina o retorno do type guard como 'animal is Peixe' para afunilar o tipo.",
            instructionsEn = "Declare the return type predicate as 'animal is Peixe' for type narrowing.",
            starterCode = "interface Peixe { nadar: () => void; }\ninterface Ave { voar: () => void; }\nfunction isPeixe(animal: Peixe | Ave): animal ___ Peixe {\n    return (animal as Peixe).nadar !== undefined;\n}",
            solutionCode = "interface Peixe { nadar: () => void; }\ninterface Ave { voar: () => void; }\nfunction isPeixe(animal: Peixe | Ave): animal is Peixe {\n    return (animal as Peixe).nadar !== undefined;\n}",
            hintPt = "Use a sintaxe 'animal is Peixe'.",
            hintEn = "Use predicate syntax 'animal is Peixe'.",
            keywords = listOf("animal is Peixe", "nadar"),
            explanationPt = "Type Predicates ativam o narrowing automático em blocos condicionais.",
            explanationEn = "Type predicates teach the compiler how custom boolean checks narrow union types."
        ),
        TsSeed(
            titlePt = "Generic Constraints com 'extends'",
            titleEn = "Generic Constraints with 'extends'",
            instructionsPt = "Restrinja o tipo T exigindo que possua 'id: number' com <T extends { id: number }>.",
            instructionsEn = "Constrain generic T to require 'id: number' via <T extends { id: number }>.",
            starterCode = "function imprimirId<T ___ { id: number }>(item: T): void {\n    console.log(item.id);\n}",
            solutionCode = "function imprimirId<T extends { id: number }>(item: T): void {\n    console.log(item.id);\n}",
            hintPt = "Use 'extends' dentro dos colchetes angulares.",
            hintEn = "Use 'extends' inside the generic type parameter.",
            keywords = listOf("T extends", "{ id: number }"),
            explanationPt = "Constraints garantem propriedades mínimas sem apagar o tipo específico original.",
            explanationEn = "Constraints require minimum member shapes without losing the caller's exact type."
        ),
        TsSeed(
            titlePt = "Conditional Types (T extends U ? X : Y)",
            titleEn = "Conditional Types (T extends U ? X : Y)",
            instructionsPt = "Crie o conditional type 'IsString<T>' resolvendo para true se T extends string, senão false.",
            instructionsEn = "Create conditional type 'IsString<T>' evaluating to true when T extends string else false.",
            starterCode = "type IsString<T> = T extends ___ ? true : false;\ntype A = IsString<\"TS\">;",
            solutionCode = "type IsString<T> = T extends string ? true : false;\ntype A = IsString<\"TS\">;",
            hintPt = "Compare T com 'string'.",
            hintEn = "Compare T against 'string'.",
            keywords = listOf("IsString", "extends string", "true : false"),
            explanationPt = "Conditional types aplicam lógica condicional diretamente em nível de tipos.",
            explanationEn = "Conditional types compute output types dynamically based on type relationships."
        ),
        TsSeed(
            titlePt = "Mapped Types e Modificador Readonly",
            titleEn = "Mapped Types & Readonly Modifier",
            instructionsPt = "Crie um mapped type que itera sobre K in keyof T adicionando o modificador readonly.",
            instructionsEn = "Build a mapped type iterating over K in keyof T adding the readonly modifier.",
            starterCode = "type MeuReadonly<T> = {\n    ___ [K in keyof T]: T[K];\n};",
            solutionCode = "type MeuReadonly<T> = {\n    readonly [K in keyof T]: T[K];\n};",
            hintPt = "Coloque 'readonly' antes de [K in keyof T].",
            hintEn = "Place 'readonly' before [K in keyof T].",
            keywords = listOf("readonly", "[K in keyof T]", "T[K]"),
            explanationPt = "Mapped types transformam sistematicamente todas as propriedades de uma interface.",
            explanationEn = "Mapped types systematically transform every property across an existing type."
        ),
        TsSeed(
            titlePt = "Utility Type Record<K, V> para Dicionários",
            titleEn = "Utility Type Record<K, V> for Dictionaries",
            instructionsPt = "Mapeie os cargos ('admin' | 'editor') para booleans usando Record<Cargo, boolean>.",
            instructionsEn = "Map roles ('admin' | 'editor') to booleans using Record<Cargo, boolean>.",
            starterCode = "type Cargo = \"admin\" | \"editor\";\nconst permissoes: ___<Cargo, boolean> = { admin: true, editor: false };",
            solutionCode = "type Cargo = \"admin\" | \"editor\";\nconst permissoes: Record<Cargo, boolean> = { admin: true, editor: false };",
            hintPt = "Use o utility type Record.",
            hintEn = "Use the Record utility type.",
            keywords = listOf("Record", "Cargo", "boolean"),
            explanationPt = "Record<K, V> garante cobertura exaustiva de todas as chaves da união K.",
            explanationEn = "Record<K, V> guarantees exhaustive coverage for all keys in union K."
        ),
        TsSeed(
            titlePt = "Inferência em Tipos Condicionais com 'infer'",
            titleEn = "Type Inference with 'infer'",
            instructionsPt = "Extraia o tipo de elemento interno de um Array usando 'infer Item' no conditional type.",
            instructionsEn = "Extract the inner element type of an Array using 'infer Item' in a conditional type.",
            starterCode = "type UnpackArray<T> = T extends Array<___ Item> ? Item : T;\ntype Str = UnpackArray<string[]>;",
            solutionCode = "type UnpackArray<T> = T extends Array<infer Item> ? Item : T;\ntype Str = UnpackArray<string[]>;",
            hintPt = "Use a palavra-chave 'infer' antes de Item.",
            hintEn = "Use the 'infer' keyword before Item.",
            keywords = listOf("extends Array<infer", "Item"),
            explanationPt = "A palavra-chave infer declara uma variável de tipo inferida dentro de conditional types.",
            explanationEn = "The infer keyword captures matched sub-types inside conditional type branches."
        ),
        TsSeed(
            titlePt = "Utility Type ReturnType<T> e Awaited<T>",
            titleEn = "ReturnType<T> & Awaited<T> Utilities",
            instructionsPt = "Extraia o tipo de retorno da função buscarUsuario usando ReturnType<typeof buscarUsuario>.",
            instructionsEn = "Extract the return type of buscarUsuario using ReturnType<typeof buscarUsuario>.",
            starterCode = "function buscarUsuario() { return { id: 1, nome: \"Eva\" }; }\ntype UsuarioRetornado = ___<typeof buscarUsuario>;",
            solutionCode = "function buscarUsuario() { return { id: 1, nome: \"Eva\" }; }\ntype UsuarioRetornado = ReturnType<typeof buscarUsuario>;",
            hintPt = "Use ReturnType.",
            hintEn = "Use ReturnType.",
            keywords = listOf("ReturnType", "typeof buscarUsuario"),
            explanationPt = "ReturnType evita duplicar manualmente tipos já inferidos pelo retorno de funções.",
            explanationEn = "ReturnType avoids manual duplication by extracting inferred function return types."
        ),
        TsSeed(
            titlePt = "Discriminated Unions Exaustivas com 'never'",
            titleEn = "Exhaustive Checks with 'never'",
            instructionsPt = "Garanta verificação exaustiva no ramo default atribuindo o caso impossível a 'never'.",
            instructionsEn = "Enforce exhaustive switch checking by assigning the unreachable branch to 'never'.",
            starterCode = "type Acao = { tipo: \"ADD\" } | { tipo: \"DEL\" };\nfunction reduzir(a: Acao) {\n    switch (a.tipo) {\n        case \"ADD\": return 1;\n        case \"DEL\": return -1;\n        default: const _: ___ = a; return _;\n    }\n}",
            solutionCode = "type Acao = { tipo: \"ADD\" } | { tipo: \"DEL\" };\nfunction reduzir(a: Acao) {\n    switch (a.tipo) {\n        case \"ADD\": return 1;\n        case \"DEL\": return -1;\n        default: const _: never = a; return _;\n    }\n}",
            hintPt = "O tipo impossível no TypeScript é 'never'.",
            hintEn = "The bottom/unreachable type in TypeScript is 'never'.",
            keywords = listOf(": never =", "switch"),
            explanationPt = "Atribuir a 'never' gera erro de compilação caso uma nova variante seja adicionada sem tratamento.",
            explanationEn = "Assigning to 'never' fails compilation whenever a new union case goes unhandled."
        ),
        TsSeed(
            titlePt = "Template Literal Types no Nível de Tipos",
            titleEn = "Template Literal Types",
            instructionsPt = "Construa o tipo de evento 'onClick' | 'onHover' usando template literal types.",
            instructionsEn = "Construct event names 'onClick' | 'onHover' via template literal types.",
            starterCode = "type Acao = \"Click\" | \"Hover\";\ntype HandlerName = `on\${___}`;",
            solutionCode = "type Acao = \"Click\" | \"Hover\";\ntype HandlerName = `on\${Acao}`;",
            hintPt = "Interpole o tipo Acao dentro da crase.",
            hintEn = "Interpolate type Acao inside backticks.",
            keywords = listOf("`on\${Acao}`", "HandlerName"),
            explanationPt = "Template literal types geram combinações de strings tipadas automaticamente.",
            explanationEn = "Template literal types generate Cartesian combinations of literal string types."
        ),
        TsSeed(
            titlePt = "Operador 'satisfies' para Validação sem Perder Inferência",
            titleEn = "'satisfies' Operator for Strict Validation",
            instructionsPt = "Valide que o objeto paleta conforma com Record<string, string | number[]> usando 'satisfies'.",
            instructionsEn = "Validate paleta conforms to Record<string, string | number[]> using 'satisfies'.",
            starterCode = "const paleta = {\n    primaria: \"#9D4EDD\",\n    rgb: [157, 78, 221]\n} ___ Record<string, string | number[]>;",
            solutionCode = "const paleta = {\n    primaria: \"#9D4EDD\",\n    rgb: [157, 78, 221]\n} satisfies Record<string, string | number[]>;",
            hintPt = "Use a palavra-chave 'satisfies'.",
            hintEn = "Use the 'satisfies' keyword.",
            keywords = listOf("satisfies Record", "primaria"),
            explanationPt = "'satisfies' valida um tipo mantendo a inferência literal específica de cada propriedade.",
            explanationEn = "'satisfies' validates structure while retaining narrow inferred property types."
        )
    )

    val list: List<PracticalActivity> by lazy {
        val beginner = (1..35).map { idx ->
            val seed = beginnerSeeds[(idx - 1) % beginnerSeeds.size]
            val suffix = if (idx <= beginnerSeeds.size) "" else " ($idx)"
            PracticalActivity(
                id = "ts_act_$idx",
                tech = Technology.TYPESCRIPT,
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
                id = "ts_act_$idx",
                tech = Technology.TYPESCRIPT,
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
                id = "ts_act_$idx",
                tech = Technology.TYPESCRIPT,
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
