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

package org.dandoy.bug4j.client;

import org.dandoy.bug4j.common.StackAnalyzer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private static final BlockingQueue<ReportableEvent> _queue = new LinkedBlockingQueue<ReportableEvent>();
    private static final ReportableEvent STOP = new ReportableEvent(null, null, null);
    private static AtomicBoolean _isStarted = new AtomicBoolean();
    private static Thread _clientThread;
    private HttpConnector _connector;
    private StackAnalyzer _stackAnalyzer = new StackAnalyzer();
    private boolean _report = true;
    private boolean _isInitialized;

    private Client() {
    }

    static void start() {
        if (_isStarted.compareAndSet(false, true)) {
            final Settings settings = Settings.getInstance();

            final Client client = new Client();
            final HttpConnector connector = new HttpConnector(settings);
            client.setConnector(connector);

            final Thread thread = new Thread() {
                @Override
                public void run() {
                    _clientThread = Thread.currentThread();
                    client.run();
                }
            };
            thread.setName("Bug4J");
            thread.setDaemon(true);
            thread.start();
        }
    }

    static void shutdown() {
        if (_clientThread != null) {
            enqueue(STOP);
            try {
                _clientThread.join();
            } catch (InterruptedException e) {
                // 
            }
        }
    }

    private void run() {
        while (true) {
            final ReportableEvent reportableEvent;
            try {
                reportableEvent = _queue.take();
            } catch (InterruptedException e) {
                break;
            }
            if (reportableEvent == STOP) {
                break;
            }
            if (_report) {
                try {
                    process(reportableEvent);
                } catch (Throwable e) {
                    _report = false;
                }
            }
        }
    }

    private void process(ReportableEvent reportableEvent) {
        lazyInitialize();
        final String title = getTitle(reportableEvent);
        final String hash = getHash(title);
        final boolean isNew = _connector.reportHit(hash);
        if (isNew) {
            _connector.reportBug(
                    hash,
                    title,
                    reportableEvent.getMessage(),
                    reportableEvent.getExceptionMessage(),
                    reportableEvent.getThrowableStrRep()
            );
        }
    }

    private void lazyInitialize() {
        if (!_isInitialized) {
            final List<String> packages = _connector.getPackages();
            _isInitialized = true;
            _stackAnalyzer.setApplicationPackages(packages);
        }
    }

    private String getTitle(ReportableEvent reportableEvent) {
        final String[] throwableStrRep = reportableEvent.getThrowableStrRep();
        final List<String> stackLines = Arrays.asList(throwableStrRep);
        final String title = _stackAnalyzer.analyze(stackLines);
        return title;
    }

    private String getHash(String title) {
        final String ret;
        final int hash = Math.abs(title.hashCode());
        ret = Integer.toString(hash, 16);
        return ret;
    }

    public static void enqueue(ReportableEvent reportableEvent) {
        start();
        _queue.add(reportableEvent);
    }

    public void setConnector(HttpConnector connector) {
        _connector = connector;
    }
}
