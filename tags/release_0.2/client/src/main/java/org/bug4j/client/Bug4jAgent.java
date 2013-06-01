/*
 * Copyright 2013 Cedric Dandoy
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

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reports exceptions to the bug4j server.
 * Exceptions are processed by a background thread which is started by {@link Bug4jStarter}.
 * The Bug4jAgent will be started automatically with the first exception report.
 */
public class Bug4jAgent {
    /**
     * Maximum number of reports to enqueue when the server is not responding.
     */
    private static final int MAX_ENQUEUE = 200;

    private static final BlockingQueue<ReportableEvent> _queue = new LinkedBlockingQueue<ReportableEvent>();
    private static final ReportableEvent STOP = new ReportableEvent("stop", new String[0], "stop");
    private static Thread _clientThread;
    private static int _reported;
    private final ReportLRU _reportLRU = new ReportLRU();

    private HttpConnector _connector;
    private static boolean _isStarted;
    private boolean _isInitialized;
    private final Settings _settings;

    private Bug4jAgent(Settings settings) {
        _settings = settings;
    }

    static void start(Settings settings) {

        synchronized (Bug4jAgent.class) {
            if (_isStarted) {
                return;
            }
            _isStarted = true;
        }

        addShutdownHook();
        _isStarted = true;
        _reported = 0;

        final Bug4jAgent client = new Bug4jAgent(settings);

        _clientThread = new Thread() {
            @Override
            public void run() {
                try {
                    client.run();
                } catch (InterruptedException ignored) {
                }
            }
        };
        _clientThread.setName("bug4j");
        _clientThread.setDaemon(true);
        _clientThread.start();
    }

    private void init() {
        if (!_isInitialized) {
            _isInitialized = true;
            final String serverUrl = _settings.getServerUrl();
            final String applicationName = _settings.getApplicationName();
            final String applicationVersion = _settings.getApplicationVersion();
            final long buildDate = _settings.getBuildDate();
            final boolean devBuild = _settings.isDevBuild();
            final Integer buildNumber = _settings.getBuildNumber();
            final String proxyHost = _settings.getProxyHost();
            final int proxyPort = _settings.getProxyPort();
            try {
                _connector = HttpConnector.createHttpConnector(
                        serverUrl, proxyHost, proxyPort,
                        applicationName, applicationVersion,
                        buildDate, devBuild, buildNumber);
            } catch (Exception e) {
                SimpleLogger.error("Failed to initialize Bug4j: " + e.getMessage());
            }
        }
    }

    /**
     * Shuts down the bug4j thread.
     */
    public static void shutdown() {

        // if the client thread has not been started yet then there is nothing to stop
        // but the client may be starting up => sync.
        synchronized (Bug4jAgent.class) {
            if (!_isStarted) {
                return;
            }
        }

        enqueue(STOP);
        try {
            _clientThread.join(2000);
            _clientThread = null;
        } catch (InterruptedException ignored) {
        } finally {
            _isStarted = false;
        }
        _queue.clear();
    }

    private void run() throws InterruptedException {
        boolean isServerReceiving = true;
        while (true) {
            final ReportableEvent reportableEvent = getNextReportableEvent();
            if (reportableEvent == null) {
                break;
            }
            if (isServerReceiving) {
                isServerReceiving = processSafely(reportableEvent);
            }
        }
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("bug4j-shutdown") {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    private boolean processSafely(ReportableEvent reportableEvent) {
        boolean ret = false;
        try {
            init();
            process(reportableEvent);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
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
        if (_connector != null) {
            final String textHash = hash(reportableEvent);
            if (_reportLRU.put(textHash)) { // Refrain from sending the same eror
                final String message = reportableEvent.getMessage();
                final String user = reportableEvent.getUser();
                final boolean isNew = _connector.reportHit(message, user, textHash);
                if (isNew) {
                    final String[] throwableStrRep = reportableEvent.getThrowableStrRep();
                    _connector.reportBug(
                            message,
                            user,
                            throwableStrRep
                    );
                }
                _reported++;
            }
        }
    }

    private static String hash(ReportableEvent reportableEvent) {
        final String ret;
        final String[] throwableStrRep = reportableEvent.getThrowableStrRep();
        final List<String> hashable = throwableStrRep == null
                                      ? Collections.singletonList(reportableEvent.getMessage())
                                      : Arrays.asList(throwableStrRep);
        ret = FullStackHashCalculator.getTextHash(hashable);
        return ret;
    }

    /**
     * Reports an exception to the server
     *
     * @param message   An error message
     * @param throwable the exception to report
     */
    public static void report(@Nullable String message, @Nullable Throwable throwable) {
        if (message != null || throwable != null) { // do not allow both to be null. We wouldn't have much to do
            final String[] throwableStrRep = TextUtils.createStringRepresentation(throwable);
            final ReportableEvent reportableEvent = ReportableEvent.createReportableEvent(message, throwableStrRep);
            enqueue(reportableEvent);
        }
    }

    /**
     * Reports an exception to the server
     *
     * @param message         An error message
     * @param throwableStrRep the exception to report
     */
    static void report(@Nullable String message, @Nullable String[] throwableStrRep) {
        if (message != null || throwableStrRep != null) { // do not allow both to be null. We wouldn't have much to do
            final ReportableEvent reportableEvent = ReportableEvent.createReportableEvent(message, throwableStrRep);
            enqueue(reportableEvent);
        }
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
        if (_queue.size() < MAX_ENQUEUE) {
            if (!_isStarted) {
                new Bug4jStarter().start();
            }
            _queue.add(reportableEvent);
        }
    }

    public static int getReported() {
        return _reported;
    }
}
