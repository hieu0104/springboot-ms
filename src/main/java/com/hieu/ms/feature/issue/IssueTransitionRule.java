package com.hieu.ms.feature.issue;

import java.util.*;

/**
 * Defines valid status transitions for issues.
 * Enforces workflow rules: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 */
public class IssueTransitionRule {

    private static final Map<IssueStatus, Set<IssueStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(IssueStatus.class);

    static {
        // OPEN can move to IN_PROGRESS or CLOSED (cancelled)
        ALLOWED_TRANSITIONS.put(IssueStatus.OPEN, EnumSet.of(IssueStatus.IN_PROGRESS, IssueStatus.CLOSED));

        // IN_PROGRESS can move to RESOLVED (done) or OPEN (rollback)
        ALLOWED_TRANSITIONS.put(IssueStatus.IN_PROGRESS, EnumSet.of(IssueStatus.RESOLVED, IssueStatus.OPEN));

        // RESOLVED can move to CLOSED (approved) or IN_PROGRESS (rejected, needs rework)
        ALLOWED_TRANSITIONS.put(IssueStatus.RESOLVED, EnumSet.of(IssueStatus.CLOSED, IssueStatus.IN_PROGRESS));

        // CLOSED can reopen to OPEN
        ALLOWED_TRANSITIONS.put(IssueStatus.CLOSED, EnumSet.of(IssueStatus.OPEN));
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
