package com.liuzx.netty.asm.server.handler;

import com.liuzx.netty.asm.message.GroupMembersRequestMessage;
import com.liuzx.netty.asm.message.GroupMembersResponseMessage;
import com.liuzx.netty.asm.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.Set;

/**
 * 群成员--处理器
 */
@ChannelHandler.Sharable
public class GroupMembersRequestMessageHandler extends AbstractRequestMessageHandler<GroupMembersRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupMembersRequestMessage msg) throws Exception {
        if (GroupSessionFactory.getGroupSession().isGroupExist(msg.getGroupName())) {
            ctx.writeAndFlush(new GroupMembersResponseMessage(false, "群聊[" + msg.getGroupName() + "]不存在"));
            return;
        }
        Set<String> members = GroupSessionFactory.getGroupSession().getMembers(msg.getGroupName());
        ctx.writeAndFlush(new GroupMembersResponseMessage(members));
    }
}
