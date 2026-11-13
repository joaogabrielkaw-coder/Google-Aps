package com.example.data.challenges

import com.example.model.PracticalActivity
import com.example.model.Technology

object TechChallengeBank {

    /**
     * Retorna a lista completa de mais de 100 desafios balanceados para a tecnologia informada.
     */
    fun getChallenges(tech: Technology): List<PracticalActivity> = when (tech) {
        Technology.PYTHON -> PythonChallenges.list
        Technology.SQL -> SqlChallenges.list
        Technology.HTML -> HtmlChallenges.list
        Technology.CSS -> CssChallenges.list
        Technology.JAVA -> JavaChallenges.list
        Technology.MYSQL -> MySqlChallenges.list
        Technology.POSTGRESQL -> PostgreSqlChallenges.list
        Technology.MONGODB -> MongoDbChallenges.list
        Technology.JAVASCRIPT -> JavaScriptChallenges.list
        Technology.TYPESCRIPT -> TypeScriptChallenges.list
        Technology.KOTLIN -> KotlinChallenges.list
        Technology.GO -> GoChallenges.list
    }

    /**
     * Retorna o total de desafios disponíveis em todo o aplicativo.
     */
    fun getTotalChallengesCount(): Int = Technology.entries.sumOf { getChallenges(it).size }
}
