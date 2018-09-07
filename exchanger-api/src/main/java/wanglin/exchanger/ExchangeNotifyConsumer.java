package wanglin.exchanger;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExchangeNotifyConsumer implements ApplicationContextAware {
    private String consumerGroup;
    private String nameServerAddress;
    private ApplicationContext applicationContext;

    public void init() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(this.consumerGroup);
        consumer.setNamesrvAddr(this.nameServerAddress);
        consumer.setConsumeMessageBatchMaxSize(1);
        consumer.setMessageModel(MessageModel.CLUSTERING);

        consumer.setVipChannelEnabled(false);
        String hostName = "";
        if (StringUtils.isNotEmpty(hostName)) {
            consumer.setInstanceName(hostName);
        }
        consumer.subscribe(ChannelFacade.NOTIFY_MQ, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

                return onMessage(getCode(msgs.get(0)), msgs.get(0));
            }


        });
        consumer.start();
        log.info("Consumer of {}, start.", this.consumerGroup);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            consumer.shutdown();
            log.info("Consumer of {}, shutdown.", this.consumerGroup);
        }));
    }

    private ConsumeConcurrentlyStatus onMessage(Object code, MessageExt messageExt) {
        log.info("收到渠道系统回调消息{}:{}",code,new String(messageExt.getBody()));
        try {
            ListenerMethod listenerMethod = getListenerMethod(code);
            if (listenerMethod != null) {
                listenerMethod.invoke(messageExt);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    private ListenerMethod getListenerMethod(Object code) {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ExchangeNotify.class);

        for (Map.Entry<String, Object> e : beans.entrySet()) {
            String beanName = e.getKey();
            Object bean = e.getValue();
            List<Method> methods = Lists.newArrayList(bean.getClass().getDeclaredMethods()).stream().filter(
                    m -> m.isAnnotationPresent(Listener.class)
                            && m.getAnnotation(Listener.class).code().equals(code)
            ).collect(Collectors.toList());
            if (methods.size() == 1) {
                return new ListenerMethod();
            }
        }

        return null;
    }


    private String getCode(MessageExt messageExt) {
        return messageExt.getKeys();
    }


    class ListenerMethod {
        Object listenerBean;
        Method method;

        public void invoke(MessageExt messageExt) throws InvocationTargetException, IllegalAccessException {
            method.invoke(listenerBean, messageExt);
        }
    }

    public void setConsumerGroup(String group) {
        this.consumerGroup = group;
    }

    public void setNameServerAddress(String nameServerAddress) {
        this.nameServerAddress = nameServerAddress;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
