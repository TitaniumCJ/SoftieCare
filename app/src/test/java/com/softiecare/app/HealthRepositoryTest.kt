package com.softiecare.app

import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.core.model.Scenario
import com.softiecare.app.core.model.StatusLevel
import com.softiecare.app.core.model.VitalIndicator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Tests for the Blueprint 4.3 rules and docs/data_guide.md
class HealthRepositoryTest {

    private fun ind(id: String): VitalIndicator =
        HealthRepository.indicators.value.first { it.id == id }

    private fun level(id: String, v: Float) = HealthRepository.evaluateLevel(id, v)

    @Before
    fun reset() {
        HealthRepository.injectScenario(Scenario.NOMINAL)
    }

    @Test
    fun cutoffsBpSys() {
        assertEquals(StatusLevel.CRITICAL, level("BP_SYS", 89.99f))
        assertEquals(StatusLevel.WARNING, level("BP_SYS", 90f))
        assertEquals(StatusLevel.WARNING, level("BP_SYS", 94.99f))
        assertEquals(StatusLevel.NORMAL, level("BP_SYS", 95f))
        assertEquals(StatusLevel.NORMAL, level("BP_SYS", 129.99f))
        assertEquals(StatusLevel.WARNING, level("BP_SYS", 130f))
        assertEquals(StatusLevel.WARNING, level("BP_SYS", 139.99f))
        assertEquals(StatusLevel.CRITICAL, level("BP_SYS", 140f))
    }

    @Test
    fun cutoffsHeartRate() {
        assertEquals(StatusLevel.CRITICAL, level("HEART_RATE", 49.99f))
        assertEquals(StatusLevel.WARNING, level("HEART_RATE", 50f))
        assertEquals(StatusLevel.WARNING, level("HEART_RATE", 59.99f))
        assertEquals(StatusLevel.NORMAL, level("HEART_RATE", 60f))
        assertEquals(StatusLevel.NORMAL, level("HEART_RATE", 90f))
        assertEquals(StatusLevel.WARNING, level("HEART_RATE", 90.01f))
        assertEquals(StatusLevel.WARNING, level("HEART_RATE", 105f))
        assertEquals(StatusLevel.CRITICAL, level("HEART_RATE", 105.01f))
    }

    @Test
    fun cutoffsGlucose() {
        assertEquals(StatusLevel.CRITICAL, level("GLUCOSE", 59.99f))
        assertEquals(StatusLevel.WARNING, level("GLUCOSE", 60f))
        assertEquals(StatusLevel.WARNING, level("GLUCOSE", 69.99f))
        assertEquals(StatusLevel.NORMAL, level("GLUCOSE", 70f))
        assertEquals(StatusLevel.NORMAL, level("GLUCOSE", 99.99f))
        assertEquals(StatusLevel.WARNING, level("GLUCOSE", 100f))
        assertEquals(StatusLevel.WARNING, level("GLUCOSE", 125f))
        assertEquals(StatusLevel.CRITICAL, level("GLUCOSE", 125.01f))
    }

    @Test
    fun cutoffsTemp() {
        assertEquals(StatusLevel.CRITICAL, level("TEMP", 35.49f))
        assertEquals(StatusLevel.WARNING, level("TEMP", 35.5f))
        assertEquals(StatusLevel.WARNING, level("TEMP", 36.09f))
        assertEquals(StatusLevel.NORMAL, level("TEMP", 36.1f))
        assertEquals(StatusLevel.NORMAL, level("TEMP", 37.29f))
        assertEquals(StatusLevel.WARNING, level("TEMP", 37.3f))
        assertEquals(StatusLevel.WARNING, level("TEMP", 37.99f))
        assertEquals(StatusLevel.CRITICAL, level("TEMP", 38.0f))
    }

    @Test
    fun cutoffsSleep() {
        assertEquals(StatusLevel.CRITICAL, level("SLEEP", 5.49f))
        assertEquals(StatusLevel.WARNING, level("SLEEP", 5.5f))
        assertEquals(StatusLevel.WARNING, level("SLEEP", 6.99f))
        assertEquals(StatusLevel.NORMAL, level("SLEEP", 7.0f))
        assertEquals(StatusLevel.NORMAL, level("SLEEP", 9.0f))
        assertEquals(StatusLevel.WARNING, level("SLEEP", 9.01f))
        assertEquals(StatusLevel.WARNING, level("SLEEP", 10.0f))
        assertEquals(StatusLevel.CRITICAL, level("SLEEP", 10.01f))
    }

    @Test
    fun cutoffsMood() {
        assertEquals(StatusLevel.CRITICAL, level("MOOD", 2.49f))
        assertEquals(StatusLevel.WARNING, level("MOOD", 2.5f))
        assertEquals(StatusLevel.WARNING, level("MOOD", 3.99f))
        assertEquals(StatusLevel.NORMAL, level("MOOD", 4.0f))
    }

    @Test
    fun seedsInNormalRange() {
        HealthRepository.indicators.value.forEach { i ->
            assertEquals("${i.id} has 7 readings", 7, i.history7Days.size)
            assertEquals("${i.id} last reading", i.normalValue, i.history7Days.last())
            i.history7Days.forEach { v ->
                assertEquals("${i.id} reading $v", StatusLevel.NORMAL, level(i.id, v))
            }
        }
    }

    @Test
    fun entryRotatesHistory() {
        val before = ind("BP_SYS").history7Days
        HealthRepository.updateIndicatorValue("BP_SYS", 145f)
        val i = ind("BP_SYS")
        assertEquals(145f, i.currentValue)
        assertEquals(StatusLevel.CRITICAL, i.level)
        assertEquals(7, i.history7Days.size)
        assertEquals(before.drop(1) + 145f, i.history7Days)
    }

    @Test
    fun newMeasurementAfterCountermeasureReclassifies() {
        // The countermeasure does not fix the value: the new measurement does
        HealthRepository.updateIndicatorValue("TEMP", 38.4f)
        assertEquals(StatusLevel.CRITICAL, ind("TEMP").level)
        HealthRepository.updateIndicatorValue("TEMP", 36.8f)
        val i = ind("TEMP")
        assertEquals(StatusLevel.NORMAL, i.level)
        assertEquals(listOf(38.4f, 36.8f), i.history7Days.takeLast(2))
        assertEquals(7, i.history7Days.size)
    }

    @Test
    fun fastPvtChangesNothing() {
        val before = ind("MOOD")
        HealthRepository.recordPvtResult(300L)
        HealthRepository.recordPvtResult(380L)
        assertEquals(before, ind("MOOD"))
    }

    @Test
    fun mediumPvtLowersTo3() {
        HealthRepository.recordPvtResult(381L)
        assertEquals(3.0f, ind("MOOD").currentValue)
        assertEquals(StatusLevel.WARNING, ind("MOOD").level)
    }

    @Test
    fun slowPvtLowersTo2() {
        HealthRepository.recordPvtResult(501L)
        assertEquals(2.0f, ind("MOOD").currentValue)
        assertEquals(StatusLevel.CRITICAL, ind("MOOD").level)
    }

    @Test
    fun pvtNeitherDuplicatesNorImproves() {
        HealthRepository.recordPvtResult(600L)
        val afterSlow = ind("MOOD")
        HealthRepository.recordPvtResult(600L)
        HealthRepository.recordPvtResult(450L)
        assertEquals(afterSlow, ind("MOOD"))
    }

    @Test
    fun radiationScenarioOnlyTemp() {
        val before = HealthRepository.indicators.value
        HealthRepository.injectScenario(Scenario.RADIATION_SPIKE)
        val after = HealthRepository.indicators.value
        assertEquals(38.4f, ind("TEMP").currentValue)
        assertEquals(StatusLevel.CRITICAL, ind("TEMP").level)
        before.zip(after).filter { it.first.id != "TEMP" }.forEach {
            assertEquals("${it.first.id} must not change", it.first, it.second)
        }
    }

    @Test
    fun fatigueScenarioOnlySleepAndMood() {
        val before = HealthRepository.indicators.value
        HealthRepository.injectScenario(Scenario.ISOLATION_FATIGUE)
        assertEquals(4.5f, ind("SLEEP").currentValue)
        assertEquals(StatusLevel.CRITICAL, ind("SLEEP").level)
        assertEquals(2.0f, ind("MOOD").currentValue)
        assertEquals(StatusLevel.CRITICAL, ind("MOOD").level)
        val after = HealthRepository.indicators.value
        before.zip(after).filter { it.first.id !in setOf("SLEEP", "MOOD") }.forEach {
            assertEquals("${it.first.id} must not change", it.first, it.second)
        }
    }

    @Test
    fun nominalResetsAll() {
        val inicial = HealthRepository.indicators.value
        HealthRepository.injectScenario(Scenario.ISOLATION_FATIGUE)
        HealthRepository.updateIndicatorValue("BP_SYS", 145f)
        HealthRepository.injectScenario(Scenario.NOMINAL)
        assertEquals(inicial, HealthRepository.indicators.value)
        assertTrue("all stable", HealthRepository.indicators.value.all { it.level == StatusLevel.NORMAL })
    }
}
