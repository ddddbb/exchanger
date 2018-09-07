package wanglin.exchanger.framework;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import wanglin.exchanger.ChannelFacade;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ChannelFacadeImpl implements ChannelFacade, InitializingBean {
    @Autowired
    Configuration     configuration;
    @Autowired
    DefaultMQProducer producer;

    @Override
    public Map<String, Object> sync(String code, Object args) throws IOException {
        Assert.notNull(code, "渠道码不能为空");
        Exchanger exchanger = configuration.getExchanger(code);
        Assert.notNull(exchanger, "无此渠道定义");
        Connection                  connection   = configuration.getProtocolFactory(exchanger.protocol).get(exchanger);
        Assemparer                  assemparer   = configuration.getAssemparer(exchanger);
        Future<Map<String, Object>> resultFuture = connection.send(exchanger, args, assemparer);
        try {
            return resultFuture.get(exchanger.getTimeout(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void async(String code, Object args) throws IOException {
        Assert.notNull(code, "渠道码不能为空");
        Exchanger exchanger = configuration.getExchanger(code);
        Assert.notNull(exchanger, "无此渠道定义");
        Connection connection = configuration.getProtocolFactory(exchanger.protocol).get(exchanger);
        Assemparer assemparer = configuration.getAssemparer(exchanger);
        connection.send(exchanger, args, assemparer);
    }

    @Override
    public void notify(String code, Object args) {
        Assert.notNull(code, "渠道码不能为空");
        Exchanger exchanger = configuration.getExchanger(code);
        Assert.notNull(exchanger, "无此渠道定义");
        Assemparer assemparer = configuration.getAssemparer(exchanger);

        try {
            Object     result     = assemparer.parse(exchanger, args);
            producer.send(new Message(ChannelFacade.NOTIFY_MQ, code, JSON.toJSONString(result).getBytes()));
            log.info("渠道{}收到回调消息{}", exchanger.channel, result);
        } catch (ParserException e) {
//            log.error("",e);
            e.printStackTrace();
        }catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void afterPropertiesSet() throws Exception {
        List<Exchanger> exchangers = configuration.getServers();
        exchangers.forEach(exchanger -> {
            export(exchanger);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.producer.shutdown();
        }));
    }

    private void export(Exchanger exchanger) {
        configuration.getProtocolFactory(exchanger.protocol).export(exchanger);
    }
}
