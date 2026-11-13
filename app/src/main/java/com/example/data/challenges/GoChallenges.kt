package com.example.data.challenges

import com.example.model.ActivityDifficulty
import com.example.model.PracticalActivity
import com.example.model.Technology

object GoChallenges {

    private data class GoSeed(
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
        GoSeed(
            titlePt = "Declaração Curta com :=",
            titleEn = "Short Variable Declaration (:=",
            instructionsPt = "Use o operador de declaração curta ':=' para inicializar a variável linguagem com \"Go\".",
            instructionsEn = "Use short variable declaration ':=' to initialize linguagem with \"Go\".",
            starterCode = "package main\nimport \"fmt\"\nfunc main() {\n    linguagem ___ \"Go\"\n    fmt.Println(linguagem)\n}",
            solutionCode = "package main\nimport \"fmt\"\nfunc main() {\n    linguagem := \"Go\"\n    fmt.Println(linguagem)\n}",
            hintPt = "Use ':=' para declarar e inferir o tipo automaticamente.",
            hintEn = "Use ':=' to declare and infer variable type.",
            keywords = listOf(":=", "fmt.Println"),
            explanationPt = "Dentro de funções em Go, ':=' declara e inicializa variáveis com inferência estática.",
            explanationEn = "Inside Go functions, ':=' declares and initializes variables with static type inference."
        ),
        GoSeed(
            titlePt = "Múltiplos Retornos em Funções",
            titleEn = "Multiple Function Return Values",
            instructionsPt = "Retorne ambos os valores (quociente, resto) na função dividir.",
            instructionsEn = "Return both quotient and remainder values in the dividir function.",
            starterCode = "func dividir(a, b int) (int, int) {\n    ___ a / b, a % b\n}",
            solutionCode = "func dividir(a, b int) (int, int) {\n    return a / b, a % b\n}",
            hintPt = "Use a palavra-chave 'return' seguida de ambos os resultados separados por vírgula.",
            hintEn = "Use 'return' followed by comma-separated results.",
            keywords = listOf("return a / b, a % b", "(int, int)"),
            explanationPt = "Funções em Go retornam múltiplos valores nativamente, base para retorno de (resultado, erro).",
            explanationEn = "Go functions natively support multiple return values, underpinning idiomatic (value, error) returns."
        ),
        GoSeed(
            titlePt = "Slices e Função append()",
            titleEn = "Slices & append() Built-in",
            instructionsPt = "Adicione o elemento \"Cloud\" ao slice stacks usando append().",
            instructionsEn = "Append \"Cloud\" onto the stacks slice using built-in append().",
            starterCode = "stacks := []string{\"API\", \"Docker\"}\nstacks = ___(stacks, \"Cloud\")\nfmt.Println(len(stacks))",
            solutionCode = "stacks := []string{\"API\", \"Docker\"}\nstacks = append(stacks, \"Cloud\")\nfmt.Println(len(stacks))",
            hintPt = "Chame append(stacks, \"Cloud\").",
            hintEn = "Invoke append(stacks, \"Cloud\").",
            keywords = listOf("append(stacks", "Cloud"),
            explanationPt = "Slices são visões dinâmicas sobre arrays; append expande capacidade automaticamente.",
            explanationEn = "Slices offer dynamic views over arrays with append handling capacity growth."
        ),
        GoSeed(
            titlePt = "Laço for Único do Go",
            titleEn = "Go's Single Loop Construct (for)",
            instructionsPt = "Complete o laço for iterando de i = 0 até i < 5 incrementando i++.",
            instructionsEn = "Complete the for loop running from i = 0 while i < 5 with i++.",
            starterCode = "___ i := 0; i < 5; i++ {\n    fmt.Println(i)\n}",
            solutionCode = "for i := 0; i < 5; i++ {\n    fmt.Println(i)\n}",
            hintPt = "Go usa apenas 'for' para todos os loops.",
            hintEn = "Go uses 'for' as its sole loop keyword.",
            keywords = listOf("for i := 0", "i < 5", "i++"),
            explanationPt = "Go simplifica laços unificando for, while e iteradores sob uma única palavra-chave 'for'.",
            explanationEn = "Go unifies for, while, and range loops under the single 'for' keyword."
        ),
        GoSeed(
            titlePt = "Estruturas de Dados com struct",
            titleEn = "Data Modeling with struct",
            instructionsPt = "Defina o campo Nome como string na struct Desenvolvedor.",
            instructionsEn = "Define field Nome of type string inside the Desenvolvedor struct.",
            starterCode = "type Desenvolvedor ___ {\n    Nome string\n    Xp   int\n}",
            solutionCode = "type Desenvolvedor struct {\n    Nome string\n    Xp   int\n}",
            hintPt = "Use a palavra-chave 'struct'.",
            hintEn = "Use the 'struct' keyword.",
            keywords = listOf("type Desenvolvedor struct", "Nome string"),
            explanationPt = "Structs agrupam campos tipados e são a unidade fundamental de modelagem no Go.",
            explanationEn = "Structs group typed fields together forming Go's primary data modeling block."
        ),
        GoSeed(
            titlePt = "Mapas com make(map[K]V)",
            titleEn = "Maps with make(map[K]V)",
            instructionsPt = "Aloque um mapa de strings para inteiros usando make(map[string]int).",
            instructionsEn = "Allocate a map from string to int using make(map[string]int).",
            starterCode = "pontos := ___(map[string]int)\npontos[\"go\"] = 100",
            solutionCode = "pontos := make(map[string]int)\npontos[\"go\"] = 100",
            hintPt = "Use a função nativa make.",
            hintEn = "Use built-in function make.",
            keywords = listOf("make(map[string]int)"),
            explanationPt = "make inicializa mapas, slices e channels prontos para escrita evitando nil panic.",
            explanationEn = "make initializes maps, slices, and channels ready for safe mutation."
        ),
        GoSeed(
            titlePt = "Iteração com range em Slices",
            titleEn = "Iterating Slices with range",
            instructionsPt = "Percorra índice e valor de um slice usando a palavra-chave 'range'.",
            instructionsEn = "Iterate index and value over a slice using the 'range' keyword.",
            starterCode = "nomes := []string{\"Ana\", \"Bia\"}\nfor idx, nome := ___ nomes {\n    fmt.Println(idx, nome)\n}",
            solutionCode = "nomes := []string{\"Ana\", \"Bia\"}\nfor idx, nome := range nomes {\n    fmt.Println(idx, nome)\n}",
            hintPt = "Use 'range' antes de nomes.",
            hintEn = "Use 'range' preceding nomes.",
            keywords = listOf(":= range nomes"),
            explanationPt = "range retorna par índice/valor (ou chave/valor em maps) de forma limpa e segura.",
            explanationEn = "range yields index/value (or key/value) pairs cleanly across collections."
        ),
        GoSeed(
            titlePt = "Constantes Tipadas e Não Tipadas",
            titleEn = "Typed & Untyped Constants",
            instructionsPt = "Declare a constante PortaPadrao igual a 8080 usando 'const'.",
            instructionsEn = "Declare constant PortaPadrao equal to 8080 using 'const'.",
            starterCode = "___ PortaPadrao = 8080\nfmt.Println(PortaPadrao)",
            solutionCode = "const PortaPadrao = 8080\nfmt.Println(PortaPadrao)",
            hintPt = "Use 'const'.",
            hintEn = "Use 'const'.",
            keywords = listOf("const PortaPadrao = 8080"),
            explanationPt = "Constantes em Go são avaliadas em tempo de compilação com precisão arbitrária.",
            explanationEn = "Go constants are evaluated at compile time with arbitrary numeric precision."
        ),
        GoSeed(
            titlePt = "Switch sem Break Explícito",
            titleEn = "Clean Switch without Break",
            instructionsPt = "Complete o bloco switch tratando o caso \"GET\".",
            instructionsEn = "Complete the switch statement handling case \"GET\".",
            starterCode = "metodo := \"GET\"\n___ metodo {\ncase \"GET\":\n    fmt.Println(\"Consulta\")\ndefault:\n    fmt.Println(\"Outro\")\n}",
            solutionCode = "metodo := \"GET\"\nswitch metodo {\ncase \"GET\":\n    fmt.Println(\"Consulta\")\ndefault:\n    fmt.Println(\"Outro\")\n}",
            hintPt = "Use a palavra-chave 'switch'.",
            hintEn = "Use the 'switch' keyword.",
            keywords = listOf("switch metodo", "case \"GET\":"),
            explanationPt = "Em Go, cases do switch não propagam (no fallthrough) por padrão, eliminando bugs de break esquecido.",
            explanationEn = "Go switch cases do not fall through by default, preventing accidental break bugs."
        ),
        GoSeed(
            titlePt = "Formatação com fmt.Sprintf()",
            titleEn = "String Formatting with fmt.Sprintf()",
            instructionsPt = "Monte a string formatada sem imprimir usando fmt.Sprintf().",
            instructionsEn = "Build a formatted string without printing using fmt.Sprintf().",
            starterCode = "msg := fmt.___(\"Usuário %s - %d XP\", \"Lucas\", 300)\nfmt.Println(msg)",
            solutionCode = "msg := fmt.Sprintf(\"Usuário %s - %d XP\", \"Lucas\", 300)\nfmt.Println(msg)",
            hintPt = "Use Sprintf do pacote fmt.",
            hintEn = "Use Sprintf from fmt package.",
            keywords = listOf("fmt.Sprintf", "%s", "%d"),
            explanationPt = "fmt.Sprintf formata strings com especificadores de tipo (%s, %d, %v) retornando uma string.",
            explanationEn = "fmt.Sprintf builds formatted strings using type verbs (%s, %d, %v)."
        )
    )

    private val intermediateSeeds = listOf(
        GoSeed(
            titlePt = "Tratamento Explícito de Erros (if err != nil)",
            titleEn = "Explicit Error Handling (if err != nil)",
            instructionsPt = "Verifique se o erro retornado não é nulo (err != nil) e propague-o.",
            instructionsEn = "Check if returned error is non-nil (err != nil) and return early.",
            starterCode = "res, err := executarTarefa()\nif err ___ nil {\n    return err\n}\nfmt.Println(res)",
            solutionCode = "res, err := executarTarefa()\nif err != nil {\n    return err\n}\nfmt.Println(res)",
            hintPt = "Compare err com nil usando '!='.",
            hintEn = "Check err != nil.",
            keywords = listOf("if err != nil", "return err"),
            explanationPt = "Em Go erros são valores explícitos tratados via fluxos de controle claros sem exceções ocultas.",
            explanationEn = "In Go errors are explicit values handled directly via clear early-return control flow."
        ),
        GoSeed(
            titlePt = "Limpeza Garantida com defer",
            titleEn = "Resource Cleanup with defer",
            instructionsPt = "Agende o fechamento do arquivo ao final da função usando 'defer'.",
            instructionsEn = "Schedule file closure when the enclosing function returns using 'defer'.",
            starterCode = "f, err := os.Open(\"dados.txt\")\nif err != nil { return }\n___ f.Close()",
            solutionCode = "f, err := os.Open(\"dados.txt\")\nif err != nil { return }\ndefer f.Close()",
            hintPt = "Use a palavra-chave 'defer' antes de f.Close().",
            hintEn = "Prepend 'defer' before f.Close().",
            keywords = listOf("defer f.Close()"),
            explanationPt = "defer empilha chamadas para executarem imediatamente antes da função retornar, evitando vazamentos.",
            explanationEn = "defer pushes calls onto a stack executed right before function exit, preventing resource leaks."
        ),
        GoSeed(
            titlePt = "Métodos com Pointer Receivers (*T)",
            titleEn = "Methods with Pointer Receivers (*T)",
            instructionsPt = "Use pointer receiver (*Conta) para permitir modificar o campo Saldo permanentemente.",
            instructionsEn = "Use pointer receiver (*Conta) so Depositar mutates Saldo permanently.",
            starterCode = "type Conta struct { Saldo int }\nfunc (c ___Conta) Depositar(valor int) {\n    c.Saldo += valor\n}",
            solutionCode = "type Conta struct { Saldo int }\nfunc (c *Conta) Depositar(valor int) {\n    c.Saldo += valor\n}",
            hintPt = "Adicione '*' antes de Conta para receber o ponteiro.",
            hintEn = "Add '*' before Conta to declare a pointer receiver.",
            keywords = listOf("(c *Conta)", "c.Saldo +="),
            explanationPt = "Pointer receivers evitam cópia da struct e permitem mutar o estado original.",
            explanationEn = "Pointer receivers avoid struct copies and allow mutating the receiver state."
        ),
        GoSeed(
            titlePt = "Polimorfismo Implícito com Interfaces",
            titleEn = "Implicit Interface Satisfaction",
            instructionsPt = "Defina o método Serializar() string dentro da interface Serializador.",
            instructionsEn = "Declare method Serializar() string inside the Serializador interface.",
            starterCode = "type Serializador ___ {\n    Serializar() string\n}",
            solutionCode = "type Serializador interface {\n    Serializar() string\n}",
            hintPt = "Use a palavra-chave 'interface'.",
            hintEn = "Use the 'interface' keyword.",
            keywords = listOf("type Serializador interface", "Serializar() string"),
            explanationPt = "Em Go qualquer tipo que implemente os métodos da interface satisfaz seu contrato automaticamente.",
            explanationEn = "Go interfaces are satisfied implicitly without explicit 'implements' declarations."
        ),
        GoSeed(
            titlePt = "Serialização JSON com Struct Tags",
            titleEn = "JSON Marshaling with Struct Tags",
            instructionsPt = "Converta a struct Usuario para bytes JSON usando json.Marshal().",
            instructionsEn = "Serialize the Usuario struct to JSON bytes using json.Marshal().",
            starterCode = "type Usuario struct { Nome string `json:\"nome\"` }\nu := Usuario{Nome: \"Carlos\"}\ndados, _ := json.___(u)",
            solutionCode = "type Usuario struct { Nome string `json:\"nome\"` }\nu := Usuario{Nome: \"Carlos\"}\ndados, _ := json.Marshal(u)",
            hintPt = "Use a função Marshal do pacote encoding/json.",
            hintEn = "Use json.Marshal.",
            keywords = listOf("json.Marshal", "`json:\"nome\"`"),
            explanationPt = "Struct tags controlam nomes de campos na serialização JSON/YAML/XML.",
            explanationEn = "Struct tags customize field names during JSON/YAML serialization."
        ),
        GoSeed(
            titlePt = "Ponteiros e Desreferenciação (& e *)",
            titleEn = "Pointers & Dereferencing (& and *)",
            instructionsPt = "Passe o endereço de memória de contador usando o operador '&'.",
            instructionsEn = "Pass the memory address of contador using '&'.",
            starterCode = "func zerar(p *int) { *p = 0 }\ncontador := 42\nzerar(___contador)",
            solutionCode = "func zerar(p *int) { *p = 0 }\ncontador := 42\nzerar(&contador)",
            hintPt = "Use '&' para obter o ponteiro da variável.",
            hintEn = "Use '&' to take the address of contador.",
            keywords = listOf("zerar(&contador)", "*p = 0"),
            explanationPt = "'&' obtém o endereço de memória enquanto '*' lê ou grava no endereço apontado.",
            explanationEn = "'&' yields a memory pointer while '*' dereferences to read or mutate the target."
        ),
        GoSeed(
            titlePt = "Composição via Struct Embedding",
            titleEn = "Composition via Struct Embedding",
            instructionsPt = "Embuta a struct Base dentro de Produto promovendo seus campos diretamente.",
            instructionsEn = "Embed struct Base directly inside Produto to promote its fields.",
            starterCode = "type Base struct { ID int }\ntype Produto struct {\n    ___\n    Nome string\n}",
            solutionCode = "type Base struct { ID int }\ntype Produto struct {\n    Base\n    Nome string\n}",
            hintPt = "Escreva apenas o nome do tipo Base sem nome de campo.",
            hintEn = "Write the embedded type name Base directly.",
            keywords = listOf("Base", "Nome string", "type Produto struct"),
            explanationPt = "Go favorece composição sobre herança promovendo campos e métodos de structs embutidas.",
            explanationEn = "Go favors composition over inheritance by embedding structs to promote fields and methods."
        ),
        GoSeed(
            titlePt = "Strings Eficientes com strings.Builder",
            titleEn = "Efficient Concatenation with strings.Builder",
            instructionsPt = "Escreva pedaços de texto no buffer usando sb.WriteString() evitando alocações extras.",
            instructionsEn = "Write fragments into strings.Builder via WriteString() avoiding quadratic allocations.",
            starterCode = "var sb strings.Builder\nsb.___(\"Go \")\nsb.WriteString(\"Lang\")\nfmt.Println(sb.String())",
            solutionCode = "var sb strings.Builder\nsb.WriteString(\"Go \")\nsb.WriteString(\"Lang\")\nfmt.Println(sb.String())",
            hintPt = "Use o método WriteString.",
            hintEn = "Invoke WriteString.",
            keywords = listOf("strings.Builder", "WriteString", "sb.String()"),
            explanationPt = "strings.Builder reduz alocações de memória ao construir strings em loops.",
            explanationEn = "strings.Builder minimizes heap allocations when assembling strings incrementally."
        ),
        GoSeed(
            titlePt = "Erros Envelopados com errors.Is e %w",
            titleEn = "Error Wrapping with %w & errors.Is",
            instructionsPt = "Envolva o erro original usando fmt.Errorf com o verbo %w.",
            instructionsEn = "Wrap the underlying error using fmt.Errorf with the %w verb.",
            starterCode = "errWrapped := fmt.Errorf(\"falha no repo: ___\", errOriginal)\nfmt.Println(errors.Is(errWrapped, errOriginal))",
            solutionCode = "errWrapped := fmt.Errorf(\"falha no repo: %w\", errOriginal)\nfmt.Println(errors.Is(errWrapped, errOriginal))",
            hintPt = "O especificador de wrap em fmt.Errorf é %w.",
            hintEn = "The error-wrapping format verb is %w.",
            keywords = listOf("fmt.Errorf", "%w", "errors.Is"),
            explanationPt = "%w preserva a cadeia original de erros permitindo inspeção com errors.Is e errors.As.",
            explanationEn = "%w preserves error chains allowing callers to match causes with errors.Is and errors.As."
        ),
        GoSeed(
            titlePt = "Handler HTTP Padrão com net/http",
            titleEn = "Standard HTTP Handler with net/http",
            instructionsPt = "Escreva a resposta \"OK\" no http.ResponseWriter usando fmt.Fprintln(w, \"OK\").",
            instructionsEn = "Write \"OK\" to the http.ResponseWriter using fmt.Fprintln(w, \"OK\").",
            starterCode = "func healthHandler(w http.ResponseWriter, r *http.Request) {\n    fmt.___(w, \"OK\")\n}",
            solutionCode = "func healthHandler(w http.ResponseWriter, r *http.Request) {\n    fmt.Fprintln(w, \"OK\")\n}",
            hintPt = "Use fmt.Fprintln passando w como io.Writer.",
            hintEn = "Use fmt.Fprintln passing w as the io.Writer.",
            keywords = listOf("http.ResponseWriter", "fmt.Fprintln"),
            explanationPt = "A biblioteca padrão net/http entrega servidores web de produção com alta concorrência.",
            explanationEn = "Go's standard net/http library powers production-grade concurrent web services out of the box."
        )
    )

    private val advancedSeeds = listOf(
        GoSeed(
            titlePt = "Concorrência Leve com Goroutines",
            titleEn = "Lightweight Concurrency with Goroutines",
            instructionsPt = "Inicie a função processar() de forma assíncrona concorrente com a palavra-chave 'go'.",
            instructionsEn = "Launch processar() asynchronously as a concurrent goroutine using 'go'.",
            starterCode = "func main() {\n    ___ processar(\"tarefa_1\")\n    time.Sleep(100 * time.Millisecond)\n}",
            solutionCode = "func main() {\n    go processar(\"tarefa_1\")\n    time.Sleep(100 * time.Millisecond)\n}",
            hintPt = "Adicione 'go' antes da chamada processar().",
            hintEn = "Prefix processar() call with 'go'.",
            keywords = listOf("go processar"),
            explanationPt = "Goroutines são threads leves gerenciadas pelo runtime do Go consumindo poucos KB.",
            explanationEn = "Goroutines are lightweight user-space threads multiplexed onto OS threads by the Go runtime."
        ),
        GoSeed(
            titlePt = "Comunicação Segura com Channels",
            titleEn = "Safe Messaging with Channels",
            instructionsPt = "Envie o inteiro 42 para o channel usando o operador de seta '<-'.",
            instructionsEn = "Send integer 42 into the channel using the arrow operator '<-'.",
            starterCode = "ch := make(chan int)\ngo func() { ch ___ 42 }()\nvalor := <-ch",
            solutionCode = "ch := make(chan int)\ngo func() { ch <- 42 }()\nvalor := <-ch",
            hintPt = "Use '<-' apontando para o channel: ch <- 42.",
            hintEn = "Use arrow '<-' pointing into the channel: ch <- 42.",
            keywords = listOf("make(chan int)", "ch <- 42", "<-ch"),
            explanationPt = "Channels sincronizam e transferem dados entre goroutines seguindo CSP sem locks manuais.",
            explanationEn = "Channels share memory by communicating, avoiding manual mutex deadlocks."
        ),
        GoSeed(
            titlePt = "Sincronização com sync.WaitGroup",
            titleEn = "Goroutine Synchronization via sync.WaitGroup",
            instructionsPt = "Aguarde todas as goroutines finalizarem chamando wg.Wait() após wg.Done().",
            instructionsEn = "Wait for all launched goroutines to complete by calling wg.Wait().",
            starterCode = "var wg sync.WaitGroup\nwg.Add(1)\ngo func() {\n    defer wg.Done()\n    fmt.Println(\"Worker\")\n}()\nwg.___()",
            solutionCode = "var wg sync.WaitGroup\nwg.Add(1)\ngo func() {\n    defer wg.Done()\n    fmt.Println(\"Worker\")\n}()\nwg.Wait()",
            hintPt = "Chame wg.Wait() para bloquear até o contador zerar.",
            hintEn = "Call wg.Wait() to block until the counter reaches zero.",
            keywords = listOf("sync.WaitGroup", "wg.Done()", "wg.Wait()"),
            explanationPt = "sync.WaitGroup coordena o término de lotes de goroutines sem usar sleeps arbitrários.",
            explanationEn = "sync.WaitGroup reliably waits for concurrent goroutine batches without arbitrary sleeps."
        ),
        GoSeed(
            titlePt = "Multiplexação de Channels com select",
            titleEn = "Multiplexing Channels with select",
            instructionsPt = "Use 'select' para aguardar o primeiro resultado pronto entre canal1 e timeout.",
            instructionsEn = "Use 'select' to await whichever channel triggers first between ch1 and timeout.",
            starterCode = "___ {\ncase msg := <-ch1:\n    fmt.Println(msg)\ncase <-time.After(time.Second):\n    fmt.Println(\"Timeout\")\n}",
            solutionCode = "select {\ncase msg := <-ch1:\n    fmt.Println(msg)\ncase <-time.After(time.Second):\n    fmt.Println(\"Timeout\")\n}",
            hintPt = "Use a palavra-chave 'select'.",
            hintEn = "Use the 'select' statement.",
            keywords = listOf("select {", "case msg := <-ch1", "time.After"),
            explanationPt = "select bloqueia até que um dos channels esteja pronto, ideal para timeouts e cancelamentos.",
            explanationEn = "select multiplexes channel operations cleanly supporting timeouts and cancellation."
        ),
        GoSeed(
            titlePt = "Cancelamento e Timeouts com context.WithTimeout",
            titleEn = "Cancellation & Timeouts with context.WithTimeout",
            instructionsPt = "Crie um contexto com timeout de 2 segundos usando context.WithTimeout().",
            instructionsEn = "Create a context with a 2-second timeout using context.WithTimeout().",
            starterCode = "ctx, cancel := context.___(context.Background(), 2*time.Second)\ndefer cancel()",
            solutionCode = "ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)\ndefer cancel()",
            hintPt = "Use WithTimeout do pacote context.",
            hintEn = "Use WithTimeout from the context package.",
            keywords = listOf("context.WithTimeout", "defer cancel()"),
            explanationPt = "Context propaga prazos e sinais de cancelamento por toda a pilha de microserviços e banco.",
            explanationEn = "Context propagates deadlines and cancellation signals across API and DB call boundaries."
        ),
        GoSeed(
            titlePt = "Proteção de Estado Concorrente com sync.Mutex",
            titleEn = "Thread-Safe State with sync.Mutex",
            instructionsPt = "Adquira e libere o lock do mutex com mu.Lock() e defer mu.Unlock().",
            instructionsEn = "Acquire the mutex lock with mu.Lock() and defer mu.Unlock().",
            starterCode = "var mu sync.Mutex\nmu.___()\ndefer mu.Unlock()\ncontador++",
            solutionCode = "var mu sync.Mutex\nmu.Lock()\ndefer mu.Unlock()\ncontador++",
            hintPt = "Chame mu.Lock() para travar a seção crítica.",
            hintEn = "Call mu.Lock() before mutating shared state.",
            keywords = listOf("sync.Mutex", "mu.Lock()", "mu.Unlock()"),
            explanationPt = "Mutex garante exclusão mútua prevenindo race conditions ao compartilhar memória.",
            explanationEn = "Mutex enforces mutual exclusion eliminating data races on shared memory."
        ),
        GoSeed(
            titlePt = "Generics em Go 1.18+ com Constraints",
            titleEn = "Go Generics with Type Constraints",
            instructionsPt = "Defina o parâmetro de tipo genérico [T int | float64] na função Somar.",
            instructionsEn = "Define generic type parameter [T int | float64] on function Somar.",
            starterCode = "func Somar[T int ___ float64](a, b T) T {\n    return a + b\n}",
            solutionCode = "func Somar[T int | float64](a, b T) T {\n    return a + b\n}",
            hintPt = "Use o operador união '|' dentro da constraint.",
            hintEn = "Use the pipe '|' constraint union.",
            keywords = listOf("Somar[T int | float64]", "return a + b"),
            explanationPt = "Generics em Go permitem algoritmos polimórficos com performance nativa sem reflexão.",
            explanationEn = "Go generics enable type-safe polymorphic algorithms compiled without reflection overhead."
        ),
        GoSeed(
            titlePt = "Pool de Workers com Buffered Channels",
            titleEn = "Worker Pool with Buffered Channels",
            instructionsPt = "Crie um channel com buffer de 10 posições usando make(chan int, 10).",
            instructionsEn = "Allocate a buffered channel of capacity 10 using make(chan int, 10).",
            starterCode = "jobs := make(chan int, ___)\njobs <- 1\nclose(jobs)",
            solutionCode = "jobs := make(chan int, 10)\njobs <- 1\nclose(jobs)",
            hintPt = "Informe a capacidade 10 como segundo argumento de make.",
            hintEn = "Pass capacity 10 as the second argument to make.",
            keywords = listOf("make(chan int, 10)", "close(jobs)"),
            explanationPt = "Buffered channels desacoplam produtores e consumidores limitando concorrência com backpressure.",
            explanationEn = "Buffered channels decouple producers and workers while enforcing bounded concurrency."
        ),
        GoSeed(
            titlePt = "Testes Unitários Nativos com pacote testing",
            titleEn = "Native Unit Testing with testing.T",
            instructionsPt = "Reporte erro no teste caso a soma falhe usando t.Errorf().",
            instructionsEn = "Fail the unit test cleanly with diagnostic output using t.Errorf().",
            starterCode = "func TestSoma(t *testing.T) {\n    if 2+2 != 4 {\n        t.___(\"esperava 4\")\n    }\n}",
            solutionCode = "func TestSoma(t *testing.T) {\n    if 2+2 != 4 {\n        t.Errorf(\"esperava 4\")\n    }\n}",
            hintPt = "Use o método Errorf de *testing.T.",
            hintEn = "Use Errorf on *testing.T.",
            keywords = listOf("*testing.T", "t.Errorf"),
            explanationPt = "O ecossistema Go inclui framework de testes e benchmarks nativo via 'go test'.",
            explanationEn = "Go ships built-in unit testing and benchmarking natively via 'go test'."
        ),
        GoSeed(
            titlePt = "Leitura Segura em Mapas Concorrentes com RWMutex",
            titleEn = "Concurrent Reads with sync.RWMutex",
            instructionsPt = "Use RLock() e RUnlock() permitindo múltiplas leituras simultâneas no cache.",
            instructionsEn = "Use RLock() and RUnlock() enabling concurrent readers on the cache.",
            starterCode = "var rw sync.RWMutex\nrw.___()\ndefer rw.RUnlock()\nval := cache[\"chave\"]",
            solutionCode = "var rw sync.RWMutex\nrw.RLock()\ndefer rw.RUnlock()\nval := cache[\"chave\"]",
            hintPt = "Chame rw.RLock() para trava compartilhada de leitura.",
            hintEn = "Invoke rw.RLock() for shared read locking.",
            keywords = listOf("sync.RWMutex", "rw.RLock()", "rw.RUnlock()"),
            explanationPt = "RWMutex maximiza throughput em caches onde leituras são muito mais frequentes que escritas.",
            explanationEn = "RWMutex boosts read-heavy cache throughput by permitting parallel readers."
        )
    )

    val list: List<PracticalActivity> by lazy {
        val beginner = (1..35).map { idx ->
            val seed = beginnerSeeds[(idx - 1) % beginnerSeeds.size]
            val suffix = if (idx <= beginnerSeeds.size) "" else " ($idx)"
            PracticalActivity(
                id = "go_act_$idx",
                tech = Technology.GO,
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
                id = "go_act_$idx",
                tech = Technology.GO,
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
                id = "go_act_$idx",
                tech = Technology.GO,
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
