package wanglin.exchanger.framework;

import java.io.IOException;
import java.util.Map;

/**
 * 报文组装和报文解析工具类
 * Assemparer = assembly+parser
 * <p>
 * * @param <AADT>  ASSEMBLY ARGS DATATYPE
 * * @param <ARDT>  ASSEMBLY RESULT DATATYPE
 * * @param <PADT>  PARSER ARGS DATATYPE
 * * @param <PRDT>  PARSER RESULT DATATYPE
 */
public interface Assemparer<AADT, ARDT, PADT, PRDT> {
    ARDT assemble(Exchanger exchanger, AADT object) throws AssemblyException;

    PRDT parse(Exchanger exchanger, PADT result) throws ParserException;

    /**
     * 请求文本
     */
    String txtOfAssemble(ARDT data);

    /**
     * 返回文本
     *
     * @param data
     * @return
     */
    String txtOfParse(PRDT data);
}
