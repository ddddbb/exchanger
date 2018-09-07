package wanglin.exchanger.framework;

import javax.xml.transform.Result;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

/**
 */
public interface Connection<AADT, PRDT> {
    Future<PRDT> send(Exchanger exchanger, AADT args, Assemparer assemparer) throws IOException;
}
