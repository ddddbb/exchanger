package wanglin.exchanger.framework.base;


import wanglin.exchanger.framework.Assemparer;
import wanglin.exchanger.framework.Exchanger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class HttpNotifyBaseAssemparer implements Assemparer<Object, Object, Object[], Object> {


    @Override
    public Object assemble(Exchanger exchanger, Object object) {
        return null;
    }

    @Override
    public Object parse(Exchanger exchanger, Object[] args) {
        return parse(exchanger, (HttpServletRequest) args[0], (HttpServletResponse) args[1]);
    }

    protected abstract Object parse(Exchanger exchanger, HttpServletRequest arg, HttpServletResponse response);

}
