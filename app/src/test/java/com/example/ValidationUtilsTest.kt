package com.example

import com.example.domain.validation.ValidationResult
import com.example.domain.validation.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun validateGrade_withinRange_returnsValid() {
        val result1 = ValidationUtils.validateGrade(1.0)
        val result2 = ValidationUtils.validateGrade(3.5)
        val result3 = ValidationUtils.validateGrade(5.0)

        assertTrue(result1.isValid)
        assertTrue(result2.isValid)
        assertTrue(result3.isValid)
    }

    @Test
    fun validateGrade_outOfRange_returnsInvalid() {
        val resultLow = ValidationUtils.validateGrade(0.9)
        val resultHigh = ValidationUtils.validateGrade(5.1)

        assertFalse(resultLow.isValid)
        assertFalse(resultHigh.isValid)
    }

    @Test
    fun sanitizeGrade_clampsAndRoundsCorrectly() {
        assertEquals(1.0, ValidationUtils.sanitizeGrade(0.5), 0.01)
        assertEquals(5.0, ValidationUtils.sanitizeGrade(6.0), 0.01)
        assertEquals(4.8, ValidationUtils.sanitizeGrade(4.78), 0.01)
    }

    @Test
    fun validateNonEmptyText_validatesCorrectly() {
        val valid = ValidationUtils.validateNonEmptyText("Examen de Álgebra", "Título")
        val empty = ValidationUtils.validateNonEmptyText("   ", "Título")
        val tooShort = ValidationUtils.validateNonEmptyText("A", "Título", minLength = 2)

        assertTrue(valid.isValid)
        assertFalse(empty.isValid)
        assertFalse(tooShort.isValid)
    }

    @Test
    fun validateTimeFormat_validatesHHmm() {
        assertTrue(ValidationUtils.validateTimeFormat("07:30").isValid)
        assertTrue(ValidationUtils.validateTimeFormat("23:59").isValid)
        assertFalse(ValidationUtils.validateTimeFormat("25:00").isValid)
        assertFalse(ValidationUtils.validateTimeFormat("7:30 PM").isValid)
        assertFalse(ValidationUtils.validateTimeFormat("invalid").isValid)
    }

    @Test
    fun validateCreditCost_validatesRange() {
        assertTrue(ValidationUtils.validateCreditCost(150).isValid)
        assertFalse(ValidationUtils.validateCreditCost(0).isValid)
        assertFalse(ValidationUtils.validateCreditCost(-10).isValid)
        assertFalse(ValidationUtils.validateCreditCost(20000).isValid)
    }
}
