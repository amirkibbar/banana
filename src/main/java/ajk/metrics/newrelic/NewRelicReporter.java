package ajk.metrics.newrelic;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import static com.newrelic.api.agent.NewRelic.recordMetric;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A reporter class for sending metrics to New Relic.
 *
 * @see <a href="http://newrelic.com/">http://newrelic.com/</a>
 * @see <a href="http://metrics.dropwizard.io/3.1.0/">http://metrics.dropwizard.io/3.1.0/</a>
 *
 */
public class NewRelicReporter extends ScheduledReporter {
    /**
     * Returns a new {@link Builder} for {@link NewRelicReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link NewRelicReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link NewRelicReporter} instances. Defaults to converting rates to events/second, converting
     * durations to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.filter = MetricFilter.ALL;
            this.rateUnit = SECONDS;
            this.durationUnit = MILLISECONDS;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link NewRelicReporter} with the given properties.
         *
         * @return a {@link NewRelicReporter}
         */
        public NewRelicReporter build() {
            return new NewRelicReporter(registry, filter, rateUnit, durationUnit);
        }
    }

    private NewRelicReporter(MetricRegistry registry, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, "newRelic-reporter", filter, rateUnit, durationUnit);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            recordMetric("Custom/app-metrics/" + entry.getKey(), ((Double) entry.getValue().getValue()).floatValue());
        }

        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            recordMetric("Custom/app-metrics/" + entry.getKey(), entry.getValue().getCount());
        }

        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            Histogram histogram = entry.getValue();
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".count", histogram.getCount());
            Snapshot snapshot = histogram.getSnapshot();
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".min", snapshot.getMin());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".max", snapshot.getMax());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".mean", Double.valueOf(snapshot.getMean()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".stddev", Double.valueOf(snapshot.getStdDev()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".median", Double.valueOf(snapshot.getMedian()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p75", Double.valueOf(snapshot.get75thPercentile()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p95", Double.valueOf(snapshot.get95thPercentile()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p98", Double.valueOf(snapshot.get98thPercentile()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p99", Double.valueOf(snapshot.get99thPercentile()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p999", Double.valueOf(snapshot.get999thPercentile()).floatValue());
        }

        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            Meter meter = entry.getValue();
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".count", meter.getCount());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".mean-rate", Double.valueOf(convertRate(meter.getMeanRate())).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".one-minute-rate", Double.valueOf(convertRate(meter.getOneMinuteRate())).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".five-minute-rate", Double.valueOf(convertRate(meter.getFiveMinuteRate())).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".fifteen-minute-rate", Double.valueOf(convertRate(meter.getFifteenMinuteRate())).floatValue());
        }

        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            Timer timer = entry.getValue();
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".count", timer.getCount());
            Snapshot snapshot = timer.getSnapshot();
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".min", snapshot.getMin());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".max", snapshot.getMax());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".mean", Double.valueOf(snapshot.getMean()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".stddev", Double.valueOf(snapshot.getStdDev()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".median", Double.valueOf(snapshot.getMedian()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p75", Double.valueOf(snapshot.get75thPercentile()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p95", Double.valueOf(snapshot.get95thPercentile()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p98", Double.valueOf(snapshot.get98thPercentile()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p99", Double.valueOf(snapshot.get99thPercentile()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".p999", Double.valueOf(snapshot.get999thPercentile()).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".mean-rate", Double.valueOf(convertRate(timer.getMeanRate())).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".one-minute-rate", Double.valueOf(convertRate(timer.getOneMinuteRate())).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".five-minute-rate", Double.valueOf(convertRate(timer.getFiveMinuteRate())).floatValue());
            recordMetric("Custom/app-metrics/" + entry.getKey() + ".fifteen-minute-rate", Double.valueOf(convertRate(timer.getFifteenMinuteRate())).floatValue());
        }
    }
}
