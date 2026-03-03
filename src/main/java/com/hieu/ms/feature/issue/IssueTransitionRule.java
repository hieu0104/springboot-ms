package com.hieu.ms.feature.issue;

import java.util.*;

/**
 * Defines valid status transitions for issues.
 * Enforces workflow rules: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 */
public class IssueTransitionRule {

    private static final Map<IssueStatus, Set<IssueStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(IssueStatus.class);

    static {
        // TODO can move to IN_PROGRESS
        ALLOWED_TRANSITIONS.put(IssueStatus.TODO, EnumSet.of(IssueStatus.IN_PROGRESS));

        // IN_PROGRESS can move to REVIEW (dev done) or TODO (rollback)
        ALLOWED_TRANSITIONS.put(IssueStatus.IN_PROGRESS, EnumSet.of(IssueStatus.REVIEW, IssueStatus.TODO));

        // REVIEW can move to DONE (approved) or IN_PROGRESS (rejected/rework)
        ALLOWED_TRANSITIONS.put(IssueStatus.REVIEW, EnumSet.of(IssueStatus.DONE, IssueStatus.IN_PROGRESS));

        // DONE can reopen to TODO
        ALLOWED_TRANSITIONS.put(IssueStatus.DONE, EnumSet.of(IssueStatus.TODO));
    }

    public static boolean isAllowed(IssueStatus from, IssueStatus to) {
        if (from == null || to == null) return false;
        Set<IssueStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static Set<IssueStatus> getAllowedTargets(IssueStatus from) {
        if (from == null) return Collections.emptySet();
        return ALLOWED_TRANSITIONS.getOrDefault(from, Collections.emptySet());
    }
}
