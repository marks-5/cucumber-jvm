package io.cucumber.core.model;

import io.cucumber.core.io.Resource;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureBuilderTest {

    @Test
    public void ignores_duplicate_features() throws IOException {
        FeatureBuilder builder = new FeatureBuilder();
        String featurePath = "foo.feature";
        Resource resource1 = createResourceMock(featurePath);
        Resource resource2 = createResourceMock(featurePath);

        builder.parse(resource1);
        builder.parse(resource2);

        List<CucumberFeature> features = builder.build();

        assertEquals(1, features.size());
    }

    @Test
    public void works_when_path_and_uri_are_the_same() throws IOException {
        String featurePath = "path/foo.feature";
        Resource resource = createResourceMock(featurePath);
        FeatureBuilder builder = new FeatureBuilder();

        builder.parse(resource);

        List<CucumberFeature> features = builder.build();

        assertEquals(1, features.size());
        assertEquals(featurePath, features.get(0).getUri());
    }

    private Resource createResourceMock(String featurePath) throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getPath()).thenReturn(featurePath);
        ByteArrayInputStream feature = new ByteArrayInputStream("Feature: foo".getBytes(UTF_8));
        when(resource.getInputStream()).thenReturn(feature);
        return resource;
    }

}
