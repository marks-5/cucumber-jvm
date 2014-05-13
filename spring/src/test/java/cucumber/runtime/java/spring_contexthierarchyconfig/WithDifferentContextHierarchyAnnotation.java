package cucumber.runtime.java.spring_contexthierarchyconfig;

import cucumber.runtime.java.spring.DummyComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;

@ContextHierarchy(@ContextConfiguration("classpath:cucumber2.xml"))
public class WithDifferentContextHierarchyAnnotation {

    private boolean autowired;

    @Autowired
    public void setAutowiredCollaborator(DummyComponent collaborator) {
        autowired = true;
    }

    public boolean isAutowired() {
        return autowired;
    }

}
