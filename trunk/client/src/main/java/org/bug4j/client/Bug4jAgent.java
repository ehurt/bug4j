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

package org.bug4j.client;

import org.bug4j.common.FullStackHashCalculator;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reports exceptions to the bug4j server.
 * Exceptions are processed by a background thread which is started by {@link Bug4jStarter}.
 * The Bug4jAgent will be started automatically with the first exception report.
 */
public class Bug4jAgent {
    private static final BlockingQueue<ReportableEvent> _queue = new LinkedBlockingQueue<ReportableEvent>();
    private static final ReportableEvent STOP = new ReportableEvent("stop", new String[0], "stop");
    private static Thread _clientThread;
    private static int _reported;
    private final boolean _anonymousReports;
    private final ReportLRU _reportLRU = new ReportLRU();

    private HttpConnector _connector;
    private boolean _report = true;
    private Long _sessionId;

    private Bug4jAgent(boolean anonymousReports) {
        _anonymousReports = anonymousReports;
    }

    static synchronized void start(Settings settings) {
        if (!isStarted()) {
            _reported = 0;

            final boolean anonymousReports = settings.isAnonymousReports();
            final Bug4jAgent client = new Bug4jAgent(anonymousReports);

            final String serverUrl = settings.getServerUrl();
            final String applicationName = settings.getApplicationName();
            final String applicationVersion = settings.getApplicationVersion();
            final HttpConnector connector = new HttpConnector(serverUrl, applicationName, applicationVersion);
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
            thread.setName("bug4j");
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

    private static boolean isStarted() {
        return _clientThread != null;
    }

    /**
     * Shuts down the bug4j thread.
     */
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

    private void createSession() {
        _sessionId = _connector.createSession();
    }

    private void processSafely(ReportableEvent reportableEvent) {
        if (_sessionId == null) {
            createSession();
        }
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
        final String[] throwableStrRep = reportableEvent.getThrowableStrRep();
        final String textHash = FullStackHashCalculator.getTextHash(Arrays.asList(throwableStrRep));
        if (textHash != null) {
            if (_reportLRU.put(textHash)) { // Refrain from sending the same eror
                final String message = reportableEvent.getMessage();
                final String user = _anonymousReports ? "anonymous" : reportableEvent.getUser();
                final boolean isNew = _connector.reportHit(_sessionId, message, user, textHash);
                if (isNew) {
                    _connector.reportBug(
                            _sessionId,
                            message,
                            user,
                            reportableEvent.getThrowableStrRep()
                    );
                }
                _reported++;
            }
        }
    }

    private void setConnector(HttpConnector connector) {
        _connector = connector;
    }

    /**
     * Reports an exception to the server
     *
     * @param message   An error message
     * @param throwable the exception to report
     */
    public static void report(@Nullable String message, Throwable throwable) {
        final ReportableEvent reportableEvent = ReportableEvent.createReportableEvent(message, throwable);
        enqueue(reportableEvent);
    }

    /**
     * Reports an exception to the server
     *
     * @param throwable the exception to report
     */
    public static void report(Throwable throwable) {
        report(null, throwable);
    }

    static void enqueue(ReportableEvent reportableEvent) {
        if (!isStarted()) {
            new Bug4jStarter().start();
        }
        _queue.add(reportableEvent);
    }

    public static int getReported() {
        return _reported;
    }
}
