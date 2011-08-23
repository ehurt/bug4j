/*
 * Copyright 2011 Cedric Dandoy
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

import java.util.List;

/**
 * This is how we ingest a report.
 */
public final class BugProcessor {
    /**
     * When trying to match by title, how many stacks we want to look at.
     */
    private static final int MATCH_BY_TITLE_LOOK_BACK_MAX = 5;

    private BugProcessor() {
    }

    /**
     * @return the bugid that was matched.
     */
    public static long process(final Store store, String app, String version, @Nullable String message, long dateReported, @Nullable String user, String stackText) {
        stackText = stackText.trim();
        final List<String> stackLines = TextToLines.toLineList(stackText);

        // First try based on the full hash of the exception.
        // In theory the client should not have sent the stack if an exact match already existed.
        final String fullHash = FullStackHashCalculator.getTextHash(stackLines);
        Stack stack = store.getStackByHash(app, fullHash);
        if (stack == null) {
            // Try to find a matching strain.
            final String strainHash = StackPathHashCalculator.analyze(stackLines);
            Strain strain = store.getStrainByHash(app, strainHash);
            if (strain == null) {
                // Determine the title that the bug would get.
                final List<String> appPackages = store.getPackages(app);
                final StackAnalyzer stackAnalyzer = new StackAnalyzer();
                stackAnalyzer.setApplicationPackages(appPackages);
                final String title = stackAnalyzer.getTitle(stackLines);

                if (title == null) {
                    throw new IllegalStateException("Failed to analyze a stack [\n" + stackText + "\n]");
                }

                // Try to find a bug with the exact same title
                Long bugId = identifyBugByTitle(store, app, stackLines, title);
                if (bugId == null) {
                    // if everything failed then it is a new bug.
                    bugId = store.createBug(app, title);
                }
                strain = store.createStrain(bugId, strainHash);
            }
            stack = store.createStack(strain.getBugId(), strain.getStrainId(), fullHash, stackText);
        }
        store.reportHitOnStack(version, message, dateReported, user, stack);

        return stack.getBugId();
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
            final List<BugHit> hits = store.getHits(bugId, null, 0, MATCH_BY_TITLE_LOOK_BACK_MAX, "D"); // only look back at the last hits
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
}
