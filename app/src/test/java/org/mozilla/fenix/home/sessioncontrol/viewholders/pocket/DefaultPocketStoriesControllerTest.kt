/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.home.sessioncontrol.viewholders.pocket

import androidx.navigation.NavController
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import mozilla.components.service.pocket.PocketRecommendedStory
import org.junit.Test
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.home.HomeFragmentAction
import org.mozilla.fenix.home.HomeFragmentState
import org.mozilla.fenix.home.HomeFragmentStore

class DefaultPocketStoriesControllerTest {
    @Test
    fun `GIVEN a category is selected WHEN that same category is clicked THEN deselect it`() {
        val category1 = PocketRecommendedStoriesCategory("cat1", emptyList())
        val category2 = PocketRecommendedStoriesCategory("cat2", emptyList())
        val selections = listOf(PocketRecommendedStoriesSelectedCategory(category2.name))
        val store = spyk(
            HomeFragmentStore(
                HomeFragmentState(
                    pocketStoriesCategories = listOf(category1, category2),
                    pocketStoriesCategoriesSelections = selections
                )
            )
        )
        val controller = DefaultPocketStoriesController(mockk(), store, mockk())

        controller.handleCategoryClick(category1)
        verify(exactly = 0) { store.dispatch(HomeFragmentAction.DeselectPocketStoriesCategory(category1.name)) }

        controller.handleCategoryClick(category2)
        verify { store.dispatch(HomeFragmentAction.DeselectPocketStoriesCategory(category2.name)) }
    }

    @Test
    fun `GIVEN 8 categories are selected WHEN when a new one is clicked THEN the oldest selected is deselected before selecting the new one`() {
        val category1 = PocketRecommendedStoriesSelectedCategory(name = "cat1", selectionTimestamp = 111)
        val category2 = PocketRecommendedStoriesSelectedCategory(name = "cat2", selectionTimestamp = 222)
        val category3 = PocketRecommendedStoriesSelectedCategory(name = "cat3", selectionTimestamp = 333)
        val oldestSelectedCategory = PocketRecommendedStoriesSelectedCategory(name = "oldestSelectedCategory", selectionTimestamp = 0)
        val category4 = PocketRecommendedStoriesSelectedCategory(name = "cat4", selectionTimestamp = 444)
        val category5 = PocketRecommendedStoriesSelectedCategory(name = "cat5", selectionTimestamp = 555)
        val category6 = PocketRecommendedStoriesSelectedCategory(name = "cat6", selectionTimestamp = 678)
        val category7 = PocketRecommendedStoriesSelectedCategory(name = "cat7", selectionTimestamp = 890)
        val newSelectedCategory = PocketRecommendedStoriesSelectedCategory(name = "newSelectedCategory", selectionTimestamp = 654321)
        val store = spyk(
            HomeFragmentStore(
                HomeFragmentState(
                    pocketStoriesCategoriesSelections = listOf(
                        category1, category2, category3, category4, category5, category6, category7, oldestSelectedCategory
                    )
                )
            )
        )
        val controller = DefaultPocketStoriesController(mockk(), store, mockk())

        controller.handleCategoryClick(PocketRecommendedStoriesCategory(newSelectedCategory.name))

        verify { store.dispatch(HomeFragmentAction.DeselectPocketStoriesCategory(oldestSelectedCategory.name)) }
        verify { store.dispatch(HomeFragmentAction.SelectPocketStoriesCategory(newSelectedCategory.name)) }
    }

    @Test
    fun `GIVEN fewer than 8 categories are selected WHEN when a new one is clicked THEN don't deselect anything but select the newly clicked category`() {
        val category1 = PocketRecommendedStoriesSelectedCategory(name = "cat1", selectionTimestamp = 111)
        val category2 = PocketRecommendedStoriesSelectedCategory(name = "cat2", selectionTimestamp = 222)
        val category3 = PocketRecommendedStoriesSelectedCategory(name = "cat3", selectionTimestamp = 333)
        val oldestSelectedCategory = PocketRecommendedStoriesSelectedCategory(name = "oldestSelectedCategory", selectionTimestamp = 0)
        val category4 = PocketRecommendedStoriesSelectedCategory(name = "cat4", selectionTimestamp = 444)
        val category5 = PocketRecommendedStoriesSelectedCategory(name = "cat5", selectionTimestamp = 555)
        val category6 = PocketRecommendedStoriesSelectedCategory(name = "cat6", selectionTimestamp = 678)
        val store = spyk(
            HomeFragmentStore(
                HomeFragmentState(
                    pocketStoriesCategoriesSelections = listOf(
                        category1, category2, category3, category4, category5, category6, oldestSelectedCategory
                    )
                )
            )
        )
        val controller = DefaultPocketStoriesController(mockk(), store, mockk())

        controller.handleCategoryClick(PocketRecommendedStoriesCategory("newSelectedCategory"))

        verify(exactly = 0) { store.dispatch(HomeFragmentAction.DeselectPocketStoriesCategory(oldestSelectedCategory.name)) }
        verify { store.dispatch(HomeFragmentAction.SelectPocketStoriesCategory("newSelectedCategory")) }
    }

    @Test
    fun `WHEN new stories are shown THEN update the State`() {
        val store = spyk(HomeFragmentStore())
        val controller = DefaultPocketStoriesController(mockk(), store, mockk())
        val storiesShown: List<PocketRecommendedStory> = mockk()

        controller.handleStoriesShown(storiesShown)

        verify { store.dispatch(HomeFragmentAction.PocketStoriesShown(storiesShown)) }
    }

    @Test
    fun `WHEN an external link is clicked THEN link is opened`() {
        val link = "https://www.mozilla.org/en-US/firefox/pocket/"
        val homeActivity: HomeActivity = mockk(relaxed = true)
        val controller = DefaultPocketStoriesController(homeActivity, mockk(), mockk(relaxed = true))

        controller.handleExternalLinkClick(link)

        verify { homeActivity.openToBrowserAndLoad(link, true, BrowserDirection.FromHome) }
    }

    @Test
    fun `WHEN an external link is clicked THEN link is opened and search dismissed`() {
        val link = "https://www.mozilla.org/en-US/firefox/pocket/"
        val homeActivity: HomeActivity = mockk(relaxed = true)
        val navController: NavController = mockk(relaxed = true)

        every { navController.currentDestination } returns mockk {
            every { id } returns R.id.searchDialogFragment
        }

        val controller = DefaultPocketStoriesController(homeActivity, mockk(), navController)
        controller.handleExternalLinkClick(link)

        verify { homeActivity.openToBrowserAndLoad(link, true, BrowserDirection.FromHome) }
        verify { navController.navigateUp() }
    }
}
