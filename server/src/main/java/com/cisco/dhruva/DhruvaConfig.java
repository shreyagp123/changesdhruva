package com.cisco.dhruva;

import com.cisco.dhruva.common.dns.*;
import com.cisco.dhruva.common.dns.metrics.DnsReporter;
import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.metric.InfluxClient;
import com.cisco.dhruva.common.metric.MetricClient;
import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.service.SipServerLocatorService;
import com.ciscospark.server.Wx2ConfigAdapter;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConditionalOnWebApplication
public class DhruvaConfig extends Wx2ConfigAdapter {

  @Autowired DhruvaSIPConfigProperties dhruvaSIPConfigProperties;

  private static final long DEFAULT_CACHE_TIMEOUT = 10;

  @Override
  public String getServiceName() {
    return "Dhruva";
  }

  @Override
  public String getMetricsNamespace() {
    return "dhruva";
  }

  @Override
  public String getUserAgent() {
    return "Dhruva/1.0";
  }

  @Bean
  public ExecutorService getExecutorService() {
    return new ExecutorService("DhruvaSipServer");
  }

  @Bean
  public MetricClient getMetricClient() {
    return new InfluxClient();
  }

  @Bean
  public Integer defaultCacheTimeout() {
    return (int) TimeUnit.MINUTES.toSeconds(DEFAULT_CACHE_TIMEOUT);
  }

  @Bean
  @Lazy
  public DnsInjectionService dnsInjectionService() {
    // TODO check for redis
    return DnsInjectionService.memoryBackedCache();
  }

  @Bean
  public DnsReporter dnsReporter() {
    return new DnsMetricsReporter();
  }

  @Bean
  public SipServerLocatorService sipServerLocatorService() {
    return new SipServerLocatorService(dhruvaSIPConfigProperties);
  }

  @Bean
  public DnsLookup dnsLookup() {
    return DnsResolvers.newBuilder()
        .cacheSize(dhruvaSIPConfigProperties.getDhruvaDnsCacheMaxSize())
        .dnsLookupTimeoutMillis(dhruvaSIPConfigProperties.dnsCacheRetentionTimeMillis())
        .retentionDurationMillis(dhruvaSIPConfigProperties.dnsCacheRetentionTimeMillis())
        .metered(new DnsMetricsReporter())
        .build();
  }
}
