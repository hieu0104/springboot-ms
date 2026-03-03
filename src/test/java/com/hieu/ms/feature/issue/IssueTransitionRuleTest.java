package com.hieu.ms.feature.issue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit Test — IssueTransitionRule (State Machine)
 *
 * Test loại: UNIT TEST (pure logic, không cần Spring context)
 * Tại sao unit test: Class này là pure function, không dependency nào
 * Framework: JUnit 5 + AssertJ + @ParameterizedTest
 */
class IssueTransitionRuleTest {

    @Nested
    @DisplayName("isAllowed — Valid Transitions")
    class ValidTransitions {

        @ParameterizedTest(name = "{0} → {1} should be ALLOWED")
        @CsvSource({
            "OPEN, IN_PROGRESS",
            "OPEN, CLOSED",
            "IN_PROGRESS, RESOLVED",
            "IN_PROGRESS, OPEN",
            "RESOLVED, CLOSED",
            "RESOLVED, IN_PROGRESS",
            "CLOSED, OPEN"
        })
        void shouldAllowValidTransitions(IssueStatus from, IssueStatus to) {
            assertThat(IssueTransitionRule.isAllowed(from, to))
                    .as("Transition %s → %s should be allowed", from, to)
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("isAllowed — Invalid Transitions")
    class InvalidTransitions {

        @ParameterizedTest(name = "{0} → {1} should be REJECTED")
        @CsvSource({
            "OPEN, RESOLVED", // Phải qua IN_PROGRESS trước
            "OPEN, OPEN", // Không tự chuyển sang chính mình
            "IN_PROGRESS, CLOSED", // Phải qua RESOLVED trước
            "IN_PROGRESS, IN_PROGRESS",
            "RESOLVED, OPEN", // Phải về IN_PROGRESS trước
            "RESOLVED, RESOLVED",
            "CLOSED, IN_PROGRESS", // Phải reopen (OPEN) trước
            "CLOSED, RESOLVED",
            "CLOSED, CLOSED"
        })
        void shouldRejectInvalidTransitions(IssueStatus from, IssueStatus to) {
            assertThat(IssueTransitionRule.isAllowed(from, to))
                    .as("Transition %s → %s should be rejected", from, to)
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases — Null handling")
    class EdgeCases {

        @Test
        @DisplayName("null → any status should be rejected")
        void shouldRejectNullFrom() {
            assertThat(IssueTransitionRule.isAllowed(null, IssueStatus.OPEN)).isFalse();
        }

        @Test
        @DisplayName("any status → null should be rejected")
        void shouldRejectNullTo() {
            assertThat(IssueTransitionRule.isAllowed(IssueStatus.OPEN, null)).isFalse();
        }

        @Test
        @DisplayName("null → null should be rejected")
        void shouldRejectBothNull() {
            assertThat(IssueTransitionRule.isAllowed(null, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("getAllowedTargets")
    class AllowedTargets {

        @Test
        @DisplayName("OPEN can go to IN_PROGRESS or CLOSED")
        void openTargets() {
            Set<IssueStatus> targets = IssueTransitionRule.getAllowedTargets(IssueStatus.OPEN);
            assertThat(targets).containsExactlyInAnyOrder(IssueStatus.IN_PROGRESS, IssueStatus.CLOSED);
        }

        @Test
        @DisplayName("IN_PROGRESS can go to RESOLVED or OPEN")
        void inProgressTargets() {
            Set<IssueStatus> targets = IssueTransitionRule.getAllowedTargets(IssueStatus.IN_PROGRESS);
            assertThat(targets).containsExactlyInAnyOrder(IssueStatus.RESOLVED, IssueStatus.OPEN);
        }

        @Test
        @DisplayName("null returns empty set")
        void nullReturnsEmpty() {
            assertThat(IssueTransitionRule.getAllowedTargets(null)).isEmpty();
        }
    }
}
