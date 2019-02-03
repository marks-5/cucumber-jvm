package io.cucumber.core.model;

import java.net.URI;

/**
 * Identifies a single feature.
 * <p>
 * Features are identified by a URI as defined in {@link FeaturePath}.
 * Additionally the scheme specific part must end with {@code .feature}
 *
 * @see FeatureWithLines
 */
public class FeatureIdentifier {

    private FeatureIdentifier() {

    }

    public static URI parse(String featureIdentifier) {
        return parse(FeaturePath.parse(featureIdentifier));
    }

    public static URI parse(URI featureIdentifier) {
        String schemeSpecificPart = featureIdentifier.getSchemeSpecificPart();
        if (!schemeSpecificPart.endsWith(".feature")) {
            throw new IllegalArgumentException("featureIdentifier does not reference a single feature file: " + featureIdentifier);
        }
        return featureIdentifier;
    }

}