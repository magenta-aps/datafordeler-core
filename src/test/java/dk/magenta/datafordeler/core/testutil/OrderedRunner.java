package dk.magenta.datafordeler.core.testutil;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Cribbed from http://stackoverflow.com/a/40211661
 */
public class OrderedRunner extends SpringJUnit4ClassRunner {

    public OrderedRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> list = super.computeTestMethods();
        List<FrameworkMethod> copy = new ArrayList<FrameworkMethod>(list);
        Collections.sort(copy, new Comparator<FrameworkMethod>() {

            @Override
            public int compare(FrameworkMethod f1, FrameworkMethod f2) {
                Order o1 = f1.getAnnotation(Order.class);
                Order o2 = f2.getAnnotation(Order.class);

                if (o1 == null || o2 == null) {
                    return -1;
                }

                return o1.order() - o2.order();
            }
        });
        return copy;
    }
}