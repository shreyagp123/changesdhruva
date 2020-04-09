APPLICATION_NAME: '{{ .Dhruva.deploy.APPLICATION_NAME }}'
sipListenPoints: '{{ .Dhruva.config.sipListenPoints }}'
MetricsNamespacePrefix: '{{ .Dhruva.config.MetricsNamespacePrefix }}'
MetricsPort: '{{ .Dhruva.config.MetricsPort }}'
aggrMetricsKafkaAddressPropertyName: '{{ .Dhruva.config.aggrMetricsKafkaAddressPropertyName }}'
aggrMetricsKafkaTopicName: '{{ .Dhruva.config.aggrMetricsKafkaTopicName }}'
aggrMetricsViaKafkaEnabled: '{{ .Dhruva.config.aggrMetricsViaKafkaEnabled }}'
dataCenterIdentifier: '{{ .Dhruva.config.dataCenterIdentifier }}'
domain: '{{ .Dhruva.config.domain }}'
environment: '{{ .Dhruva.config.environment }}'
kafkaBootstrapServers: '{{ .Dhruva.config.kafkaBootstrapServers }}'
kafkaDiagHostPortAddress: '{{ .Dhruva.config.kafkaDiagHostPortAddress }}'
kafkaHostPortAddress: '{{ .Dhruva.config.kafkaHostPortAddress }}'
kafkaMeetHostPortAddress: '{{ .Dhruva.config.kafkaMeetHostPortAddress }}'
metricsKafkaClusterConfig: '{{ .Dhruva.config.metricsKafkaClusterConfig }}'
metricsKafkaHostPortAddress: '{{ .Dhruva.config.metricsKafkaHostPortAddress }}'
metricsPublicUrl: '{{ .Dhruva.config.metricsPublicUrl }}'
metricsSiteUrl: '{{ .Dhruva.config.metricsSiteUrl }}'
metricsUrl: '{{ .Dhruva.config.metricsUrl }}'
metrics_environment: '{{ .Dhruva.config.metrics_environment }}'
elasticache_dhruva_sslEnabled: '{{ .Dhruva.config.elasticache_dhruva_sslEnabled }}'
elasticache_dhruva_startTlsEnabled: '{{ .Dhruva.config.elasticache_dhruva_startTlsEnabled }}'
elasticache_dhruva_host: '{{ .Dhruva.config.elasticache_dhruva_host }}'
elasticache_dhruva_commandTimeoutMillis: '{{ .Dhruva.config.elasticache_dhruva_commandTimeoutMillis }}'
CATALINA_OPTS: '{{ .Dhruva.deploy.CATALINA_OPTS }}'
JAVA_OPTS: '{{ .Dhruva.deploy.JAVA_OPTS }}'
THIS_SHOULD_FAIL=true