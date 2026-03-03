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
            "TODO, IN_PROGRESS",
            "IN_PROGRESS, REVIEW",
            "IN_PROGRESS, TODO",
            "REVIEW, DONE",
            "REVIEW, IN_PROGRESS",
            "DONE, TODO"
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
            "TODO, REVIEW", // Phải qua IN_PROGRESS trước
            "TODO, DONE", // Không skip bước
            "TODO, TODO", // Không tự chuyển sang chính mình
            "IN_PROGRESS, DONE", // Phải qua REVIEW trước
            "IN_PROGRESS, IN_PROGRESS",
            "REVIEW, TODO", // Phải về IN_PROGRESS trước
            "REVIEW, REVIEW",
            "DONE, IN_PROGRESS", // Phải reopen (TODO) trước
            "DONE, REVIEW",
            "DONE, DONE"
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
            assertThat(IssueTransitionRule.isAllowed(null, IssueStatus.TODO)).isFalse();
        }

        @Test
        @DisplayName("any status → null should be rejected")
        void shouldRejectNullTo() {
            assertThat(IssueTransitionRule.isAllowed(IssueStatus.TODO, null)).isFalse();
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
        @DisplayName("TODO can go to IN_PROGRESS")
        void todoTargets() {
            Set<IssueStatus> targets = IssueTransitionRule.getAllowedTargets(IssueStatus.TODO);
            assertThat(targets).containsExactlyInAnyOrder(IssueStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("IN_PROGRESS can go to REVIEW or TODO")
        void inProgressTargets() {
            Set<IssueStatus> targets = IssueTransitionRule.getAllowedTargets(IssueStatus.IN_PROGRESS);
            assertThat(targets).containsExactlyInAnyOrder(IssueStatus.REVIEW, IssueStatus.TODO);
        }

        @Test
        @DisplayName("null returns empty set")
        void nullReturnsEmpty() {
            assertThat(IssueTransitionRule.getAllowedTargets(null)).isEmpty();
        }
    }
}
