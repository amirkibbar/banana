# metrics-newrelic-reporter

a New Relic metrics reporter (see: http://metrics.dropwizard.io/3.1.0/, http://newrelic.com/)

[![Build Status](https://travis-ci.org/amirkibbar/banana.svg?branch=master)](https://travis-ci.org/amirkibbar/banana)

[ ![Download](https://api.bintray.com/packages/amirk/maven/metrics-newrelic-reporter/images/download.svg) ](https://bintray.com/amirk/maven/metrics-newrelic-reporter/_latestVersion)

# Using the New Relic Metrics Reporter

Add the following to your gradle build:

```gradle

    repositories {
        maven { url "http://dl.bintray.com/amirk/maven" }
    }
    
    dependencies {
        compile "ajk.metrics.newrelic:metrics-newrelic-reporter:0.0.3"
    }
```

Define the reporter:

```java

    @Configuration
    public class MetricsConfig {
        @Bean
        public MetricRegistry metrics() {
            return new MetricRegistry();
        }
    
        @PostConstruct
        public void registerReporters() {
            NewRelicReporter newRelic = NewRelicReporter.forRegistry(metrics())
                    .build();
    
            newRelic.start(1, MINUTES);
        }
    }
```

This configuration will convert the rates into events per second, the durations into milliseconds and then report the
metrics collected by the metric registry to New Relic every minute.
 
You need to setup your New Relic agent in the JVM running this reporter, if the agent is not setup then the report will
simply not do anything.