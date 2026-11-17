package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.IdleExecutionResult
import com.example.model.IdleExecutionStatus
import com.example.model.Language
import com.example.model.LanguageIdleCatalog
import com.example.model.Technology

private val IdeBg = Color(0xFF0C0917)
private val IdeGutterBg = Color(0xFF141024)
private val IdeBorder = Color(0xFF2B2444)
private val IdeConsoleBg = Color(0xFF090711)
private val IdeSuccess = Color(0xFF22C55E)
private val IdeError = Color(0xFFF87171)

@Composable
fun LanguageIdleWorkspace(
    tech: Technology,
    code: String,
    executionResult: IdleExecutionResult?,
    language: Language,
    onCodeChange: (String) -> Unit,
    onRunCode: () -> Unit,
    onResetCode: () -> Unit,
    onClearOutput: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val spec = remember(tech) { LanguageIdleCatalog.getSpec(tech) }
    val clipboard = LocalClipboardManager.current
    var copiedNotice by remember(tech) { mutableStateOf(false) }

    val lineCount = remember(code) { code.lines().size.coerceAtLeast(1) }
    val syntaxHighlight = remember(tech) { IdleSyntaxHighlighter(tech) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("idle_workspace_${tech.id}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. IDLE Header & Toolbar
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, tech.accentColor.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tech.accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tech.icon,
                                contentDescription = null,
                                tint = tech.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = spec.idleTitle(language),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = spec.defaultFileName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tech.accentColor
                                )
                                Text(
                                    text = "• ${spec.runtimeBadge}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(code))
                                copiedNotice = true
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("btn_idle_copy_${tech.id}")
                        ) {
                            Icon(
                                imageVector = if (copiedNotice) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                contentDescription = if (isPt) "Copiar código" else "Copy code",
                                tint = if (copiedNotice) IdeSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                copiedNotice = false
                                onResetCode()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("btn_idle_reset_${tech.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = if (isPt) "Restaurar exemplo" else "Reset template",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Template selector chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(spec.templates, key = { it.id }) { template ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, tech.accentColor.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    copiedNotice = false
                                    onCodeChange(template.code)
                                }
                                .testTag("chip_idle_template_${template.id}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = template.title(language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Code Editor with Line Numbers & Syntax Highlighting
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = IdeBg,
            border = BorderStroke(1.dp, IdeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Window chrome bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IdeGutterBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = spec.defaultFileName,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFC4B5FD)
                        )
                    }
                    Text(
                        text = "$lineCount ${if (isPt) "linhas" else "lines"}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF8E84B2)
                    )
                }

                // Line numbers + editable code area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 210.dp)
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(IdeGutterBg.copy(alpha = 0.55f))
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (n in 1..lineCount) {
                            Text(
                                text = n.toString(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFF6B628E)
                            )
                        }
                    }

                    BasicTextField(
                        value = code,
                        onValueChange = {
                            copiedNotice = false
                            onCodeChange(it)
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                            color = Color(0xFFF3ECFF)
                        ),
                        cursorBrush = SolidColor(tech.accentColor),
                        visualTransformation = syntaxHighlight,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                            .testTag("input_idle_editor_${tech.id}")
                    )
                }

                HorizontalDivider(color = IdeBorder)

                // Quick-Insert Symbol Strip for mobile coding ergonomics
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IdeGutterBg)
                ) {
                    items(spec.quickTokens) { token ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF211B38),
                            border = BorderStroke(1.dp, tech.accentColor.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    copiedNotice = false
                                    val separator = if (code.isNotEmpty() && !code.endsWith("\n") && !code.endsWith(" ")) " " else ""
                                    onCodeChange(code + separator + token)
                                }
                        ) {
                            Text(
                                text = token.trim(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE9D5FF),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Run Code Button
        val isRunning = executionResult?.status == IdleExecutionStatus.RUNNING
        Button(
            onClick = onRunCode,
            enabled = !isRunning,
            colors = ButtonDefaults.buttonColors(containerColor = tech.accentColor),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_idle_run_${tech.id}")
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPt) "Executando..." else "Running...",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPt) "Executar ${spec.idleTitle(language)}" else "Run ${spec.idleTitle(language)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // 4. Execution Console / Table / Live HTML+CSS Preview Output
        IdleOutputPanel(
            tech = tech,
            result = executionResult,
            isPt = isPt,
            onClearOutput = onClearOutput
        )
    }
}

@Composable
private fun IdleOutputPanel(
    tech: Technology,
    result: IdleExecutionResult?,
    isPt: Boolean,
    onClearOutput: () -> Unit
) {
    val status = result?.status ?: IdleExecutionStatus.READY
    val statusColor = when (status) {
        IdleExecutionStatus.READY -> Color(0xFF94A3B8)
        IdleExecutionStatus.RUNNING -> Color(0xFFFBBF24)
        IdleExecutionStatus.SUCCESS -> IdeSuccess
        IdleExecutionStatus.ERROR -> IdeError
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = IdeConsoleBg,
        border = BorderStroke(
            1.dp,
            if (status == IdleExecutionStatus.ERROR) IdeError.copy(alpha = 0.6f)
            else IdeBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("panel_idle_output_${tech.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (result?.htmlPreviewDocument != null) Icons.Default.Web else Icons.Default.Terminal,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when (status) {
                            IdleExecutionStatus.READY -> if (isPt) "Console Pronto" else "Console Ready"
                            IdleExecutionStatus.RUNNING -> if (isPt) "Executando..." else "Running..."
                            IdleExecutionStatus.SUCCESS -> if (isPt) "Saída (${result?.executionTimeMs ?: 0} ms)" else "Output (${result?.executionTimeMs ?: 0} ms)"
                            IdleExecutionStatus.ERROR -> if (isPt) "Erro de Execução" else "Execution Error"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                if (result != null && status != IdleExecutionStatus.READY) {
                    IconButton(
                        onClick = onClearOutput,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_idle_clear_output_${tech.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = if (isPt) "Limpar console" else "Clear console",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            when {
                result == null || status == IdleExecutionStatus.READY -> {
                    Text(
                        text = if (isPt) {
                            ">>> Pressione 'Executar' para rodar o código em ${tech.displayName}."
                        } else {
                            ">>> Tap 'Run' to execute your ${tech.displayName} code."
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF7C739E)
                    )
                }

                result.errorMessage != null -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = IdeError,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = result.errorMessage,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = IdeError
                        )
                    }
                }

                result.htmlPreviewDocument != null -> {
                    result.outputLines.forEach { line ->
                        Text(
                            text = ">>> $line",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF86EFAC)
                        )
                    }
                    HtmlCssLiveWebPreview(
                        htmlDocument = result.htmlPreviewDocument,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                result.tableColumns.isNotEmpty() -> {
                    result.outputLines.forEach { line ->
                        Text(
                            text = ">>> $line",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF86EFAC)
                        )
                    }
                    IdleSqlTableOutput(
                        columns = result.tableColumns,
                        rows = result.tableRows
                    )
                }

                else -> {
                    if (result.outputLines.isEmpty()) {
                        Text(
                            text = if (isPt) "(Execução concluída sem saída no console)" else "(Finished with no console output)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    } else {
                        result.outputLines.forEach { line ->
                            Text(
                                text = line,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HtmlCssLiveWebPreview(
    htmlDocument: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlDocument, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

@Composable
private fun IdleSqlTableOutput(
    columns: List<String>,
    rows: List<List<String>>
) {
    val hScroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(hScroll)
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFF1E1930))
                .padding(vertical = 6.dp, horizontal = 8.dp)
        ) {
            columns.forEach { col ->
                Text(
                    text = col,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC4B5FD),
                    modifier = Modifier.width(120.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (rows.isEmpty()) {
            Text(
                text = "(0 rows)",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(8.dp)
            )
        } else {
            rows.forEachIndexed { idx, row ->
                Row(
                    modifier = Modifier
                        .background(if (idx % 2 == 0) Color(0xFF100C1C) else Color(0xFF151024))
                        .padding(vertical = 5.dp, horizontal = 8.dp)
                ) {
                    row.forEach { cell ->
                        Text(
                            text = cell,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.width(120.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private class IdleSyntaxHighlighter(private val tech: Technology) : VisualTransformation {
    private val keywordColor = Color(0xFFC084FC)
    private val stringColor = Color(0xFF86EFAC)
    private val numberColor = Color(0xFFFDBA74)
    private val commentColor = Color(0xFF64748B)
    private val typeOrBuiltInColor = Color(0xFF38BDF8)

    private val keywords = when (tech) {
        Technology.SQL, Technology.MYSQL, Technology.POSTGRESQL -> setOf(
            "SELECT", "FROM", "WHERE", "ORDER", "BY", "GROUP", "LIMIT", "AS", "WITH",
            "JOIN", "ON", "AND", "OR", "PRAGMA", "EXPLAIN", "QUERY", "PLAN", "SHOW", "TABLES", "DESCRIBE"
        )
        Technology.HTML -> setOf("html", "head", "body", "main", "section", "div", "span", "h1", "h2", "p", "ul", "li", "button", "input", "label", "style")
        Technology.CSS -> setOf("display", "flex", "grid", "color", "background", "padding", "margin", "border", "border-radius", "gap", "font-family")
        else -> setOf(
            "fun", "def", "func", "function", "val", "var", "let", "const", "int", "String", "double", "boolean",
            "if", "elif", "else", "for", "while", "in", "return", "public", "static", "class", "package", "import", "interface", "range", "true", "false", "True", "False"
        )
    }

    private val builtIns = setOf("print", "println", "console", "log", "System", "out", "fmt", "Println", "len", "sum", "max", "min", "find", "insertOne", "countDocuments")

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val highlighted = buildAnnotatedString {
            append(raw)
            // Strings
            Regex(""""([^"\\]|\\.)*"|'([^'\\]|\\.)*'|`([^`\\]|\\.)*`""").findAll(raw).forEach { m ->
                addStyle(SpanStyle(color = stringColor), m.range.first, m.range.last + 1)
            }
            // Numbers
            Regex("""\b\d+(\.\d+)?\b""").findAll(raw).forEach { m ->
                addStyle(SpanStyle(color = numberColor), m.range.first, m.range.last + 1)
            }
            // Words (keywords & builtins)
            Regex("""\b[a-zA-Z_][a-zA-Z0-9_-]*\b""").findAll(raw).forEach { m ->
                val word = m.value
                when {
                    word in keywords || word.uppercase() in keywords ->
                        addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), m.range.first, m.range.last + 1)
                    word in builtIns ->
                        addStyle(SpanStyle(color = typeOrBuiltInColor, fontWeight = FontWeight.SemiBold), m.range.first, m.range.last + 1)
                }
            }
            // Line comments (# or //)
            Regex("""(#|//).*""").findAll(raw).forEach { m ->
                addStyle(SpanStyle(color = commentColor), m.range.first, m.range.last + 1)
            }
        }
        return TransformedText(highlighted, OffsetMapping.Identity)
    }
}
