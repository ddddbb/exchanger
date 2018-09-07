package wanglin.exchanger.framework;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

import java.util.List;

public class ExchangerUrlMapping extends AbstractUrlHandlerMapping {

    public void initApplicationContext() throws BeansException {
        super.initApplicationContext();
        this.registerUrlHandlers(this.getApplicationContext().getBean(Configuration.class).getServers());
    }

    private void registerUrlHandlers(List<Exchanger> servers) {
        servers.forEach(exchanger -> {
            super.registerHandler(exchanger.url, exchanger.code);
        });
    }
}
