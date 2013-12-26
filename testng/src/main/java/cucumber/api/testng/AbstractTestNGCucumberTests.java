package cucumber.api.testng;

import cucumber.runtime.model.CucumberFeature;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs cucumber every detected feature as separated test
 */
public abstract class AbstractTestNGCucumberTests {
    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() throws Exception {
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Feature", dataProvider = "features")
    public void feature(CucumberFeatureWrapper cucumberFeature) {
        testNGCucumberRunner.runCucumber(cucumberFeature.getCucumberFeature());
    }

    /**
     * @return returns two dimensional array of {@link CucumberFeatureWrapper} objects.
     */
    @DataProvider
    public Object[][] features() {
        List<CucumberFeature> features = testNGCucumberRunner.getFeatures();
        List<Object[]> featuresList = new ArrayList<Object[]>(features.size());
        for (CucumberFeature feature : features) {
            featuresList.add(new Object[]{new CucumberFeatureWrapper(feature)});
        }
        return featuresList.toArray(new Object[][]{});
    }

}
