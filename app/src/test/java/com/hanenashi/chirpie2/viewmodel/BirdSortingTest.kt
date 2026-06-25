package com.hanenashi.chirpie2.viewmodel

import com.hanenashi.chirpie2.data.model.Bird
import com.hanenashi.chirpie2.data.preferences.BirdList
import com.hanenashi.chirpie2.data.preferences.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class BirdSortingTest {
    private val birds = listOf(
        bird(
            id = 2,
            japanese = "スズメ",
            english = "Tree Sparrow",
            czech = "Vrabec polní",
            scientific = "Passer montanus"
        ),
        bird(
            id = 1,
            japanese = "アオジ",
            english = "Black-faced Bunting",
            czech = "Strnad šedohlavý",
            scientific = "Emberiza spodocephala"
        ),
        bird(
            id = 3,
            japanese = "コゲラ",
            english = "Japanese Pygmy Woodpecker",
            czech = "Strakapoud japonský",
            scientific = "Yungipicus kizuki"
        )
    )

    @Test
    fun customOrderUsesBirdId() {
        assertOrder(SortOrder.Custom, 2, 1, 3)
    }

    @Test
    fun japaneseOrderUsesJapaneseName() {
        assertOrder(SortOrder.Japanese, 1, 3, 2)
    }

    @Test
    fun englishOrderUsesEnglishName() {
        assertOrder(SortOrder.English, 1, 3, 2)
    }

    @Test
    fun czechOrderUsesCzechName() {
        assertOrder(SortOrder.Czech, 3, 1, 2)
    }

    @Test
    fun scientificOrderUsesScientificName() {
        assertOrder(SortOrder.Scientific, 1, 2, 3)
    }

    @Test
    fun allBirdsListDoesNotFilter() {
        assertEquals(
            listOf(2L, 1L, 3L),
            filterBirds(birds, BirdList.All, emptyMap()).map(Bird::id)
        )
    }

    @Test
    fun savedListOnlyIncludesMembers() {
        val memberships = mapOf(
            1L to setOf(BirdList.Favorites),
            2L to setOf(BirdList.Study, BirdList.Winter)
        )

        assertEquals(
            listOf(1L),
            filterBirds(birds, BirdList.Favorites, memberships).map(Bird::id)
        )
        assertEquals(
            listOf(2L),
            filterBirds(birds, BirdList.Study, memberships).map(Bird::id)
        )
    }

    private fun assertOrder(sortOrder: SortOrder, vararg expectedIds: Long) {
        assertEquals(expectedIds.toList(), sortBirds(birds, sortOrder).map(Bird::id))
    }

    private fun bird(
        id: Long,
        japanese: String,
        english: String,
        czech: String,
        scientific: String
    ) = Bird(
        id = id,
        englishName = english,
        romanizedJapaneseName = "",
        kanjiJapaneseName = japanese,
        czechName = czech,
        scientificName = scientific,
        imageUrl = "",
        audioAssetPath = "",
        sortIndex = when (id) {
            2L -> 0
            1L -> 1
            else -> 2
        }
    )
}
