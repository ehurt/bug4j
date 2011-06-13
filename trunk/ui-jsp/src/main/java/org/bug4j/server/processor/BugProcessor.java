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
import org.bug4j.server.gwt.client.data.Stack;
import org.bug4j.server.gwt.client.data.Strain;
import org.bug4j.server.gwt.client.util.TextToLines;
import org.bug4j.server.store.Store;
import org.bug4j.server.store.StoreFactory;

import java.util.List;

public final class BugProcessor {
    private BugProcessor() {
    }

    public static void process(String app, String version, String stackText) {
        final Store store = StoreFactory.getStore();
        final List<String> stackLines = TextToLines.toList(stackText);

        final String fullHash = FullStackHashCalculator.getTextHash(stackLines);

        Stack stack = store.getStackByHash(app, fullHash);
        if (stack == null) {
            final List<String> appPackages = store.getPackages(app);
            final StackAnalyzer stackAnalyzer = new StackAnalyzer();
            stackAnalyzer.setApplicationPackages(appPackages);
            final String title = stackAnalyzer.analyze(stackLines);

            final String strainHash = StackPathHashCalculator.analyze(stackLines);
            Strain strain = store.getStrainByHash(app, strainHash);
            if (strain == null) {
                Long bugId = store.getBugIdByTitle(app, title);
                if (bugId == null) {
                    bugId = store.createBug(app, title);
                }
                strain = store.createStrain(app, bugId, strainHash);
            }
            stack = store.createStack(app, strain.getBugId(), strain.getStrainId(), fullHash, stackText);
        }
        store.reportHitOnStack(app, version, stack);
    }
}
