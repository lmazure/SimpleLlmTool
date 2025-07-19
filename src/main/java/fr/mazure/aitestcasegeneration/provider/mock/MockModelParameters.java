package fr.mazure.aitestcasegeneration.provider.mock;

import java.util.Optional;

import fr.mazure.aitestcasegeneration.provider.base.ModelParameters;

/**
 * Parameters for the mock model provider.
 */
public class MockModelParameters extends ModelParameters {

    public MockModelParameters() {
        super("mock", Optional.empty(), null);
    }
}
