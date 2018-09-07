package wanglin.exchanger.framework;

public enum ProtocolEnum {
    HTTP, HTTPS,ZHONGAN,XINWANG,SPDB;

    public interface Pool<T> {
        T borrowObject();
    }
}
