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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private static final BlockingQueue<ReportableEvent> _queue = new LinkedBlockingQueue<ReportableEvent>();
    private static final ReportableEvent STOP = new ReportableEvent(null, null, null);
    private static Thread _clientThread;
    private static int _reported;

    private final StackAnalyzer _stackAnalyzer = new StackAnalyzer();
    private HttpConnector _connector;
    private boolean _report = true;
    private boolean _isInitialized;

    private Client() {
    }

    public static synchronized void start() {
        start(null);
    }

    public static synchronized void start(@Nullable Settings settings) {
        if (_clientThread == null) {
            _reported = 0;

            if (settings == null) {
                settings = Settings.getDefaultInstance();
            }

            final Client client = new Client();
            final HttpConnector connector = new HttpConnector(settings);
            client.setConnector(connector);
            final Object tellMeWhenYouAreReady = new Object();
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    synchronized (tellMeWhenYouAreReady) {
                        tellMeWhenYouAreReady.notify();
                    }

                    client.run();
                }
            };
            thread.setName("Bug4J");
            thread.setDaemon(true);
            thread.start();
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (tellMeWhenYouAreReady) {
                try {
                    tellMeWhenYouAreReady.wait();
                    _clientThread = thread;
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public synchronized static void shutdown() {
        if (_clientThread != null) {
            enqueue(STOP);
            try {
                _clientThread.join();
                _clientThread = null;
            } catch (InterruptedException e) {
                //
            }
        }
    }

    private void run() {
        while (true) {
            final ReportableEvent reportableEvent = getNextReportableEvent();
            if (reportableEvent == null) {
                break;
            }
            processSafely(reportableEvent);
        }
    }

    private void processSafely(ReportableEvent reportableEvent) {
        if (_report) {
            try {
                process(reportableEvent);
            } catch (Throwable e) {
                _report = false;
            }
        }
    }

    private ReportableEvent getNextReportableEvent() {
        ReportableEvent ret = null;
        try {
            final ReportableEvent top = _queue.take();
            if (top != STOP) {
                ret = top;
            }
        } catch (InterruptedException e) {
            // ret stays null
        }
        return ret;
    }

    private void process(ReportableEvent reportableEvent) {
        lazyInitialize();
        final String title = getTitle(reportableEvent);
        if (title != null) {
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
            _reported++;
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

    public void setConnector(HttpConnector connector) {
        _connector = connector;
    }

    public static void report(String message, Throwable throwable) {
        final ReportableEvent reportableEvent = ReportableEvent.createReportableEvent(message, throwable);
        enqueue(reportableEvent);
    }

    public static void enqueue(ReportableEvent reportableEvent) {
        start();
        _queue.add(reportableEvent);
    }

    public static int getReported() {
        return _reported;
    }
}
