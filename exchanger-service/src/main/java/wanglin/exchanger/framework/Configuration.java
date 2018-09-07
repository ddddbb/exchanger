package wanglin.exchanger.framework;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import wanglin.exchanger.framework.protocol.HttpFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class Configuration implements ApplicationContextAware, InitializingBean {
    ConcurrentMap<String, Exchanger> exchangers  = new ConcurrentHashMap<>();
    HttpFactory                      httpFactory = new HttpFactory();

    private ApplicationContext applicationContext;

    public Configuration() {
    }

    public Configuration(Map<String, Exchanger> map) {
        exchangers.putAll(map);
    }

    public Exchanger getExchanger(String code) {
        return exchangers.get(code);
    }

    public Assemparer getAssemparer(Exchanger exchanger) {
        Assemparer bean = applicationContext.getBean(exchanger.assemparer, Assemparer.class);
        return bean;
    }


    public ProtocolFactory getProtocolFactory(ProtocolEnum protocol) {
        switch (protocol) {
            case HTTP:
                return httpFactory;
            case HTTPS:
                return httpFactory;
            default:
                return httpFactory;
        }
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        //INIT
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public List<Exchanger> getServers() {
        return exchangers.values().stream().filter(t -> t.model == ModelEnum.SERVER).collect(Collectors.toList());
    }
}
