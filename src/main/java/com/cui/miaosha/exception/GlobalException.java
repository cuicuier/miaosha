package com.cui.miaosha.exception;

import com.cui.miaosha.result.CodeMsg;

public class GlobalException extends RuntimeException {
    private static final long serialVersionUID = 3960792308117795421L;
    private CodeMsg codeMsg;

    public GlobalException(CodeMsg codeMsg) {
        super(codeMsg.toString());
        this.codeMsg=codeMsg;
    }



    public CodeMsg getCodeMsg() {
        return codeMsg;
    }
}
