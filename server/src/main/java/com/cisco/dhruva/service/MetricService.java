/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.service;

import static com.cisco.dhruva.util.log.event.Event.DIRECTION.OUT;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.common.metric.Metric;
import com.cisco.dhruva.common.metric.MetricClient;
import com.cisco.dhruva.common.metric.Metrics;
import com.cisco.dhruva.common.metric.Metrics.MetricType;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.event.Event.DIRECTION;
import com.cisco.dhruva.util.log.event.Event.MESSAGE_TYPE;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetricService {

  private static final String DHRUVA = "dhruva";
  private final ScheduledThreadPoolExecutor scheduledExecutor;
  @Autowired
  private ExecutorService executorService;

  public MetricService() {
    executorService.startScheduledExecutorService(ExecutorType.METRIC_SERVICE, 4);
    scheduledExecutor = executorService.getScheduledExecutorThreadPool(ExecutorType.SIP_TIMER);
  }

  public void registerPeriodicMetric(String measurement, Supplier<Metric> metricSupplier,
      int interval, TimeUnit timeUnit) {
    scheduledExecutor
        .scheduleAtFixedRate(getMetricFromSupplier(measurement, metricSupplier), interval, interval,
            timeUnit);
  }

  @NotNull
  private Runnable getMetricFromSupplier(String measurement, Supplier<Metric> metricSupplier) {
    return () -> {

      Metric metric = metricSupplier.get();
      if (metric != null) {
        metric.measurement(prefixDhruvaToMeasurementName(measurement));
        sendMetric(metric);
      }

    };
  }

  @Autowired
  MetricClient metricClient;

  public void sendConnectionMetric(
      String localIp,
      int localPort,
      String remoteIp,
      int remotePort,
      Transport transport,
      DIRECTION direction,
      Connection.STATE connectionState) {

    Metric metric =
        Metrics.newMetric(MetricType.INFLUX)
            .measurement(prefixDhruvaToMeasurementName("Connection"))
            .tag("transport", transport.toString())
            .tag("direction", direction.name())
            .field("localIp", localIp)
            .field("localPort", localPort)
            .field("remoteIp", remoteIp)
            .field("remotePort", remotePort);

    sendMetric(metric);
  }

  public void sendSipMessageMetric(
      String method,
      String callId,
      String cseq,
      MESSAGE_TYPE messageType,
      Transport transport,
      DIRECTION direction,
      boolean isMidCall,
      boolean isInternallyGenerated,
      long dhruvaProcessingDelayInMillis) {

    Metric metric =
        Metrics.newMetric(MetricType.INFLUX)
            .measurement(prefixDhruvaToMeasurementName("SipMessage"))
            .tag("method", method)
            .tag("messageType", messageType.name())
            .tag("direction", direction.name())
            .field("callId", callId)
            .field("isMidCall", isMidCall)
            .field("isInternallyGenerated", isInternallyGenerated)
            .field("cseq", cseq);
    if (direction == OUT && !isInternallyGenerated) {
      metric.field("dhruvaProcessingDelayInMillis", dhruvaProcessingDelayInMillis);
    }

    sendMetric(metric);
  }

  @NotNull
  private String prefixDhruvaToMeasurementName(String measurement) {
    return DHRUVA + measurement;
  }

  private void sendMetric(Metric metric) {
    metricClient.sendMetric(metric);
  }
}
