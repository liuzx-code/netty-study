package com.liuzx.netty.asm.server.handler;

import com.liuzx.netty.asm.message.GroupCreateRequestMessage;
import com.liuzx.netty.asm.message.GroupCreateResponseMessage;
import com.liuzx.netty.asm.server.session.Group;
import com.liuzx.netty.asm.server.session.GroupSession;
import com.liuzx.netty.asm.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

/**
 * 创建群---管理器
 */
@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        // 群名
        final String groupName = msg.getGroupName();
        final Set<String> members = msg.getMembers();
        // 群管理器
        final GroupSession groupSession = GroupSessionFactory.getGroupSession();
        final Group group = groupSession.createGroup(groupName, members);

        // 创建成功
        if(group == null){
            // 发送成功消息
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, "成功创建群聊:" + groupName));

            // 发送拉群消息
            final List<Channel> channels = groupSession.getMembersChannel(groupName);
            for (Channel ch : channels){
                ch.writeAndFlush(new GroupCreateResponseMessage(true, "您已被拉入群聊:" + groupName));
            }
        }
        // 创建失败
        else{
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, "已存在群: " + groupName));
        }


    }
}
