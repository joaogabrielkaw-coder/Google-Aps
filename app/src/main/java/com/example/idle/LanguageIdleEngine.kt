package com.example.idle

import com.example.data.repository.SqlLabRepository
import com.example.model.IdleExecutionResult
import com.example.model.IdleExecutionStatus
import com.example.model.Language
import com.example.model.SqlQueryResult
import com.example.model.Technology
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * Motor local sandboxed para execução das IDLEs das 12 linguagens do app
 * (sem Dynamic Code Loading externo, 100% compatível com Google Play).
 */
object LanguageIdleEngine {
    private const val MAX_EXECUTION_STEPS = 5000

    suspend fun execute(
        tech: Technology,
        rawCode: String,
        language: Language,
        sqlLabRepository: SqlLabRepository? = null
    ): IdleExecutionResult {
        val start = System.currentTimeMillis()
        val isPt = language == Language.PT
        if (rawCode.isBlank()) {
            return IdleExecutionResult(
                tech = tech,
                status = IdleExecutionStatus.ERROR,
                errorMessage = if (isPt) "Digite algum código antes de executar." else "Write some code before running.",
                executionTimeMs = 1L
            )
        }

        return when (tech) {
            Technology.HTML -> evaluateHtml(rawCode, start, isPt)
            Technology.CSS -> evaluateCss(rawCode, start, isPt)
            Technology.SQL,
            Technology.MYSQL,
            Technology.POSTGRESQL -> evaluateSqlFamily(tech, rawCode, start, isPt, sqlLabRepository)
            Technology.MONGODB -> evaluateMongoShell(rawCode, start, isPt)
            Technology.PYTHON -> evaluatePython(rawCode, start, isPt)
            Technology.JAVASCRIPT,
            Technology.TYPESCRIPT,
            Technology.KOTLIN,
            Technology.JAVA,
            Technology.GO -> evaluateBraceLanguage(tech, rawCode, start, isPt)
        }
    }

    private fun evaluateHtml(code: String, start: Long, isPt: Boolean): IdleExecutionResult {
        val tagRegex = Regex("""<(/?)([a-zA-Z0-9]+)([^>]*)>""")
        val voidTags = setOf("br", "hr", "img", "input", "meta", "link", "area", "base", "col", "embed", "source", "track", "wbr")
        val stack = mutableListOf<String>()
        var tagCount = 0
        for (m in tagRegex.findAll(code)) {
            val isClosing = m.groupValues[1] == "/"
            val name = m.groupValues[2].lowercase()
            val selfClosing = m.groupValues[3].trimEnd().endsWith("/") || name in voidTags
            tagCount++
            if (isClosing) {
                if (stack.isNotEmpty() && stack.last() == name) {
                    stack.removeAt(stack.lastIndex)
                }
            } else if (!selfClosing) {
                stack.add(name)
            }
        }

        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
        val previewDoc = if (code.contains("<html", ignoreCase = true)) {
            code
        } else {
            """
            <!DOCTYPE html>
            <html>
            <head><meta name="viewport" content="width=device-width, initial-scale=1.0" /></head>
            <body style="margin:0;padding:12px;background:#120d1f;color:#f8fafc;font-family:sans-serif;">
            $code
            </body>
            </html>
            """.trimIndent()
        }

        val infoLine = if (isPt) {
            "HTML5 renderizado no Preview • $tagCount tags detectadas" +
                if (stack.isNotEmpty()) " (aviso: tags abertas ${stack.joinToString()})" else " • estrutura válida"
        } else {
            "HTML5 rendered in Preview • $tagCount tags detected" +
                if (stack.isNotEmpty()) " (warning: unclosed ${stack.joinToString()})" else " • valid structure"
        }

        return IdleExecutionResult(
            tech = Technology.HTML,
            status = IdleExecutionStatus.SUCCESS,
            outputLines = listOf(infoLine),
            htmlPreviewDocument = previewDoc,
            executionTimeMs = elapsed
        )
    }

    private fun evaluateCss(code: String, start: Long, isPt: Boolean): IdleExecutionResult {
        val openBraces = code.count { it == '{' }
        val closeBraces = code.count { it == '}' }
        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
        if (openBraces != closeBraces) {
            return IdleExecutionResult(
                tech = Technology.CSS,
                status = IdleExecutionStatus.ERROR,
                errorMessage = if (isPt) {
                    "Erro de sintaxe CSS: chaves desbalanceadas ($openBraces '{' vs $closeBraces '}')."
                } else {
                    "CSS syntax error: unbalanced braces ($openBraces '{' vs $closeBraces '}')."
                },
                executionTimeMs = elapsed
            )
        }

        val previewHtml = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta name="viewport" content="width=device-width, initial-scale=1.0" />
              <style>$code</style>
            </head>
            <body>
              <div class="preview-card">
                <span class="badge">CSS3 LIVE</span>
                <h2 style="margin:0;">DevRoutine CSS Studio</h2>
                <p style="margin:0;opacity:0.9;">Edite seletores como body, .preview-card, .badge, .grid-demo e .box.</p>
              </div>
              <div class="grid-demo" style="margin-top:12px;">
                <div class="box">Flexbox</div>
                <div class="box">CSS Grid</div>
              </div>
            </body>
            </html>
        """.trimIndent()

        return IdleExecutionResult(
            tech = Technology.CSS,
            status = IdleExecutionStatus.SUCCESS,
            outputLines = listOf(
                if (isPt) "CSS3 compilado: $openBraces regra(s) aplicada(s) ao Preview."
                else "CSS3 compiled: $openBraces rule block(s) applied to Preview."
            ),
            htmlPreviewDocument = previewHtml,
            executionTimeMs = elapsed
        )
    }

    private suspend fun evaluateSqlFamily(
        tech: Technology,
        rawCode: String,
        start: Long,
        isPt: Boolean,
        sqlLabRepository: SqlLabRepository?
    ): IdleExecutionResult {
        val normalized = rawCode.trim().removeSuffix(";").trim()
        val upper = normalized.uppercase()

        val translatedSql = when {
            (tech == Technology.MYSQL && (upper == "SHOW TABLES" || upper.startsWith("SHOW TABLES"))) ->
                "SELECT name AS table_name, type FROM sqlite_master WHERE type = 'table' ORDER BY name"
            (tech == Technology.POSTGRESQL && (upper == "\\DT" || upper == "SHOW TABLES")) ->
                "SELECT name AS table_name, type FROM sqlite_master WHERE type = 'table' ORDER BY name"
            (tech == Technology.MYSQL && upper.startsWith("DESCRIBE ")) -> {
                val tbl = normalized.substringAfter(" ").trim().replace("`", "").replace("'", "")
                "PRAGMA table_info($tbl)"
            }
            (tech == Technology.MYSQL && upper.startsWith("DESC ")) -> {
                val tbl = normalized.substringAfter(" ").trim().replace("`", "").replace("'", "")
                "PRAGMA table_info($tbl)"
            }
            else -> normalized
        }

        if (sqlLabRepository != null) {
            val result: SqlQueryResult = sqlLabRepository.executeReadOnlySql(translatedSql)
            if (result.errorMessage != null) {
                return IdleExecutionResult(
                    tech = tech,
                    status = IdleExecutionStatus.ERROR,
                    errorMessage = result.errorMessage,
                    executionTimeMs = result.executionTimeMs
                )
            }
            return IdleExecutionResult(
                tech = tech,
                status = IdleExecutionStatus.SUCCESS,
                outputLines = listOf(
                    if (isPt) "${result.rows.size} linha(s) retornada(s) em ${result.executionTimeMs} ms"
                    else "${result.rows.size} row(s) returned in ${result.executionTimeMs} ms"
                ),
                tableColumns = result.columns,
                tableRows = result.rows,
                executionTimeMs = result.executionTimeMs
            )
        }

        // Pure-JVM validation fallback when invoked in unit tests without Room DB
        val err = SqlLabRepository.validateReadOnlySql(translatedSql)
        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
        return if (err != null) {
            IdleExecutionResult(
                tech = tech,
                status = IdleExecutionStatus.ERROR,
                errorMessage = err,
                executionTimeMs = elapsed
            )
        } else {
            IdleExecutionResult(
                tech = tech,
                status = IdleExecutionStatus.SUCCESS,
                outputLines = listOf("SQL validado: $translatedSql"),
                executionTimeMs = elapsed
            )
        }
    }

    private fun evaluateMongoShell(rawCode: String, start: Long, isPt: Boolean): IdleExecutionResult {
        val collections = mutableMapOf(
            "study_notes" to mutableListOf(
                linkedMapOf<String, Any?>("_id" to "n1", "techId" to "sql", "title" to "Índices B-Tree", "score" to 92),
                linkedMapOf<String, Any?>("_id" to "n2", "techId" to "mongodb", "title" to "Aggregation \$match", "score" to 88),
                linkedMapOf<String, Any?>("_id" to "n3", "techId" to "kotlin", "title" to "Coroutines Flow", "score" to 96)
            ),
            "game_scores" to mutableListOf(
                linkedMapOf<String, Any?>("_id" to "g1", "playerName" to "Ana", "score" to 1450, "crystals" to 24),
                linkedMapOf<String, Any?>("_id" to "g2", "playerName" to "João", "score" to 1220, "crystals" to 19)
            )
        )

        val lines = rawCode.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("//") }
        val output = mutableListOf<String>()

        for (cmd in lines) {
            val clean = cmd.removeSuffix(";").trim()
            when {
                clean.equals("show collections", ignoreCase = true) -> {
                    collections.keys.forEach { output.add(it) }
                }
                clean.startsWith("db.") -> {
                    val afterDb = clean.removePrefix("db.")
                    val colName = afterDb.substringBefore(".").trim()
                    val docs = collections.getOrPut(colName) { mutableListOf() }
                    val op = afterDb.substringAfter(".", "")
                    when {
                        op.startsWith("insertOne(") -> {
                            val body = op.substringAfter("insertOne(").substringBeforeLast(")").trim()
                            val parsed = parseFlatJsonObject(body)
                            val newId = "doc_${docs.size + 1}"
                            val doc = linkedMapOf<String, Any?>("_id" to newId)
                            doc.putAll(parsed)
                            docs.add(doc)
                            output.add("{ \"acknowledged\": true, \"insertedId\": \"$newId\" }")
                        }
                        op.startsWith("countDocuments(") -> {
                            val filterStr = op.substringAfter("countDocuments(").substringBeforeLast(")").trim()
                            val filter = parseFlatJsonObject(filterStr)
                            val count = filterMongoDocs(docs, filter).size
                            output.add(count.toString())
                        }
                        op.startsWith("find(") -> {
                            val filterStr = op.substringAfter("find(").substringBefore(")").trim()
                            val filter = parseFlatJsonObject(filterStr)
                            var matched = filterMongoDocs(docs, filter)
                            val limitMatch = Regex("""\.limit\((\d+)\)""").find(op)
                            if (limitMatch != null) {
                                val lim = limitMatch.groupValues[1].toIntOrNull() ?: matched.size
                                matched = matched.take(lim)
                            }
                            if (matched.isEmpty()) {
                                output.add("[]")
                            } else {
                                matched.forEach { d ->
                                    val props = d.entries.joinToString(", ") { (k, v) ->
                                        val rendered = if (v is String) "\"$v\"" else v.toString()
                                        "\"$k\": $rendered"
                                    }
                                    output.add("{ $props }")
                                }
                            }
                        }
                        else -> {
                            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                            return IdleExecutionResult(
                                tech = Technology.MONGODB,
                                status = IdleExecutionStatus.ERROR,
                                errorMessage = if (isPt) {
                                    "Comando MongoDB não suportado: '$clean'. Use show collections, db.<colecao>.find({...}), insertOne({...}) ou countDocuments()."
                                } else {
                                    "Unsupported MongoDB command: '$clean'. Try show collections, db.<col>.find({...}), insertOne({...}), or countDocuments()."
                                },
                                executionTimeMs = elapsed
                            )
                        }
                    }
                }
                else -> {
                    val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                    return IdleExecutionResult(
                        tech = Technology.MONGODB,
                        status = IdleExecutionStatus.ERROR,
                        errorMessage = if (isPt) "Comando inválido no MongoDB Shell: '$clean'." else "Invalid MongoDB Shell command: '$clean'.",
                        executionTimeMs = elapsed
                    )
                }
            }
        }

        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
        return IdleExecutionResult(
            tech = Technology.MONGODB,
            status = IdleExecutionStatus.SUCCESS,
            outputLines = output,
            executionTimeMs = elapsed
        )
    }

    private fun parseFlatJsonObject(raw: String): Map<String, Any?> {
        val trimmed = raw.trim().removePrefix("{").removeSuffix("}").trim()
        if (trimmed.isBlank()) return emptyMap()
        val result = linkedMapOf<String, Any?>()
        for (part in splitTopLevelArgs(trimmed)) {
            val idx = part.indexOf(':')
            if (idx <= 0) continue
            val key = part.substring(0, idx).trim().removeSurrounding("\"").removeSurrounding("'")
            val rawVal = part.substring(idx + 1).trim()
            val value: Any? = when {
                (rawVal.startsWith("\"") && rawVal.endsWith("\"")) ||
                    (rawVal.startsWith("'") && rawVal.endsWith("'")) ->
                    rawVal.substring(1, rawVal.length - 1)
                rawVal.equals("true", ignoreCase = true) -> true
                rawVal.equals("false", ignoreCase = true) -> false
                rawVal.toLongOrNull() != null -> rawVal.toLong()
                rawVal.toDoubleOrNull() != null -> rawVal.toDouble()
                else -> rawVal
            }
            if (key.isNotBlank()) result[key] = value
        }
        return result
    }

    private fun filterMongoDocs(
        docs: List<Map<String, Any?>>,
        filter: Map<String, Any?>
    ): List<Map<String, Any?>> {
        if (filter.isEmpty()) return docs
        return docs.filter { doc ->
            filter.all { (k, v) -> doc[k]?.toString() == v?.toString() }
        }
    }

    // ==================== PYTHON INTERPRETER ====================

    private fun evaluatePython(rawCode: String, start: Long, isPt: Boolean): IdleExecutionResult {
        return try {
            val rawLines = rawCode.lines()
            val parsedLines = mutableListOf<PyLine>()
            rawLines.forEachIndexed { idx, line ->
                val strippedComment = stripLineComment(line, "#")
                if (strippedComment.isNotBlank()) {
                    val indent = strippedComment.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
                    parsedLines.add(PyLine(idx + 1, indent, strippedComment.trim()))
                }
            }
            val (topNodes, _) = parsePythonBlock(parsedLines, 0, parsedLines.firstOrNull()?.indent ?: 0)
            val ctx = ScriptExecContext(Technology.PYTHON, isPt)
            val funcs = mutableMapOf<String, ScriptFunc>()
            val mainNodes = mutableListOf<StmtNode>()
            for (node in topNodes) {
                if (node is StmtNode.FuncDef) {
                    funcs[node.name] = ScriptFunc(node.params, node.body)
                } else {
                    mainNodes.add(node)
                }
            }
            ctx.functions.putAll(funcs)
            executeBlock(mainNodes, ctx.globalScope, ctx)

            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            IdleExecutionResult(
                tech = Technology.PYTHON,
                status = IdleExecutionStatus.SUCCESS,
                outputLines = ctx.outputLines,
                executionTimeMs = elapsed
            )
        } catch (e: IdleScriptException) {
            IdleExecutionResult(
                tech = Technology.PYTHON,
                status = IdleExecutionStatus.ERROR,
                errorMessage = e.message,
                executionTimeMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            )
        } catch (e: Exception) {
            IdleExecutionResult(
                tech = Technology.PYTHON,
                status = IdleExecutionStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Python error",
                executionTimeMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            )
        }
    }

    private data class PyLine(val lineNumber: Int, val indent: Int, val text: String)

    private fun parsePythonBlock(
        lines: List<PyLine>,
        startIndex: Int,
        expectedIndent: Int
    ): Pair<List<StmtNode>, Int> {
        val nodes = mutableListOf<StmtNode>()
        var i = startIndex
        while (i < lines.size) {
            val line = lines[i]
            if (line.indent < expectedIndent) break
            val text = line.text
            when {
                text.startsWith("def ") && text.endsWith(":") -> {
                    val sig = text.removePrefix("def ").removeSuffix(":").trim()
                    val name = sig.substringBefore("(").trim()
                    val paramsRaw = sig.substringAfter("(", "").substringBeforeLast(")", "")
                    val params = if (paramsRaw.isBlank()) emptyList()
                    else paramsRaw.split(",").map { it.trim().substringBefore("=").trim() }.filter { it.isNotBlank() }
                    val nextIndent = lines.getOrNull(i + 1)?.indent ?: (expectedIndent + 4)
                    val (body, nextIdx) = if (nextIndent > line.indent) {
                        parsePythonBlock(lines, i + 1, nextIndent)
                    } else {
                        emptyList<StmtNode>() to (i + 1)
                    }
                    nodes.add(StmtNode.FuncDef(name, params, body))
                    i = nextIdx
                }
                text.startsWith("if ") && text.endsWith(":") -> {
                    val branches = mutableListOf<Pair<String?, List<StmtNode>>>()
                    val cond = text.removePrefix("if ").removeSuffix(":").trim()
                    val firstBodyIndent = lines.getOrNull(i + 1)?.indent ?: (expectedIndent + 4)
                    val (firstBody, afterFirst) = if (firstBodyIndent > line.indent) {
                        parsePythonBlock(lines, i + 1, firstBodyIndent)
                    } else {
                        emptyList<StmtNode>() to (i + 1)
                    }
                    branches.add(cond to firstBody)
                    i = afterFirst
                    while (i < lines.size && lines[i].indent == line.indent) {
                        val cur = lines[i].text
                        if (cur.startsWith("elif ") && cur.endsWith(":")) {
                            val eCond = cur.removePrefix("elif ").removeSuffix(":").trim()
                            val bIndent = lines.getOrNull(i + 1)?.indent ?: (expectedIndent + 4)
                            val (eBody, afterElif) = if (bIndent > line.indent) {
                                parsePythonBlock(lines, i + 1, bIndent)
                            } else emptyList<StmtNode>() to (i + 1)
                            branches.add(eCond to eBody)
                            i = afterElif
                        } else if (cur == "else:") {
                            val bIndent = lines.getOrNull(i + 1)?.indent ?: (expectedIndent + 4)
                            val (eBody, afterElse) = if (bIndent > line.indent) {
                                parsePythonBlock(lines, i + 1, bIndent)
                            } else emptyList<StmtNode>() to (i + 1)
                            branches.add(null to eBody)
                            i = afterElse
                            break
                        } else {
                            break
                        }
                    }
                    nodes.add(StmtNode.IfChain(branches))
                }
                text.startsWith("for ") && text.endsWith(":") -> {
                    val header = text.removePrefix("for ").removeSuffix(":").trim()
                    val varName = header.substringBefore(" in ").trim()
                    val iterableExpr = header.substringAfter(" in ", "").trim()
                    val bIndent = lines.getOrNull(i + 1)?.indent ?: (expectedIndent + 4)
                    val (body, nextIdx) = if (bIndent > line.indent) {
                        parsePythonBlock(lines, i + 1, bIndent)
                    } else emptyList<StmtNode>() to (i + 1)
                    nodes.add(StmtNode.ForEach(varName, iterableExpr, body))
                    i = nextIdx
                }
                text.startsWith("while ") && text.endsWith(":") -> {
                    val cond = text.removePrefix("while ").removeSuffix(":").trim()
                    val bIndent = lines.getOrNull(i + 1)?.indent ?: (expectedIndent + 4)
                    val (body, nextIdx) = if (bIndent > line.indent) {
                        parsePythonBlock(lines, i + 1, bIndent)
                    } else emptyList<StmtNode>() to (i + 1)
                    nodes.add(StmtNode.WhileLoop(cond, body))
                    i = nextIdx
                }
                text == "pass" -> i++
                else -> {
                    nodes.add(parseSimpleStatement(text, Technology.PYTHON))
                    i++
                }
            }
        }
        return nodes to i
    }

    // ==================== BRACE LANGUAGES (JS, TS, KOTLIN, JAVA, GO) ====================

    private fun evaluateBraceLanguage(
        tech: Technology,
        rawCode: String,
        start: Long,
        isPt: Boolean
    ): IdleExecutionResult {
        return try {
            val cleaned = preprocessBraceCode(tech, rawCode)
            val tokens = tokenizeBraceStatements(cleaned)
            val (nodes, _) = parseBraceBlock(tokens, 0, tech)

            val ctx = ScriptExecContext(tech, isPt)
            val mainNodes = mutableListOf<StmtNode>()
            for (node in nodes) {
                if (node is StmtNode.FuncDef) {
                    ctx.functions[node.name] = ScriptFunc(node.params, node.body)
                } else {
                    mainNodes.add(node)
                }
            }

            if (mainNodes.isNotEmpty()) {
                executeBlock(mainNodes, ctx.globalScope, ctx)
            }
            ctx.functions["main"]?.let { mainFunc ->
                executeBlock(mainFunc.body, Scope(ctx.globalScope), ctx)
            }

            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            IdleExecutionResult(
                tech = tech,
                status = IdleExecutionStatus.SUCCESS,
                outputLines = ctx.outputLines,
                executionTimeMs = elapsed
            )
        } catch (e: IdleScriptException) {
            IdleExecutionResult(
                tech = tech,
                status = IdleExecutionStatus.ERROR,
                errorMessage = e.message,
                executionTimeMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            )
        } catch (e: Exception) {
            IdleExecutionResult(
                tech = tech,
                status = IdleExecutionStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Execution error",
                executionTimeMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            )
        }
    }

    private fun preprocessBraceCode(tech: Technology, rawCode: String): String {
        val noComments = rawCode.lines().joinToString("\n") { line ->
            stripLineComment(line, "//")
        }
        val lines = noComments.lines().filter { line ->
            val t = line.trim()
            when {
                tech == Technology.GO && (t.startsWith("package ") || t.startsWith("import ")) -> false
                tech == Technology.JAVA && (t.startsWith("package ") || t.startsWith("import ")) -> false
                tech == Technology.TYPESCRIPT && (t.startsWith("type ") && !t.contains("{")) -> false
                else -> true
            }
        }
        var joined = lines.joinToString("\n")
        if (tech == Technology.TYPESCRIPT) {
            // Strip TypeScript interface blocks
            joined = joined.replace(Regex("""interface\s+\w+\s*\{[^}]*\}"""), "")
        }
        if (tech == Technology.JAVA) {
            // Unwrap outer public class X { ... }
            val classMatch = Regex("""(?:public\s+)?class\s+\w+\s*\{""").find(joined)
            if (classMatch != null) {
                val openIdx = classMatch.range.last
                val closeIdx = findMatchingBrace(joined, openIdx)
                if (closeIdx > openIdx) {
                    joined = joined.substring(openIdx + 1, closeIdx)
                }
            }
        }
        return joined
    }

    private sealed interface BraceToken {
        data class Header(val text: String) : BraceToken
        data class Statement(val text: String) : BraceToken
        data object OpenBrace : BraceToken
        data object CloseBrace : BraceToken
    }

    private fun tokenizeBraceStatements(code: String): List<BraceToken> {
        val tokens = mutableListOf<BraceToken>()
        val buf = StringBuilder()
        var inString: Char? = null
        var parenDepth = 0
        var i = 0
        while (i < code.length) {
            val ch = code[i]
            if (inString != null) {
                buf.append(ch)
                if (ch == inString && (i == 0 || code[i - 1] != '\\')) {
                    inString = null
                }
                i++
                continue
            }
            when (ch) {
                '"', '\'', '`' -> {
                    inString = ch
                    buf.append(ch)
                }
                '(' -> {
                    parenDepth++
                    buf.append(ch)
                }
                ')' -> {
                    if (parenDepth > 0) parenDepth--
                    buf.append(ch)
                }
                '{' -> {
                    val hdr = buf.toString().trim()
                    buf.clear()
                    if (hdr.isNotEmpty()) tokens.add(BraceToken.Header(hdr))
                    tokens.add(BraceToken.OpenBrace)
                }
                '}' -> {
                    val stmt = buf.toString().trim()
                    buf.clear()
                    if (stmt.isNotEmpty()) tokens.add(BraceToken.Statement(stmt))
                    tokens.add(BraceToken.CloseBrace)
                }
                ';', '\n' -> {
                    val trimmedBuf = buf.toString().trimStart()
                    val isGoForHeader = ch == ';' && parenDepth == 0 &&
                        (trimmedBuf.startsWith("for ") || trimmedBuf.startsWith("for\t"))
                    if (parenDepth == 0 && !isGoForHeader) {
                        val stmt = buf.toString().trim()
                        buf.clear()
                        if (stmt.isNotEmpty()) tokens.add(BraceToken.Statement(stmt))
                    } else if (ch == ';') {
                        buf.append(';')
                    } else {
                        buf.append(' ')
                    }
                }
                else -> buf.append(ch)
            }
            i++
        }
        val tail = buf.toString().trim()
        if (tail.isNotEmpty()) tokens.add(BraceToken.Statement(tail))
        return tokens
    }

    private fun parseBraceBlock(
        tokens: List<BraceToken>,
        startIndex: Int,
        tech: Technology
    ): Pair<List<StmtNode>, Int> {
        val nodes = mutableListOf<StmtNode>()
        var i = startIndex
        while (i < tokens.size) {
            when (val tok = tokens[i]) {
                BraceToken.CloseBrace -> return nodes to (i + 1)
                BraceToken.OpenBrace -> {
                    val (inner, nextIdx) = parseBraceBlock(tokens, i + 1, tech)
                    nodes.addAll(inner)
                    i = nextIdx
                }
                is BraceToken.Statement -> {
                    nodes.add(parseSimpleStatement(tok.text, tech))
                    i++
                }
                is BraceToken.Header -> {
                    val hdr = tok.text.trim()
                    if (tokens.getOrNull(i + 1) == BraceToken.OpenBrace) {
                        val (body, nextIdx) = parseBraceBlock(tokens, i + 2, tech)
                        when {
                            hdr.startsWith("if") -> {
                                val cond = extractHeaderCondition(hdr.removePrefix("if").trim())
                                val branches = mutableListOf<Pair<String?, List<StmtNode>>>(cond to body)
                                i = nextIdx
                                while (i < tokens.size) {
                                    val nextTok = tokens[i]
                                    if (nextTok is BraceToken.Header &&
                                        tokens.getOrNull(i + 1) == BraceToken.OpenBrace
                                    ) {
                                        val nh = nextTok.text.trim()
                                        if (nh.startsWith("else if")) {
                                            val eCond = extractHeaderCondition(nh.removePrefix("else if").trim())
                                            val (eBody, afterElif) = parseBraceBlock(tokens, i + 2, tech)
                                            branches.add(eCond to eBody)
                                            i = afterElif
                                        } else if (nh == "else") {
                                            val (eBody, afterElse) = parseBraceBlock(tokens, i + 2, tech)
                                            branches.add(null to eBody)
                                            i = afterElse
                                            break
                                        } else break
                                    } else break
                                }
                                nodes.add(StmtNode.IfChain(branches))
                            }
                            hdr.startsWith("for") -> {
                                val loopHeader = extractHeaderCondition(hdr.removePrefix("for").trim())
                                nodes.add(parseBraceForLoop(loopHeader, body, tech))
                                i = nextIdx
                            }
                            hdr.startsWith("while") -> {
                                val cond = extractHeaderCondition(hdr.removePrefix("while").trim())
                                nodes.add(StmtNode.WhileLoop(cond, body))
                                i = nextIdx
                            }
                            isFunctionHeader(hdr, tech) -> {
                                val (name, params) = parseFunctionSignature(hdr, tech)
                                nodes.add(StmtNode.FuncDef(name, params, body))
                                i = nextIdx
                            }
                            else -> {
                                nodes.addAll(body)
                                i = nextIdx
                            }
                        }
                    } else {
                        nodes.add(parseSimpleStatement(hdr, tech))
                        i++
                    }
                }
            }
        }
        return nodes to i
    }

    private fun extractHeaderCondition(raw: String): String {
        val t = raw.trim()
        return if (t.startsWith("(") && t.endsWith(")")) {
            t.substring(1, t.length - 1).trim()
        } else t
    }

    private fun isFunctionHeader(hdr: String, tech: Technology): Boolean {
        return when (tech) {
            Technology.JAVASCRIPT, Technology.TYPESCRIPT -> hdr.startsWith("function ")
            Technology.KOTLIN -> hdr.startsWith("fun ")
            Technology.GO -> hdr.startsWith("func ")
            Technology.JAVA -> hdr.contains("(") && hdr.contains(")")
            else -> false
        }
    }

    private fun parseFunctionSignature(hdr: String, tech: Technology): Pair<String, List<String>> {
        val beforeParen = hdr.substringBefore("(").trim()
        val name = beforeParen.split(Regex("\\s+")).lastOrNull().orEmpty()
        val paramsSection = hdr.substringAfter("(", "").substringBeforeLast(")", "")
        if (paramsSection.isBlank()) return name to emptyList()
        val params = paramsSection.split(",").mapNotNull { rawParam ->
            val p = rawParam.trim()
            if (p.isBlank()) return@mapNotNull null
            when (tech) {
                Technology.KOTLIN, Technology.TYPESCRIPT -> p.substringBefore(":").trim()
                Technology.GO -> p.split(Regex("\\s+")).firstOrNull()?.trim()
                Technology.JAVA -> p.split(Regex("\\s+")).lastOrNull()?.trim()
                else -> p.substringBefore("=").trim()
            }
        }
        return name to params
    }

    private fun parseBraceForLoop(header: String, body: List<StmtNode>, tech: Technology): StmtNode {
        // Kotlin: i in 1..5 or i in 0 until 5
        if (header.contains(" in ")) {
            val varName = header.substringBefore(" in ").trim()
            val iterableExpr = header.substringAfter(" in ").trim()
            return StmtNode.ForEach(varName, iterableExpr, body)
        }
        // C-style: init; cond; step
        val parts = header.split(";").map { it.trim() }
        if (parts.size == 3) {
            val initNode = if (parts[0].isNotBlank()) parseSimpleStatement(parts[0], tech) else null
            val stepNode = if (parts[2].isNotBlank()) parseSimpleStatement(parts[2], tech) else null
            return StmtNode.CStyleFor(initNode, parts[1], stepNode, body)
        }
        // Go single-condition `for x < 10`
        return StmtNode.WhileLoop(header.ifBlank { "true" }, body)
    }

    // ==================== AST & STATEMENT EXECUTION ====================

    private sealed interface StmtNode {
        data class FuncDef(val name: String, val params: List<String>, val body: List<StmtNode>) : StmtNode
        data class IfChain(val branches: List<Pair<String?, List<StmtNode>>>) : StmtNode
        data class ForEach(val varName: String, val iterableExpr: String, val body: List<StmtNode>) : StmtNode
        data class CStyleFor(val init: StmtNode?, val condExpr: String, val step: StmtNode?, val body: List<StmtNode>) : StmtNode
        data class WhileLoop(val condExpr: String, val body: List<StmtNode>) : StmtNode
        data class ReturnStmt(val expr: String?) : StmtNode
        data class AssignStmt(val name: String, val expr: String, val isDeclaration: Boolean) : StmtNode
        data class ExprStmt(val expr: String) : StmtNode
    }

    private fun parseSimpleStatement(raw: String, tech: Technology): StmtNode {
        val trimmed = raw.trim().removeSuffix(";").trim()
        if (trimmed == "return") return StmtNode.ReturnStmt(null)
        if (trimmed.startsWith("return ")) return StmtNode.ReturnStmt(trimmed.removePrefix("return ").trim())

        // Handle i++ / i--
        if (trimmed.endsWith("++") && trimmed.dropLast(2).trim().matches(Regex("[a-zA-Z_]\\w*"))) {
            val v = trimmed.dropLast(2).trim()
            return StmtNode.AssignStmt(v, "$v + 1", isDeclaration = false)
        }
        if (trimmed.endsWith("--") && trimmed.dropLast(2).trim().matches(Regex("[a-zA-Z_]\\w*"))) {
            val v = trimmed.dropLast(2).trim()
            return StmtNode.AssignStmt(v, "$v - 1", isDeclaration = false)
        }

        // Handle compound assignments +=, -=, *=
        for (op in listOf("+=", "-=", "*=")) {
            val idx = findTopLevelOperator(trimmed, op)
            if (idx > 0) {
                val lhs = trimmed.substring(0, idx).trim()
                val rhs = trimmed.substring(idx + 2).trim()
                if (lhs.matches(Regex("[a-zA-Z_]\\w*"))) {
                    return StmtNode.AssignStmt(lhs, "$lhs ${op[0]} ($rhs)", isDeclaration = false)
                }
            }
        }

        // Go short variable declaration :=
        val walrusIdx = findTopLevelOperator(trimmed, ":=")
        if (walrusIdx > 0) {
            val lhs = trimmed.substring(0, walrusIdx).trim()
            val rhs = trimmed.substring(walrusIdx + 2).trim()
            return StmtNode.AssignStmt(lhs, rhs, isDeclaration = true)
        }

        val eqIdx = findTopLevelSingleEquals(trimmed)
        if (eqIdx > 0) {
            var lhs = trimmed.substring(0, eqIdx).trim()
            val rhs = trimmed.substring(eqIdx + 1).trim()
            val isDecl = lhs.startsWith("val ") || lhs.startsWith("var ") ||
                lhs.startsWith("let ") || lhs.startsWith("const ") ||
                lhs.startsWith("int ") || lhs.startsWith("double ") ||
                lhs.startsWith("String ") || lhs.startsWith("boolean ") ||
                lhs.startsWith("float ")
            lhs = lhs.removePrefix("val ").removePrefix("var ").removePrefix("let ").removePrefix("const ")
                .removePrefix("int ").removePrefix("double ").removePrefix("String ").removePrefix("boolean ").removePrefix("float ")
                .trim()
            if (tech == Technology.TYPESCRIPT || tech == Technology.KOTLIN) {
                lhs = lhs.substringBefore(":").trim()
            }
            if (lhs.matches(Regex("[a-zA-Z_]\\w*"))) {
                return StmtNode.AssignStmt(lhs, rhs, isDeclaration = isDecl)
            }
        }

        return StmtNode.ExprStmt(trimmed)
    }

    private data class ScriptFunc(val params: List<String>, val body: List<StmtNode>)

    private class Scope(val parent: Scope? = null) {
        private val vars = linkedMapOf<String, Any?>()

        fun get(name: String): Any? =
            if (vars.containsKey(name)) vars[name] else parent?.get(name)

        fun contains(name: String): Boolean =
            vars.containsKey(name) || (parent?.contains(name) == true)

        fun declare(name: String, value: Any?) {
            vars[name] = value
        }

        fun assign(name: String, value: Any?) {
            if (vars.containsKey(name) || parent == null || !parent.contains(name)) {
                vars[name] = value
            } else {
                parent.assign(name, value)
            }
        }
    }

    private class ScriptExecContext(val tech: Technology, val isPt: Boolean) {
        val globalScope = Scope()
        val functions = mutableMapOf<String, ScriptFunc>()
        val outputLines = mutableListOf<String>()
        private var steps = 0

        fun tickStep() {
            steps++
            if (steps > MAX_EXECUTION_STEPS) {
                throw IdleScriptException(
                    if (isPt) "Limite de execução atingido ($MAX_EXECUTION_STEPS passos). Verifique loops infinitos."
                    else "Execution step limit reached ($MAX_EXECUTION_STEPS steps). Check for infinite loops."
                )
            }
        }

        fun appendOutput(text: String) {
            outputLines.add(text)
        }
    }

    private sealed class ExecSignal {
        data object Normal : ExecSignal()
        data class Return(val value: Any?) : ExecSignal()
    }

    private class IdleScriptException(message: String) : RuntimeException(message)

    private fun executeBlock(
        nodes: List<StmtNode>,
        scope: Scope,
        ctx: ScriptExecContext
    ): ExecSignal {
        for (node in nodes) {
            ctx.tickStep()
            when (node) {
                is StmtNode.FuncDef -> {
                    ctx.functions[node.name] = ScriptFunc(node.params, node.body)
                }
                is StmtNode.ReturnStmt -> {
                    val v = node.expr?.let { evalExpr(it, scope, ctx) }
                    return ExecSignal.Return(v)
                }
                is StmtNode.AssignStmt -> {
                    val v = evalExpr(node.expr, scope, ctx)
                    if (node.isDeclaration) scope.declare(node.name, v)
                    else scope.assign(node.name, v)
                }
                is StmtNode.ExprStmt -> {
                    evalExpr(node.expr, scope, ctx)
                }
                is StmtNode.IfChain -> {
                    for ((condExpr, body) in node.branches) {
                        val take = if (condExpr == null) true else isTruthy(evalExpr(condExpr, scope, ctx))
                        if (take) {
                            val sig = executeBlock(body, Scope(scope), ctx)
                            if (sig is ExecSignal.Return) return sig
                            break
                        }
                    }
                }
                is StmtNode.ForEach -> {
                    val items = resolveIterable(node.iterableExpr, scope, ctx)
                    for (item in items) {
                        ctx.tickStep()
                        val loopScope = Scope(scope)
                        loopScope.declare(node.varName, item)
                        val sig = executeBlock(node.body, loopScope, ctx)
                        if (sig is ExecSignal.Return) return sig
                    }
                }
                is StmtNode.CStyleFor -> {
                    val loopScope = Scope(scope)
                    node.init?.let { executeBlock(listOf(it), loopScope, ctx) }
                    while (isTruthy(evalExpr(node.condExpr, loopScope, ctx))) {
                        ctx.tickStep()
                        val sig = executeBlock(node.body, Scope(loopScope), ctx)
                        if (sig is ExecSignal.Return) return sig
                        node.step?.let { executeBlock(listOf(it), loopScope, ctx) }
                    }
                }
                is StmtNode.WhileLoop -> {
                    while (isTruthy(evalExpr(node.condExpr, scope, ctx))) {
                        ctx.tickStep()
                        val sig = executeBlock(node.body, Scope(scope), ctx)
                        if (sig is ExecSignal.Return) return sig
                    }
                }
            }
        }
        return ExecSignal.Normal
    }

    private fun resolveIterable(expr: String, scope: Scope, ctx: ScriptExecContext): List<Any?> {
        val t = expr.trim()
        // Kotlin ranges: 1..5 or 0 until 5
        if (t.contains("..")) {
            val a = toDouble(evalExpr(t.substringBefore(".."), scope, ctx)).toInt()
            val b = toDouble(evalExpr(t.substringAfter(".."), scope, ctx)).toInt()
            return (a..b).toList()
        }
        if (t.contains(" until ")) {
            val a = toDouble(evalExpr(t.substringBefore(" until "), scope, ctx)).toInt()
            val b = toDouble(evalExpr(t.substringAfter(" until "), scope, ctx)).toInt()
            return (a until b).toList()
        }
        val v = evalExpr(t, scope, ctx)
        return when (v) {
            is List<*> -> v
            is String -> v.map { it.toString() }
            else -> emptyList()
        }
    }

    // ==================== EXPRESSION EVALUATOR ====================

    private fun evalExpr(raw: String, scope: Scope, ctx: ScriptExecContext): Any? {
        val expr = unwrapOuterParens(raw.trim())
        if (expr.isBlank()) return null

        // Boolean OR
        findTopLevelKeywordOrOp(expr, listOf("||", " or "))?.let { (lhs, _, rhs) ->
            return isTruthy(evalExpr(lhs, scope, ctx)) || isTruthy(evalExpr(rhs, scope, ctx))
        }
        // Boolean AND
        findTopLevelKeywordOrOp(expr, listOf("&&", " and "))?.let { (lhs, _, rhs) ->
            return isTruthy(evalExpr(lhs, scope, ctx)) && isTruthy(evalExpr(rhs, scope, ctx))
        }
        // Equality / Relational
        findTopLevelKeywordOrOp(expr, listOf("===", "!==", "==", "!=", "<=", ">=", "<", ">"))?.let { (lhs, op, rhs) ->
            val l = evalExpr(lhs, scope, ctx)
            val r = evalExpr(rhs, scope, ctx)
            return compareValues(l, r, op)
        }
        // Addition / Subtraction (rightmost at top level for left-associativity)
        findLastTopLevelBinaryOp(expr, listOf('+', '-'))?.let { (lhs, op, rhs) ->
            val l = evalExpr(lhs, scope, ctx)
            val r = evalExpr(rhs, scope, ctx)
            return if (op == '+') {
                if (l is String || r is String) formatValue(l) + formatValue(r)
                else numericResult(toDouble(l) + toDouble(r))
            } else {
                numericResult(toDouble(l) - toDouble(r))
            }
        }
        // Multiplication / Division / Modulo / Floor Div
        findLastTopLevelMulOp(expr)?.let { (lhs, op, rhs) ->
            val l = toDouble(evalExpr(lhs, scope, ctx))
            val r = toDouble(evalExpr(rhs, scope, ctx))
            return when (op) {
                "*" -> numericResult(l * r)
                "//" -> {
                    if (r == 0.0) throw IdleScriptException("Division by zero")
                    floor(l / r).toLong()
                }
                "/" -> {
                    if (r == 0.0) throw IdleScriptException("Division by zero")
                    numericResult(l / r)
                }
                "%" -> {
                    if (r == 0.0) throw IdleScriptException("Modulo by zero")
                    numericResult(l % r)
                }
                "**" -> numericResult(l.pow(r))
                else -> 0L
            }
        }

        // Unary NOT / minus
        if (expr.startsWith("!")) return !isTruthy(evalExpr(expr.drop(1), scope, ctx))
        if (expr.startsWith("not ")) return !isTruthy(evalExpr(expr.removePrefix("not "), scope, ctx))
        if (expr.startsWith("-")) return numericResult(-toDouble(evalExpr(expr.drop(1), scope, ctx)))

        // Python f-string
        if ((expr.startsWith("f\"") && expr.endsWith("\"")) || (expr.startsWith("f'") && expr.endsWith("'"))) {
            val inner = expr.substring(2, expr.length - 1)
            return interpolateBraceExpressions(inner, scope, ctx)
        }
        // JS Template literal `...${...}...`
        if (expr.startsWith("`") && expr.endsWith("`")) {
            val inner = expr.substring(1, expr.length - 1)
            return interpolateDollarBraceExpressions(inner, scope, ctx)
        }
        // Standard string literal (supports Kotlin $var and ${expr})
        if ((expr.startsWith("\"") && expr.endsWith("\"")) || (expr.startsWith("'") && expr.endsWith("'"))) {
            val inner = expr.substring(1, expr.length - 1)
            return if (ctx.tech == Technology.KOTLIN) {
                interpolateKotlinString(inner, scope, ctx)
            } else inner
        }

        // List literal [a, b, c]
        if (expr.startsWith("[") && expr.endsWith("]")) {
            val inner = expr.substring(1, expr.length - 1).trim()
            if (inner.isEmpty()) return mutableListOf<Any?>()
            return splitTopLevelArgs(inner).map { evalExpr(it, scope, ctx) }.toMutableList()
        }

        // Literals
        if (expr == "true" || expr == "True") return true
        if (expr == "false" || expr == "False") return false
        if (expr == "null" || expr == "None" || expr == "nil") return null
        expr.toLongOrNull()?.let { return it }
        expr.toDoubleOrNull()?.let { return it }

        // Index access e.g. arr[i]
        val bracketIdx = expr.indexOf('[')
        if (bracketIdx > 0 && expr.endsWith("]")) {
            val targetName = expr.substring(0, bracketIdx).trim()
            val idxExpr = expr.substring(bracketIdx + 1, expr.length - 1)
            val target = evalExpr(targetName, scope, ctx)
            val idx = toDouble(evalExpr(idxExpr, scope, ctx)).toInt()
            if (target is List<*>) return target.getOrNull(idx)
            if (target is String) return target.getOrNull(idx)?.toString()
        }

        // Function / method call
        val parenIdx = findCallOpenParen(expr)
        if (parenIdx > 0 && expr.endsWith(")")) {
            val callee = expr.substring(0, parenIdx).trim()
            val argsRaw = expr.substring(parenIdx + 1, expr.length - 1)
            val args = if (argsRaw.isBlank()) emptyList() else splitTopLevelArgs(argsRaw).map { evalExpr(it, scope, ctx) }
            return invokeCall(callee, args, scope, ctx)
        }

        // Property access e.g. arr.length, list.size
        val dotIdx = expr.lastIndexOf('.')
        if (dotIdx > 0) {
            val objExpr = expr.substring(0, dotIdx).trim()
            val prop = expr.substring(dotIdx + 1).trim()
            val obj = evalExpr(objExpr, scope, ctx)
            if (prop == "length" || prop == "size") {
                return when (obj) {
                    is List<*> -> obj.size.toLong()
                    is String -> obj.length.toLong()
                    else -> 0L
                }
            }
        }

        if (scope.contains(expr)) return scope.get(expr)
        throw IdleScriptException(
            if (ctx.isPt) "Símbolo ou expressão desconhecida: '$expr'" else "Unknown symbol or expression: '$expr'"
        )
    }

    private fun invokeCall(
        callee: String,
        args: List<Any?>,
        scope: Scope,
        ctx: ScriptExecContext
    ): Any? {
        when (callee) {
            "print", "println", "console.log", "System.out.println", "System.out.print",
            "fmt.Println", "fmt.Print" -> {
                ctx.appendOutput(args.joinToString(" ") { formatValue(it) })
                return null
            }
            "len" -> {
                val first = args.firstOrNull()
                return when (first) {
                    is List<*> -> first.size.toLong()
                    is String -> first.length.toLong()
                    else -> 0L
                }
            }
            "range" -> {
                val nums = args.map { toDouble(it).toInt() }
                return when (nums.size) {
                    1 -> (0 until nums[0]).map { it.toLong() }
                    2 -> (nums[0] until nums[1]).map { it.toLong() }
                    3 -> {
                        val step = nums[2].takeIf { it != 0 } ?: 1
                        val out = mutableListOf<Long>()
                        var cur = nums[0]
                        if (step > 0) {
                            while (cur < nums[1]) { out.add(cur.toLong()); cur += step }
                        } else {
                            while (cur > nums[1]) { out.add(cur.toLong()); cur += step }
                        }
                        out
                    }
                    else -> emptyList<Long>()
                }
            }
            "listOf", "mutableListOf", "arrayOf" -> return args.toMutableList()
            "sum" -> {
                val list = args.firstOrNull() as? List<*> ?: return 0L
                return numericResult(list.sumOf { toDouble(it) })
            }
            "abs", "Math.abs" -> return numericResult(abs(toDouble(args.firstOrNull())))
            "sqrt", "Math.sqrt" -> return numericResult(sqrt(toDouble(args.firstOrNull())))
            "max", "Math.max" -> {
                val flat = if (args.size == 1 && args[0] is List<*>) (args[0] as List<*>) else args
                return numericResult(flat.maxOfOrNull { toDouble(it) } ?: 0.0)
            }
            "min", "Math.min" -> {
                val flat = if (args.size == 1 && args[0] is List<*>) (args[0] as List<*>) else args
                return numericResult(flat.minOfOrNull { toDouble(it) } ?: 0.0)
            }
            "round", "Math.round" -> return toDouble(args.firstOrNull()).roundToLong()
            "int" -> return toDouble(args.firstOrNull()).toLong()
            "float" -> return toDouble(args.firstOrNull())
            "str" -> return formatValue(args.firstOrNull())
        }

        // Method call on object e.g. list.add(x) / arr.push(x) / s.uppercase()
        val dotIdx = callee.lastIndexOf('.')
        if (dotIdx > 0) {
            val targetName = callee.substring(0, dotIdx).trim()
            val method = callee.substring(dotIdx + 1).trim()
            val target = evalExpr(targetName, scope, ctx)
            if (target is MutableList<*> && (method == "append" || method == "add" || method == "push")) {
                @Suppress("UNCHECKED_CAST")
                val mutable = target as MutableList<Any?>
                args.forEach { mutable.add(it) }
                return mutable.size.toLong()
            }
            if (target is String) {
                return when (method) {
                    "upper", "uppercase", "toUpperCase" -> target.uppercase()
                    "lower", "lowercase", "toLowerCase" -> target.lowercase()
                    "trim" -> target.trim()
                    else -> target
                }
            }
        }

        val func = ctx.functions[callee] ?: throw IdleScriptException(
            if (ctx.isPt) "Função não encontrada: '$callee'" else "Undefined function: '$callee'"
        )
        val fnScope = Scope(ctx.globalScope)
        func.params.forEachIndexed { idx, pName ->
            fnScope.declare(pName, args.getOrNull(idx))
        }
        val sig = executeBlock(func.body, fnScope, ctx)
        return if (sig is ExecSignal.Return) sig.value else null
    }

    private fun interpolateBraceExpressions(inner: String, scope: Scope, ctx: ScriptExecContext): String {
        return Regex("""\{([^}]+)\}""").replace(inner) { m ->
            formatValue(evalExpr(m.groupValues[1], scope, ctx))
        }
    }

    private fun interpolateDollarBraceExpressions(inner: String, scope: Scope, ctx: ScriptExecContext): String {
        return Regex("""\$\{([^}]+)\}""").replace(inner) { m ->
            formatValue(evalExpr(m.groupValues[1], scope, ctx))
        }
    }

    private fun interpolateKotlinString(inner: String, scope: Scope, ctx: ScriptExecContext): String {
        val withExprs = interpolateDollarBraceExpressions(inner, scope, ctx)
        return Regex("""\$([a-zA-Z_]\w*)""").replace(withExprs) { m ->
            formatValue(scope.get(m.groupValues[1]))
        }
    }

    private fun compareValues(l: Any?, r: Any?, op: String): Boolean {
        return when (op) {
            "==", "===" -> if (l is Number && r is Number) l.toDouble() == r.toDouble() else l == r
            "!=", "!==" -> if (l is Number && r is Number) l.toDouble() != r.toDouble() else l != r
            "<" -> toDouble(l) < toDouble(r)
            "<=" -> toDouble(l) <= toDouble(r)
            ">" -> toDouble(l) > toDouble(r)
            ">=" -> toDouble(l) >= toDouble(r)
            else -> false
        }
    }

    private fun isTruthy(v: Any?): Boolean = when (v) {
        null -> false
        is Boolean -> v
        is Number -> v.toDouble() != 0.0
        is String -> v.isNotEmpty()
        is Collection<*> -> v.isNotEmpty()
        else -> true
    }

    private fun toDouble(v: Any?): Double = when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull() ?: 0.0
        is Boolean -> if (v) 1.0 else 0.0
        else -> 0.0
    }

    private fun numericResult(d: Double): Number {
        val rounded = d.toLong()
        return if (d == rounded.toDouble()) rounded else d
    }

    private fun formatValue(v: Any?): String = when (v) {
        null -> "null"
        is Double -> if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
        is List<*> -> v.joinToString(prefix = "[", postfix = "]") { formatValue(it) }
        else -> v.toString()
    }

    // ==================== PARSING HELPERS ====================

    private fun stripLineComment(line: String, marker: String): String {
        var inString: Char? = null
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (inString != null) {
                if (ch == inString && (i == 0 || line[i - 1] != '\\')) inString = null
            } else {
                if (ch == '"' || ch == '\'' || ch == '`') inString = ch
                else if (line.startsWith(marker, i)) return line.substring(0, i)
            }
            i++
        }
        return line
    }

    private fun findMatchingBrace(text: String, openIdx: Int): Int {
        var depth = 0
        var inString: Char? = null
        for (i in openIdx until text.length) {
            val ch = text[i]
            if (inString != null) {
                if (ch == inString && text[i - 1] != '\\') inString = null
                continue
            }
            when (ch) {
                '"', '\'', '`' -> inString = ch
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    private fun unwrapOuterParens(expr: String): String {
        var cur = expr
        while (cur.startsWith("(") && cur.endsWith(")")) {
            var depth = 0
            var inStr: Char? = null
            var wrapsAll = true
            for (i in cur.indices) {
                val ch = cur[i]
                if (inStr != null) {
                    if (ch == inStr && cur[i - 1] != '\\') inStr = null
                    continue
                }
                when (ch) {
                    '"', '\'', '`' -> inStr = ch
                    '(' -> depth++
                    ')' -> {
                        depth--
                        if (depth == 0 && i < cur.lastIndex) {
                            wrapsAll = false
                            break
                        }
                    }
                }
            }
            if (wrapsAll) cur = cur.substring(1, cur.length - 1).trim() else break
        }
        return cur
    }

    private fun splitTopLevelArgs(raw: String): List<String> {
        val result = mutableListOf<String>()
        val buf = StringBuilder()
        var depth = 0
        var inStr: Char? = null
        for (i in raw.indices) {
            val ch = raw[i]
            if (inStr != null) {
                buf.append(ch)
                if (ch == inStr && (i == 0 || raw[i - 1] != '\\')) inStr = null
                continue
            }
            when (ch) {
                '"', '\'', '`' -> { inStr = ch; buf.append(ch) }
                '(', '[', '{' -> { depth++; buf.append(ch) }
                ')', ']', '}' -> { if (depth > 0) depth--; buf.append(ch) }
                ',' -> if (depth == 0) {
                    result.add(buf.toString().trim())
                    buf.clear()
                } else buf.append(ch)
                else -> buf.append(ch)
            }
        }
        val tail = buf.toString().trim()
        if (tail.isNotEmpty()) result.add(tail)
        return result
    }

    private fun findCallOpenParen(expr: String): Int {
        var inStr: Char? = null
        var depth = 0
        for (i in expr.indices) {
            val ch = expr[i]
            if (inStr != null) {
                if (ch == inStr && (i == 0 || expr[i - 1] != '\\')) inStr = null
                continue
            }
            when (ch) {
                '"', '\'', '`' -> inStr = ch
                '(', '[', '{' -> {
                    if (ch == '(' && depth == 0) return i
                    depth++
                }
                ')', ']', '}' -> if (depth > 0) depth--
            }
        }
        return -1
    }

    private fun findTopLevelOperator(text: String, op: String): Int {
        var inStr: Char? = null
        var depth = 0
        for (i in text.indices) {
            val ch = text[i]
            if (inStr != null) {
                if (ch == inStr && (i == 0 || text[i - 1] != '\\')) inStr = null
                continue
            }
            when (ch) {
                '"', '\'', '`' -> inStr = ch
                '(', '[', '{' -> depth++
                ')', ']', '}' -> if (depth > 0) depth--
                else -> if (depth == 0 && text.startsWith(op, i)) return i
            }
        }
        return -1
    }

    private fun findTopLevelSingleEquals(text: String): Int {
        var inStr: Char? = null
        var depth = 0
        for (i in text.indices) {
            val ch = text[i]
            if (inStr != null) {
                if (ch == inStr && (i == 0 || text[i - 1] != '\\')) inStr = null
                continue
            }
            when (ch) {
                '"', '\'', '`' -> inStr = ch
                '(', '[', '{' -> depth++
                ')', ']', '}' -> if (depth > 0) depth--
                '=' -> if (depth == 0) {
                    val prev = text.getOrNull(i - 1)
                    val next = text.getOrNull(i + 1)
                    if (prev != '=' && prev != '!' && prev != '<' && prev != '>' && prev != ':' && next != '=') {
                        return i
                    }
                }
            }
        }
        return -1
    }

    private fun findTopLevelKeywordOrOp(expr: String, ops: List<String>): Triple<String, String, String>? {
        var inStr: Char? = null
        var depth = 0
        for (i in expr.indices) {
            val ch = expr[i]
            if (inStr != null) {
                if (ch == inStr && (i == 0 || expr[i - 1] != '\\')) inStr = null
                continue
            }
            when (ch) {
                '"', '\'', '`' -> inStr = ch
                '(', '[', '{' -> depth++
                ')', ']', '}' -> if (depth > 0) depth--
                else -> if (depth == 0) {
                    for (op in ops) {
                        if (expr.startsWith(op, i)) {
                            val lhs = expr.substring(0, i).trim()
                            val rhs = expr.substring(i + op.length).trim()
                            if (lhs.isNotEmpty() && rhs.isNotEmpty()) {
                                return Triple(lhs, op.trim(), rhs)
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun findLastTopLevelBinaryOp(expr: String, ops: List<Char>): Triple<String, Char, String>? {
        var inStr: Char? = null
        var depth = 0
        var lastMatch: Triple<String, Char, String>? = null
        for (i in expr.indices) {
            val ch = expr[i]
            if (inStr != null) {
                if (ch == inStr && (i == 0 || expr[i - 1] != '\\')) inStr = null
                continue
            }
            when (ch) {
                '"', '\'', '`' -> inStr = ch
                '(', '[', '{' -> depth++
                ')', ']', '}' -> if (depth > 0) depth--
                in ops -> if (depth == 0 && i > 0) {
                    val prevNonSpace = expr.substring(0, i).trimEnd().lastOrNull()
                    if (prevNonSpace != null && prevNonSpace !in listOf('+', '-', '*', '/', '%', '=', '!', '<', '>', '(', '[')) {
                        val lhs = expr.substring(0, i).trim()
                        val rhs = expr.substring(i + 1).trim()
                        if (lhs.isNotEmpty() && rhs.isNotEmpty()) {
                            lastMatch = Triple(lhs, ch, rhs)
                        }
                    }
                }
            }
        }
        return lastMatch
    }

    private fun findLastTopLevelMulOp(expr: String): Triple<String, String, String>? {
        var inStr: Char? = null
        var depth = 0
        var lastMatch: Triple<String, String, String>? = null
        var i = 0
        while (i < expr.length) {
            val ch = expr[i]
            if (inStr != null) {
                if (ch == inStr && (i == 0 || expr[i - 1] != '\\')) inStr = null
                i++
                continue
            }
            when (ch) {
                '"', '\'', '`' -> inStr = ch
                '(', '[', '{' -> depth++
                ')', ']', '}' -> if (depth > 0) depth--
                else -> if (depth == 0 && i > 0) {
                    val op = when {
                        expr.startsWith("**", i) -> "**"
                        expr.startsWith("//", i) -> "//"
                        ch == '*' || ch == '/' || ch == '%' -> ch.toString()
                        else -> null
                    }
                    if (op != null) {
                        val lhs = expr.substring(0, i).trim()
                        val rhs = expr.substring(i + op.length).trim()
                        if (lhs.isNotEmpty() && rhs.isNotEmpty()) {
                            lastMatch = Triple(lhs, op, rhs)
                        }
                        i += op.length
                        continue
                    }
                }
            }
            i++
        }
        return lastMatch
    }
}
