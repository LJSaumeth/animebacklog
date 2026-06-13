package dae.me.javafx;

import javafx.util.Callback;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringFxWeaver implements Callback<Class<?>, Object> {

    private final ApplicationContext applicationContext;

    public SpringFxWeaver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object call(Class<?> type) {
        return applicationContext.getBean(type);
    }
}
