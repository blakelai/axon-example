package com.example.axon;

import com.mongodb.MongoClient;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.eventhandling.scheduling.quartz.QuartzEventScheduler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.extensions.mongo.DefaultMongoTemplate;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.axonframework.extensions.mongo.eventhandling.saga.repository.MongoSagaStore;
import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine;
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudHttpBackupCommandRouter;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.eventsourcing.SpringAggregateSnapshotter;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AxonConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AxonConfiguration.class);

    @Autowired
    public void configure(EventProcessingConfigurer config) {
        config.registerListenerInvocationErrorHandler("projections", conf -> PropagatingErrorHandler.instance());
    }

    // The Event store `EmbeddedEventStore` delegates actual storage and retrieval of events to an `EventStorageEngine`.
    @Bean
    public EmbeddedEventStore eventStore(EventStorageEngine storageEngine, org.axonframework.spring.config.AxonConfiguration configuration) {
        return EmbeddedEventStore.builder()
                .storageEngine(storageEngine)
                .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                .build();
    }

    // The `MongoEventStorageEngine` stores each event in a separate MongoDB document
    @Bean
    public EventStorageEngine storageEngine(MongoClient client) {
        return MongoEventStorageEngine.builder().mongoTemplate(DefaultMongoTemplate.builder().mongoDatabase(client).build()).build();
    }

    @Bean
    public Snapshotter snapShotter(EventStore eventStore) {
        return SpringAggregateSnapshotter.builder()
                .eventStore(eventStore)
                .build();
    }

    @Bean
    public SnapshotTriggerDefinition snapshotTriggerDefinition(Snapshotter snapShotter) {
        return new EventCountSnapshotTriggerDefinition(snapShotter, 1);
    }

    @Bean
    public MongoTemplate axonMongoTemplate(MongoClient client) {
        return DefaultMongoTemplate.builder()
                .mongoDatabase(client)
                .build();
    }

    @Bean
    public TokenStore tokenStore(MongoTemplate mongoTemplate, Serializer serializer) {
        return MongoTokenStore.builder()
                .mongoTemplate(mongoTemplate)
                .serializer(serializer)
                .build();
    }

    @Bean
    public CommandRouter springCloudHttpBackupCommandRouter(
            DiscoveryClient discoveryClient,
            Registration localServiceInstance,
            RestTemplate restTemplate,
            @Value("${axon.distributed.spring-cloud.fallback-url}")
                    String messageRoutingInformationEndpoint) {
        return SpringCloudHttpBackupCommandRouter.builder()
                .serviceInstanceFilter(s -> !s.getServiceId().equals("consul"))
                .discoveryClient(discoveryClient)
                .localServiceInstance(localServiceInstance)
                .routingStrategy(new AnnotationRoutingStrategy())
                .restTemplate(restTemplate)
                .messageRoutingInformationEndpoint(messageRoutingInformationEndpoint)
                .build();
    }

    @Bean
    public SagaStore sagaStore(MongoTemplate mongoTemplate, Serializer serializer) {
        return MongoSagaStore.builder()
                .mongoTemplate(mongoTemplate)
                .serializer(serializer)
                .build();
    }

    @Bean
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    @Bean
    public EventScheduler eventScheduler(Scheduler scheduler, EventBus eventBus) {
        return QuartzEventScheduler.builder()
                .scheduler(scheduler)
                .eventBus(eventBus)
                .build();
    }

}
