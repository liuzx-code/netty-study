package com.liuzx.netty.asm.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class ChatResponseMessage extends AbstractResponseMessage {

    /**
     * 发送者
     */
    private String from;
    private String content;

    public ChatResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    public ChatResponseMessage(String from, String content) {
        this.from = from;
        this.content = content;
    }

    @Override
    public int getMessageType() {
        return ChatResponseMessage;
    }

    @Override
    public String toString() {
        return "ChatResponseMessage{" +
                "from='" + from + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
