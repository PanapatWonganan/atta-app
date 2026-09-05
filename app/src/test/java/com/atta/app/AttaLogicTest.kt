package com.atta.app

import com.atta.app.data.AffirmationRepository
import com.atta.app.data.Daypart
import com.atta.app.data.WidgetThemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AttaLogicTest {

    @Test
    fun `line of day is deterministic`() {
        val date = LocalDate.of(2026, 9, 1)
        val a = AffirmationRepository.lineFor(date, setOf("rest"))
        val b = AffirmationRepository.lineFor(date, setOf("rest"))
        assertEquals(a.id, b.id)
    }

    @Test
    fun `morning line never uses a night-only affirmation`() {
        repeat(60) { offset ->
            val date = LocalDate.of(2026, 1, 1).plusDays(offset.toLong())
            val line = AffirmationRepository.lineFor(date, emptySet(), evening = false)
            assertNotEquals(Daypart.NIGHT, line.daypart)
        }
    }

    @Test
    fun `evening line never uses a morning-only affirmation`() {
        repeat(60) { offset ->
            val date = LocalDate.of(2026, 1, 1).plusDays(offset.toLong())
            val line = AffirmationRepository.lineFor(date, emptySet(), evening = true)
            assertNotEquals(Daypart.MORNING, line.daypart)
        }
    }

    @Test
    fun `focus filter narrows the pool`() {
        repeat(30) { offset ->
            val date = LocalDate.of(2026, 3, 1).plusDays(offset.toLong())
            val line = AffirmationRepository.lineFor(date, setOf("rest"), evening = false)
            assertEquals("rest", line.categoryId)
        }
    }

    @Test
    fun `feed starts today and walks backwards`() {
        val today = LocalDate.of(2026, 9, 1)
        val feed = AffirmationRepository.feed(today, 5)
        assertEquals(5, feed.size)
        assertEquals(today, feed[0].first)
        assertEquals(today.minusDays(4), feed[4].first)
    }

    @Test
    fun `eight themes with ordered stops`() {
        assertEquals(8, WidgetThemes.All.size)
        WidgetThemes.All.forEach { theme ->
            assertTrue(theme.stops.size >= 2)
            assertEquals(0f, theme.stops.first().first)
            assertEquals(1f, theme.stops.last().first)
            assertTrue(theme.stops.zipWithNext().all { (a, b) -> a.first < b.first })
        }
    }

    @Test
    fun `gradient endpoints span the surface`() {
        WidgetThemes.All.forEach { theme ->
            val (start, end) = theme.gradientPoints(330f, 158f)
            val dx = end.x - start.x
            val dy = end.y - start.y
            assertTrue(dx * dx + dy * dy > 0f)
        }
    }

    @Test
    fun `widget date stamp is short`() {
        val label = AffirmationRepository.shortDate(LocalDate.of(2026, 9, 1))
        assertEquals("1 Sep", label)
    }
}
