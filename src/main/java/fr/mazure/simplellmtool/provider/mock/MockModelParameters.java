package fr.mazure.simplellmtool.provider.mock;

import java.util.Optional;

import fr.mazure.simplellmtool.provider.base.ModelParameters;

/**
 * Parameters for the mock model provider.
 */
public class MockModelParameters extends ModelParameters {

    public MockModelParameters() {
        super("mock", Optional.empty(), null);
    }
}
