package com.liuzx.netty.asm.server.handler;

import com.liuzx.netty.asm.message.LoginRequestMessage;
import com.liuzx.netty.asm.message.LoginResponseMessage;
import com.liuzx.netty.asm.server.service.UserServiceFactory;
import com.liuzx.netty.asm.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable // 没有共享变量、没有状态信息
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {

        final String username = msg.getUsername();
        final String password = msg.getPassword();

        final boolean login = UserServiceFactory.getUserService().login(username, password);
        LoginResponseMessage responseMessage;

        if (login) {
            SessionFactory.getSession().bind(ctx.channel(), username); // 用户 、channel 简历关系
            responseMessage = new LoginResponseMessage(true, "登录成功！");
        } else {
            responseMessage = new LoginResponseMessage(false, "用户名或密码错误！");
        }
        // 登录结果 返回
        ctx.writeAndFlush(responseMessage); // 【 【当前节点】 开始向上 找 "出站Handler"  (ch.writeAndFlush 和 ctx.channel().write(msg) 从尾部向上查找)】

    }
}
