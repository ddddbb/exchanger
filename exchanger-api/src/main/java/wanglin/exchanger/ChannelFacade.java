package wanglin.exchanger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ChannelFacade {
    /**
     * 渠道系统回调消息Q
     */
    static final String NOTIFY_MQ = "channel_exchange_notify";

    /**
     * 同步发送消息
     * @param code              交换码
     * @param object            请求对象，建议用json，map等通用字段
     * @return
     * @throws IOException        发送
     */
    Map sync(String code, Object object) throws IOException;

    /**
     * 异步发送
     * @param code
     * @param object
     * @throws IOException
     */
    void async(String code, Object object) throws IOException;

    /**
     * 渠道回调接口
     *
     *
     *          回调Controller ==>channelFacade.notify
     *          回调socket ==>channelFacade.notify -->notify_mq ==>Listner(code="")'s mehtod
     *          暂时没做服务export，不能可配，业务做自己做服务监听器。
     * @param code
     * @param object
     */
    void notify(String code, Object object);
}
