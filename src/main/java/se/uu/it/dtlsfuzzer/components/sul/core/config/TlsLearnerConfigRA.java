package se.uu.it.dtlsfuzzer.components.sul.core.config;

import io.github.protocolfuzzing.protocolstatefuzzer.components.learner.config.LearnerConfigRA;

/**
 * RA learning config for (D)TLS which adjusts default parameters.
 */
public class TlsLearnerConfigRA extends LearnerConfigRA {

    public TlsLearnerConfigRA() {
        super();
        this.seedTransitions = Boolean.TRUE;
        this.probNewDataValue = 0.5;
        this.maxDepthRA = 10;
    }
}
