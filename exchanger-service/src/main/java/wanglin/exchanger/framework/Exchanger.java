package wanglin.exchanger.framework;

import lombok.Data;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Data
public class Exchanger {
    public Channel             channel;
    public String              code;
    public DataTypeEnum        resultType     = DataTypeEnum.STRING;
    public DataTypeEnum        requestType    = DataTypeEnum.STRING;
    public ModelEnum           model          = ModelEnum.CLIENT;
    public String              url;
    public String              charset        = Charset.defaultCharset().name();
    public String              assemparer;
    public Integer             timeout        = 6000;
    public ProtocolEnum        protocol       = ProtocolEnum.HTTP;
    public ProtocolConfig      protocolConfig = ProtocolConfig.defaultHttpConfig();
    public SSL                 ssl;
    public Boolean             URL_ENCODE     = false;
    public Map<String, Object> attrs;


    @Data
    public static class SSL {
        public String              keyStoreType;
        public String              keyStorePassword;
        public String              keyStorePath;
        public Map<String, Object> attrs;
    }

    @Data
    public static class ProtocolConfig {
        public Integer             maxTotal;
        public Map<String, Object> attrs;

        public static ProtocolConfig defaultHttpConfig() {
            ProtocolConfig config = new ProtocolConfig();
            config.maxTotal = 2;
            config.attrs = new HashMap<>();
            return config;
        }
    }
}
