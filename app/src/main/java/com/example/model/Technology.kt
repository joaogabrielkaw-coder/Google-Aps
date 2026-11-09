package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.TechCssColor
import com.example.ui.theme.TechHtmlColor
import com.example.ui.theme.TechJavaColor
import com.example.ui.theme.TechMongoColor
import com.example.ui.theme.TechMySqlColor
import com.example.ui.theme.TechPostgresColor
import com.example.ui.theme.TechPythonColor
import com.example.ui.theme.TechSqlColor

enum class Technology(
    val id: String,
    val displayName: String,
    val categoryPt: String,
    val categoryEn: String,
    val shortDescPt: String,
    val shortDescEn: String,
    val badge: String,
    val accentColor: Color,
    val icon: ImageVector
) {
    PYTHON(
        id = "python",
        displayName = "Python",
        categoryPt = "Linguagem de Programação",
        categoryEn = "Programming Language",
        shortDescPt = "Sintaxe limpa, scripts rápidos, automação, backend e ciência de dados.",
        shortDescEn = "Clean syntax, fast scripting, automation, backend & data science.",
        badge = "Script & AI",
        accentColor = TechPythonColor,
        icon = Icons.Default.Terminal
    ),
    SQL(
        id = "sql",
        displayName = "SQL",
        categoryPt = "Linguagem de Consulta",
        categoryEn = "Query Language",
        shortDescPt = "Linguagem padrão para consultas e manipulação em bancos de dados relacionais.",
        shortDescEn = "Standard language for querying and managing relational databases.",
        badge = "Queries",
        accentColor = TechSqlColor,
        icon = Icons.Default.Storage
    ),
    HTML(
        id = "html",
        displayName = "HTML5",
        categoryPt = "Web Frontend",
        categoryEn = "Web Frontend",
        shortDescPt = "Estruturação semântica, formulários e base de toda aplicação web moderna.",
        shortDescEn = "Semantic structure, forms, and the foundation of all modern web apps.",
        badge = "Web Markup",
        accentColor = TechHtmlColor,
        icon = Icons.Default.Html
    ),
    CSS(
        id = "css",
        displayName = "CSS3",
        categoryPt = "Estilização Web",
        categoryEn = "Web Styling",
        shortDescPt = "Layouts responsivos, Flexbox, Grid, animações e design moderno.",
        shortDescEn = "Responsive layouts, Flexbox, Grid, animations, and modern UI design.",
        badge = "Styles & UI",
        accentColor = TechCssColor,
        icon = Icons.Default.Code
    ),
    JAVA(
        id = "java",
        displayName = "Java",
        categoryPt = "Linguagem de Programação",
        categoryEn = "Programming Language",
        shortDescPt = "Orientação a objetos robusta, ecossistema corporativo e alta performance.",
        shortDescEn = "Robust OOP, enterprise ecosystem, JVM, and high performance.",
        badge = "OOP & Backend",
        accentColor = TechJavaColor,
        icon = Icons.Default.DataObject
    ),
    MYSQL(
        id = "mysql",
        displayName = "MySQL",
        categoryPt = "SGBD Relacional",
        categoryEn = "Relational DBMS",
        shortDescPt = "O banco relacional open-source mais popular do mundo, rápido e confiável.",
        shortDescEn = "World's most popular open-source relational database, fast & reliable.",
        badge = "RDBMS",
        accentColor = TechMySqlColor,
        icon = Icons.Default.Storage
    ),
    POSTGRESQL(
        id = "postgresql",
        displayName = "PostgreSQL",
        categoryPt = "SGBD Objeto-Relacional",
        categoryEn = "Object-Relational DBMS",
        shortDescPt = "Banco relacional avançado com suporte a JSONB, CTEs, concorrência e extensões.",
        shortDescEn = "Advanced relational database with JSONB, CTEs, concurrency & extensions.",
        badge = "Advanced SQL",
        accentColor = TechPostgresColor,
        icon = Icons.Default.Dataset
    ),
    MONGODB(
        id = "mongodb",
        displayName = "MongoDB",
        categoryPt = "Banco NoSQL (Documentos)",
        categoryEn = "NoSQL Database (Docs)",
        shortDescPt = "Banco NoSQL líder orientado a documentos BSON/JSON, escalável e flexível.",
        shortDescEn = "Leading document-oriented NoSQL database with BSON/JSON and scalable pipelines.",
        badge = "NoSQL / BSON",
        accentColor = TechMongoColor,
        icon = Icons.Default.Dataset
    );

    companion object {
        fun fromId(id: String): Technology = entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PYTHON
    }
}
