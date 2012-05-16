/*
 * Copyright 2012 Cedric Dandoy
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.bug4j.server.processor;

import org.bug4j.common.FullStackHashCalculator;
import org.bug4j.common.TextToLines;
import org.bug4j.gwt.user.client.data.BugHit;
import org.bug4j.gwt.user.client.data.Stack;
import org.bug4j.gwt.user.client.data.Strain;
import org.bug4j.server.store.Store;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * This is how we ingest a report.
 */
public final class BugProcessor {
    /**
     * When trying to match by title, how many stacks we want to look at.
     */
    private static final int MATCH_BY_TITLE_LOOK_BACK_MAX = 1;
    private static final boolean COLLECT_STATISTICS = false;
    private static int _matchByStrain = 0;
    private static int _matchByCauses = 0;

    private BugProcessor() {
    }

    public static void printStatistics() {
        if (COLLECT_STATISTICS) {
            System.out.println("_matchByStrain = " + _matchByStrain);
            System.out.println("_matchByCauses = " + _matchByCauses);
        }
    }

    /**
     * @return the bugid that was matched.
     */
    public static long process(final Store store, long sessionId, String app, @Nullable String message, long dateReported, @Nullable String user, @Nullable String stackText) {
        final List<String> stackLines;
        if (stackText != null) {
            stackText = stackText.trim();
            stackLines = stackText.isEmpty() ? null : TextToLines.toLineList(stackText);
        } else {
            stackLines = null;
        }

        // First try based on the full hash of the exception.
        // In theory the client should not have sent the stack if an exact match already existed.
        final String fullHash = hash(message, stackLines);
        Stack stack;
        if (stackLines != null) {
            // Try to find a matching strain.
            final String strainHash = StackPathHashCalculator.analyze(stackLines);
            Strain strain = store.getStrainByHash(app, strainHash);
            if (strain == null) {
                // Determine the title that the bug would get.
                final List<String> appPackages = store.getPackages(app);
                final StackAnalyzer stackAnalyzer = new StackAnalyzer();
                stackAnalyzer.setApplicationPackages(appPackages);

                String title = stackAnalyzer.getTitle(stackLines);
                if (title == null) { // This may happen if the stack does not contain any of the application packages.
                    // Try again without application packages
                    stackAnalyzer.setApplicationPackages(Collections.<String>emptyList());
                    title = stackAnalyzer.getTitle(stackLines);
                }

                if (title == null) {
                    throw new IllegalStateException("Failed to analyze a stack [\n" + stackText + "\n]");
                }

                // Try to find a bug with the exact same title
                Long bugId = identifyBugByTitle(store, app, stackLines, title);
                if (bugId == null) {
                    // if everything failed then it is a new bug.
                    bugId = store.createBug(app, title);
                } else {
                    if (COLLECT_STATISTICS) {
                        _matchByCauses++;
                        System.out.println("Match by cause: " + bugId);
                    }
                }
                strain = store.createStrain(bugId, strainHash);
            } else {
                if (COLLECT_STATISTICS) {
                    System.out.println("Match by strain: " + strain.getBugId());
                    _matchByStrain++;
                }
            }
            stack = store.createStack(strain.getBugId(), strain.getStrainId(), fullHash, stackText);
        } else {
            Long bugId = identifyBugByTitle(store, app, message);
            if (bugId == null) {
                bugId = store.createBug(app, message);
            }
            stack = new Stack(bugId, null);
        }
        store.reportHitOnStack(sessionId, message, dateReported, user, stack);

        return stack.getBugId();
    }

    private static String hash(@Nullable String message, @Nullable List<String> stackLines) {
        final String ret;
        final List<String> hashable;
        if (stackLines != null) {
            hashable = stackLines;
        } else {
            hashable = Collections.singletonList(message);
        }
        ret = FullStackHashCalculator.getTextHash(hashable);
        return ret;
    }

    /**
     * Tries to deduplicate based on the exact location and causes.
     * This addresses the case for example of a NPE at the exact same location but from different code paths.
     * To match, the underlying causes must be identical.
     */
    private static Long identifyBugByTitle(Store store, String app, List<String> thisStackLines, String title) {
        final StackAnalyzer stackAnalyzer = new StackAnalyzer();
        final List<String> thisCauses = stackAnalyzer.getCauses(thisStackLines);
        final List<Long> bugIds = store.getBugIdByTitle(app, title);
        for (long bugId : bugIds) {
            final List<BugHit> hits = store.getHits(bugId, 0, MATCH_BY_TITLE_LOOK_BACK_MAX, "C"); // only look back at the last hits
            for (BugHit hit : hits) {
                final long hitId = hit.getHitId();
                final String thatStackText = store.getStack(hitId);
                final List<String> thatStackLines = TextToLines.toLineList(thatStackText);
                final List<String> thatCauses = stackAnalyzer.getCauses(thatStackLines);
                if (thisCauses.equals(thatCauses)) {
                    return bugId;
                }
            }
        }
        return null;
    }

    /**
     * Tries to deduplicate based on the exact title.
     * This is used only when there is no stack trace
     */
    private static Long identifyBugByTitle(Store store, String app, String title) {
        Long ret = null;
        final List<Long> bugIds = store.getBugIdByTitle(app, title);
        if (bugIds.size() > 0) {
            ret = bugIds.get(0);
        }
        return ret;
    }
}
