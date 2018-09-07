package wanglin.exchanger.framework.base;


import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;
import wanglin.exchanger.framework.Assemparer;
import wanglin.exchanger.framework.DataTypeEnum;
import wanglin.exchanger.framework.Exchanger;

import java.io.IOException;
import java.nio.charset.Charset;

public abstract class HttpSendBaseAssemparer<AADT, PRDT> implements Assemparer<AADT, HttpUriRequest, HttpEntity, PRDT> {
    @Override
    public HttpUriRequest assemble(Exchanger exchanger, AADT args) {
        HttpUriRequest request = buildRequest(exchanger, args);
        Assert.notNull(request, "http请求不能为空");
        setHeader(request, exchanger, args);
        return request;
    }


    public abstract HttpUriRequest buildRequest(Exchanger exchanger, AADT args);

    public void setHeader(HttpUriRequest request, Exchanger exchanger, AADT args){
        if (exchanger.requestType == DataTypeEnum.JSON ) {
            request.setHeader( new BasicHeader("Content-Type", ContentType.APPLICATION_JSON.toString()));
        }
        if (exchanger.requestType == DataTypeEnum.XML ) {
            request.setHeader( new BasicHeader("Content-Type", ContentType.APPLICATION_XML.toString()));
        }
        if (exchanger.requestType == DataTypeEnum.STRING ) {
            request.setHeader( new BasicHeader("Content-Type", ContentType.TEXT_PLAIN.toString()));
        }
        if (exchanger.requestType == DataTypeEnum.BYTE ) {
            request.setHeader( new BasicHeader("Content-Type", ContentType.DEFAULT_BINARY.toString()));
        }

    }


    @Override
    public String txtOfAssemble(HttpUriRequest data) {
        String text = null;
        if (data instanceof HttpGet) {
            text = ((HttpGet) data).getURI().toString();
        } else if (data instanceof HttpPost) {
            try {
                text = EntityUtils.toString(((HttpPost) data).getEntity(), Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text;
    }

    @Override
    public String txtOfParse(PRDT data) {
        return JSON.toJSONString(data);
    }


}
