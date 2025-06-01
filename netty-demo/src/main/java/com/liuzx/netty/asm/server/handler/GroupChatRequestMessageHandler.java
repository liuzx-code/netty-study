package com.liuzx.netty.asm.server.handler;

import com.liuzx.netty.asm.message.GroupChatRequestMessage;
import com.liuzx.netty.asm.message.GroupChatResponseMessage;
import com.liuzx.netty.asm.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

/**
 * 群聊---管理器
 */
@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage msg) throws Exception {
        // 判断群聊是否存在
        if (GroupSessionFactory.getGroupSession().isGroupExist(msg.getGroupName())) {
            ctx.writeAndFlush(new GroupChatResponseMessage(false, "群聊[" + msg.getGroupName() + "]不存在"));
            return;
        }
        // 获取群聊中在线成员, 不包括发送人
        List<Channel> channels = GroupSessionFactory.getGroupSession()
                .getMembersChannel(msg.getGroupName(), msg.getFrom());
        channels.forEach(channel ->
                channel.writeAndFlush(new GroupChatResponseMessage(msg.getGroupName() + "." + msg.getFrom(), msg.getContent())));

    }

}
