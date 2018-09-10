package wanglin.exchanger.framework;

/**
 * 获取渠道连接对象（作用类似于连接池，根据不同渠道，协议针对性实现，必须做缓存，重量级对象）
 */
public interface ProtocolFactory {
    /**
     * 获取服务连接
     *
     * @param exchanger
     * @return
     */
    Connection get(Exchanger exchanger);

    /**
     * 发布服务
     * <p>
     * 注册服务到url
     * 将url映射到对应的对应的exchanger的配置，并执行回调
     *
     * @param exchanger
     */
    void export(Exchanger exchanger);
}
