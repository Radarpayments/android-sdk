package net.payrdr.mobile.payment.sdk.utils

import androidx.test.filters.SmallTest
import io.kotest.matchers.shouldBe
import org.junit.Test

@SmallTest
class OrderStatusesTest {

    @Test
    fun payedStatuesContainsDepositedAndApproved() {
        OrderStatuses.payedStatues.contains(OrderStatus.DEPOSITED) shouldBe true
        OrderStatuses.payedStatues.contains(OrderStatus.APPROVED) shouldBe true
    }

    @Test
    fun payedStatuesDoesNotContainDeclinedOrCreated() {
        OrderStatuses.payedStatues.contains(OrderStatus.DECLINED) shouldBe false
        OrderStatuses.payedStatues.contains(OrderStatus.CREATED) shouldBe false
    }

    @Test
    fun payedCouldNotBeCompletedContainsDeclined() {
        OrderStatuses.payedCouldNotBeCompleted.contains(OrderStatus.DECLINED) shouldBe true
    }

    @Test
    fun payedCouldNotBeCompletedDoesNotContainSuccessStatuses() {
        OrderStatuses.payedCouldNotBeCompleted.contains(OrderStatus.DEPOSITED) shouldBe false
        OrderStatuses.payedCouldNotBeCompleted.contains(OrderStatus.APPROVED) shouldBe false
    }

    @Test
    fun containsAnyOfKeywordIgnoreCaseMatchesExact() {
        "DEPOSITED".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe true
        "APPROVED".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe true
        "DECLINED".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedCouldNotBeCompleted) shouldBe true
    }

    @Test
    fun containsAnyOfKeywordIgnoreCaseIsCaseInsensitive() {
        "deposited".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe true
        "Approved".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe true
        "declined".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedCouldNotBeCompleted) shouldBe true
    }

    @Test
    fun containsAnyOfKeywordIgnoreCaseReturnsFalseForUnknownStatus() {
        "PENDING".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedStatues) shouldBe false
        "PENDING".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedCouldNotBeCompleted) shouldBe false
        "CREATED".containsAnyOfKeywordIgnoreCase(OrderStatuses.payedCouldNotBeCompleted) shouldBe false
    }

    @Test
    fun allOrderStatusEnumValuesHaveCorrectStatusName() {
        OrderStatus.CREATED.statusName shouldBe "CREATED"
        OrderStatus.DEPOSITED.statusName shouldBe "DEPOSITED"
        OrderStatus.APPROVED.statusName shouldBe "APPROVED"
        OrderStatus.DECLINED.statusName shouldBe "DECLINED"
    }
}
