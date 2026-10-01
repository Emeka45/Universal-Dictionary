package com.emeka45.universaldictionary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class OfflineDictionaryTest {
    @Test fun bundledEntryIsAvailableOffline() {
        val entry=OfflineDictionary.get("integrity")
        assertNotNull(entry)
        assertEquals("integrity",entry?.word)
    }
    @Test fun specialistTermsAreAvailable() {
        assertNotNull(SpecialistDictionary.get("affidavit"))
        assertNotNull(SpecialistDictionary.get("wahala"))
    }
}
