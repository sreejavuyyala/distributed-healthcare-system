package com.healthcare.platform.ingestion;

import com.healthcare.platform.config.PlatformProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailureSimulatorTest {

    private FailureSimulator withFailureRate(double rate) {
        var ingestion = new PlatformProperties.Ingestion("feeds", rate, 3, 10, 2.0);
        var properties = new PlatformProperties(null, ingestion, null);
        return new FailureSimulator(properties);
    }

    @Test
    void zeroFailureRateNeverInjectsFailure() {
        FailureSimulator simulator = withFailureRate(0.0);
        for (int i = 1; i <= 50; i++) {
            simulator.maybeInjectFailure(FeedName.PATIENTS, i); // should never throw
        }
    }

    @Test
    void fullFailureRateAlwaysInjectsFailure() {
        FailureSimulator simulator = withFailureRate(1.0);
        assertThatThrownBy(() -> simulator.maybeInjectFailure(FeedName.LABS, 1))
                .isInstanceOf(SimulatedTransientFailureException.class);
    }

    @Test
    void forcedFailureConsumesExactlyConfiguredAttempts() {
        FailureSimulator simulator = withFailureRate(0.0);
        simulator.forceFailure(FeedName.ENCOUNTERS, 2);

        assertThat(simulator.hasForcedFailure(FeedName.ENCOUNTERS)).isTrue();
        assertThatThrownBy(() -> simulator.maybeInjectFailure(FeedName.ENCOUNTERS, 1))
                .isInstanceOf(SimulatedTransientFailureException.class);
        assertThatThrownBy(() -> simulator.maybeInjectFailure(FeedName.ENCOUNTERS, 2))
                .isInstanceOf(SimulatedTransientFailureException.class);

        // Third attempt: forced failures exhausted, should succeed (no throw).
        assertThat(simulator.hasForcedFailure(FeedName.ENCOUNTERS)).isFalse();
        simulator.maybeInjectFailure(FeedName.ENCOUNTERS, 3);
    }

    @Test
    void forcedFailureOnOneFeedDoesNotAffectAnother() {
        FailureSimulator simulator = withFailureRate(0.0);
        simulator.forceFailure(FeedName.ENCOUNTERS, 5);

        assertThat(simulator.hasForcedFailure(FeedName.PATIENTS)).isFalse();
        simulator.maybeInjectFailure(FeedName.PATIENTS, 1); // must not throw
    }

    @Test
    void clearForcedFailureRemovesIt() {
        FailureSimulator simulator = withFailureRate(0.0);
        simulator.forceFailure(FeedName.ENCOUNTERS, 3);
        simulator.clearForcedFailure(FeedName.ENCOUNTERS);

        assertThat(simulator.hasForcedFailure(FeedName.ENCOUNTERS)).isFalse();
        simulator.maybeInjectFailure(FeedName.ENCOUNTERS, 1); // must not throw
    }
}