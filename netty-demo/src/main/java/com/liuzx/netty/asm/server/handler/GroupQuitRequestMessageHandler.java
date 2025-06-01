package com.liuzx.netty.asm.server.handler;

import com.liuzx.netty.asm.message.GroupQuitRequestMessage;
import com.liuzx.netty.asm.message.GroupQuitResponseMessage;
import com.liuzx.netty.asm.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * 退出群--处理器
 */
@ChannelHandler.Sharable
public class GroupQuitRequestMessageHandler extends AbstractRequestMessageHandler<GroupQuitRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupQuitRequestMessage msg) throws Exception {
        if (GroupSessionFactory.getGroupSession().isGroupExist(msg.getGroupName())) {
            ctx.writeAndFlush(new GroupQuitResponseMessage(false, "群聊[" + msg.getGroupName() + "]不存在"));
            return;
        }
        if (!GroupSessionFactory.getGroupSession().quitGroup(msg.getGroupName(), msg.getUsername())) {
            ctx.writeAndFlush(new GroupQuitResponseMessage(false, "群聊[" + msg.getGroupName() + "]退出失败"));
            return;
        }
        // 退出成功告知在群用户
        List<Channel> channels = GroupSessionFactory.getGroupSession().getMembersChannel(msg.getGroupName());
        channels.forEach(channel -> channel.writeAndFlush(new GroupQuitResponseMessage(true, msg.getUsername() + "退出[" + msg.getGroupName() + "]群聊")));
    }
}
