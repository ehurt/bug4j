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
import org.bug4j.common.StackAnalyzer;
import org.bug4j.common.StackPathHashCalculator;
import org.bug4j.server.gwt.client.data.Hit;
import org.bug4j.server.gwt.client.data.Stack;
import org.bug4j.server.gwt.client.data.Strain;
import org.bug4j.server.gwt.client.util.TextToLines;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BugProcessor {
    /**
     * When trying to match by title, how many stacks we want to look at.
     */
    private static final int MATCH_BY_TITLE_LOOK_BACK_MAX = 30;

    private BugProcessor() {
    }

    /**
     * @return the bugid that was matched.
     */
    public static long process(String app, String version, @Nullable String message, @Nullable String user, String stackText) {
        final Store store = StoreFactory.getStore();
        final List<String> stackLines = TextToLines.toList(stackText);

        final String fullHash = FullStackHashCalculator.getTextHash(stackLines);

        Stack stack = store.getStackByHash(app, fullHash);
        if (stack == null) {
            final List<String> appPackages = store.getPackages(app);
            final StackAnalyzer stackAnalyzer = new StackAnalyzer();
            stackAnalyzer.setApplicationPackages(appPackages);
            final String title = stackAnalyzer.getTitle(stackLines);

            if (title == null) {
                throw new IllegalStateException("Failed to analyze a stack [\n" + stackText + "\n]");
            }

            final String strainHash = StackPathHashCalculator.analyze(stackLines);
            Strain strain = store.getStrainByHash(app, strainHash);
            if (strain == null) {
                Long bugId = identifyBugByTitle(store, app, stackLines, title);
                if (bugId == null) {
                    bugId = store.createBug(app, title);
                }
                strain = store.createStrain(app, bugId, strainHash);
            }
            stack = store.createStack(app, strain.getBugId(), strain.getStrainId(), fullHash, stackText);
        }
        store.reportHitOnStack(app, version, message, user, stack);

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
            final List<Hit> hits = store.getHits(bugId, 0, MATCH_BY_TITLE_LOOK_BACK_MAX, "D"); // only look back at the last hits
            for (Hit hit : hits) {
                final long hitId = hit.getId();
                final String thatStackText = store.getStack(hitId);
                final List<String> thatStackLines = TextToLines.toList(thatStackText);
                final List<String> thatCauses = stackAnalyzer.getCauses(thatStackLines);
                if (thisCauses.equals(thatCauses)) {
                    return bugId;
                }
            }
        }
        return null;
    }
}
