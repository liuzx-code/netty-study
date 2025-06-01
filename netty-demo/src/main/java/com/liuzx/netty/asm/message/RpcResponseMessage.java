package com.liuzx.netty.asm.message;

import lombok.Data;
import lombok.ToString;

/**
 * @author ZhuHJay
 * @date 2023/3/10 17:12
 */
@Data
@ToString(callSuper = true)
public class RpcResponseMessage extends Message {
    /**
     * 返回值
     */
    private Object returnValue;
    /**
     * 异常值
     */
    private Exception exceptionValue;

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}
