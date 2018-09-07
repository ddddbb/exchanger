package wanglin.exchanger.framework;

import lombok.Data;

import java.util.Map;

@Data
public class Channel {
    public String              name;
    public Map<String, Object> attrs;
}
