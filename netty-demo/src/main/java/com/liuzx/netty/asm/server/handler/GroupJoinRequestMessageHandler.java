package com.liuzx.netty.asm.server.handler;

import com.liuzx.netty.asm.message.GroupJoinRequestMessage;
import com.liuzx.netty.asm.message.GroupJoinResponseMessage;
import com.liuzx.netty.asm.server.session.Group;
import com.liuzx.netty.asm.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * 加入群聊---处理器GroupMembersRequestMessageHandler
 */
@ChannelHandler.Sharable
public class GroupJoinRequestMessageHandler extends AbstractRequestMessageHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage msg) throws Exception {
        Group group = GroupSessionFactory.getGroupSession().joinMember(msg.getGroupName(), msg.getUsername());
        if (group == null) {
            // 组不存在
            ctx.writeAndFlush(new GroupJoinResponseMessage(false, "群聊[" + msg.getGroupName() + "]不存在"));
        } else {
            // 告诉群组中所有人添加消息
            List<Channel> channels = GroupSessionFactory.getGroupSession().getMembersChannel(msg.getGroupName(), msg.getUsername());
            channels.forEach(channel -> channel.writeAndFlush(new GroupJoinResponseMessage(true, msg.getUsername() + "加入[" + msg.getGroupName() + "]群聊")));
            // 独自发送给申请人的消息
            ctx.writeAndFlush(new GroupJoinResponseMessage(true, "您已加入[" + msg.getGroupName() + "]群聊"));
        }
    }
}
