package com.emeka45.universaldictionary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineDictionaryTest {
    @Test fun bundledWordsAreAvailable() {
        assertTrue(OfflineDictionary.get("integrity") != null)
        assertTrue(OfflineDictionary.get("comprehensive") != null)
    }
    @Test fun specialistTermsAreAvailable() {
        assertTrue(SpecialistDictionary.get("affidavit") != null)
        assertTrue(SpecialistDictionary.get("wahala") != null)
        assertTrue(SpecialistDictionary.get("algorithm") != null)
    }
    @Test fun morphologyGeneratesUsefulCandidates() {
        assertTrue(WordSearch.candidates("studies").contains("study"))
        assertTrue(WordSearch.candidates("running").contains("runn"))
    }
    @Test fun normalizationTrimsAndCollapsesSpaces() {
        assertEquals("hello world", WordSearch.normalize("  Hello   World "))
    }
}
