package com.example

import com.example.data.repository.TechContentRepository
import com.example.model.Language
import com.example.model.StudySession
import com.example.model.Technology
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `technologies enum contains all required tracks`() {
        val tracks = Technology.entries.map { it.id }
        assertEquals(8, tracks.size)
        assertTrue(tracks.contains("python"))
        assertTrue(tracks.contains("sql"))
        assertTrue(tracks.contains("html"))
        assertTrue(tracks.contains("css"))
        assertTrue(tracks.contains("java"))
        assertTrue(tracks.contains("mysql"))
        assertTrue(tracks.contains("postgresql"))
        assertTrue(tracks.contains("mongodb"))
    }

    @Test
    fun `all eight technologies provide hands-on practical activities`() {
        for (tech in Technology.entries) {
            val activities = TechContentRepository.getPracticalActivities(tech)
            assertTrue("Tech ${tech.id} must have practical activities", activities.isNotEmpty())
            for (act in activities) {
                assertNotNull(act.id)
                assertTrue(act.pointsValue > 0)
                assertTrue(act.requiredKeywords.isNotEmpty())
                assertTrue(act.starterCode.isNotBlank())
                assertTrue(act.solutionCode.isNotBlank())
                assertTrue(act.title(Language.PT).isNotBlank())
                assertTrue(act.title(Language.EN).isNotBlank())
            }
        }
    }

    @Test
    fun `all eight technologies provide quiz questions with point value`() {
        for (tech in Technology.entries) {
            val questions = TechContentRepository.getQuizQuestions(tech)
            assertTrue("Tech ${tech.id} must have quiz questions", questions.isNotEmpty())
            for (q in questions) {
                assertTrue(q.pointsValue >= 20)
                assertTrue(q.options(Language.PT).size >= 2)
            }
        }
    }

    @Test
    fun `study session model properties mapping`() {
        val session = StudySession(
            tech = Technology.PYTHON,
            titlePt = "Python Básico",
            titleEn = "Basic Python",
            topicPt = "Listas",
            topicEn = "Lists",
            notesPt = "Praticar",
            notesEn = "Practice",
            dateIso = "2026-11-09",
            timeFormatted = "19:00",
            durationMinutes = 45,
            isCompleted = false
        )

        assertEquals("Python Básico", session.title(Language.PT))
        assertEquals("Basic Python", session.title(Language.EN))
        assertEquals("Listas", session.topic(Language.PT))
        assertEquals("Lists", session.topic(Language.EN))
        assertNotNull(session)
    }

    @Test
    fun `default user badges cover streaks, python master, night owl, and activity milestones`() {
        val badges = com.example.data.repository.UserBadgeRepository.DEFAULT_BADGES
        assertTrue(badges.size >= 10)

        val badgeIds = badges.map { it.id }
        assertTrue("Must include Python Master", badgeIds.contains("badge_python_master"))
        assertTrue("Must include Night Owl Learner", badgeIds.contains("badge_night_owl"))
        assertTrue("Must include Early Bird Coder", badgeIds.contains("badge_early_bird"))
        assertTrue("Must include 3-day Streak", badgeIds.contains("badge_streak_3"))
        assertTrue("Must include 7-day Streak", badgeIds.contains("badge_streak_7"))
        assertTrue("Must include SQL Wizard", badgeIds.contains("badge_sql_wizard"))
        assertTrue("Must include First Code Craft", badgeIds.contains("badge_code_crafter"))
        assertTrue("Must include Challenge Hunter (5 acts)", badgeIds.contains("badge_activity_5"))
        assertTrue("Must include Polyglot", badgeIds.contains("badge_polyglot"))
        assertTrue("Must include AI Collaborator", badgeIds.contains("badge_ai_collaborator"))

        for (badge in badges) {
            assertTrue(badge.title(Language.PT).isNotBlank())
            assertTrue(badge.title(Language.EN).isNotBlank())
            assertTrue(badge.description(Language.PT).isNotBlank())
            assertTrue(badge.description(Language.EN).isNotBlank())
            assertTrue(badge.maxProgress >= 1)
            assertTrue(badge.xpReward > 0)
        }
    }

    @Test
    fun `user badge entity progress fraction calculates accurately`() {
        val badge = com.example.data.local.UserBadgeEntity(
            id = "test_badge",
            titlePt = "Teste",
            titleEn = "Test",
            descriptionPt = "Desc",
            descriptionEn = "Desc",
            category = "STREAK",
            iconKey = "streak",
            isUnlocked = false,
            currentProgress = 3,
            maxProgress = 7,
            xpReward = 100
        )
        assertEquals(3f / 7f, badge.progressFraction, 0.001f)
    }

    @Test
    fun `technology filter correctly filters study sessions by language`() {
        val sessions = listOf(
            StudySession(id = 1, tech = Technology.PYTHON, titlePt = "Python 1", titleEn = "Python 1", topicPt = "", topicEn = "", dateIso = "2026-11-09"),
            StudySession(id = 2, tech = Technology.SQL, titlePt = "SQL 1", titleEn = "SQL 1", topicPt = "", topicEn = "", dateIso = "2026-11-09"),
            StudySession(id = 3, tech = Technology.JAVA, titlePt = "Java 1", titleEn = "Java 1", topicPt = "", topicEn = "", dateIso = "2026-11-09")
        )

        val pythonFilter: Technology? = Technology.PYTHON
        val filtered = sessions.filter { pythonFilter == null || it.tech == pythonFilter }
        assertEquals(1, filtered.size)
        assertEquals(Technology.PYTHON, filtered.first().tech)

        val allFilter: Technology? = null
        val allFiltered = sessions.filter { allFilter == null || it.tech == allFilter }
        assertEquals(3, allFiltered.size)
    }

    @Test
    fun `technology filter matches routines by targetTechsCsv and targetTechnologies`() {
        val routine1 = com.example.data.local.StudyRoutineEntity(
            id = 1,
            title = "Backend Master",
            description = "Python and MySQL",
            targetTechnologies = "Python, MySQL",
            targetTechsCsv = "python,mysql"
        )
        val routine2 = com.example.data.local.StudyRoutineEntity(
            id = 2,
            title = "Frontend Master",
            description = "HTML & CSS",
            targetTechnologies = "HTML, CSS",
            targetTechsCsv = "html,css"
        )

        fun matchesTech(r: com.example.data.local.StudyRoutineEntity, tech: Technology): Boolean {
            val filterId = tech.id.lowercase()
            return r.targetTechsCsv.lowercase().contains(filterId) ||
                    r.targetTechnologies.lowercase().contains(filterId)
        }

        assertTrue(matchesTech(routine1, Technology.PYTHON))
        assertTrue(matchesTech(routine1, Technology.MYSQL))
        assertTrue(!matchesTech(routine1, Technology.HTML))

        assertTrue(matchesTech(routine2, Technology.HTML))
        assertTrue(matchesTech(routine2, Technology.CSS))
        assertTrue(!matchesTech(routine2, Technology.JAVA))
    }
}
