package com.liuzx.netty.rpc.server.session;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Set;

/**
 * 聊天组会话管理接口
 */
public interface GroupSession {

    /**
     * 创建一个聊天组, 如果不存在才能创建成功
     * @param name 组名
     * @param members 成员
     * @return 成功时返回 null , 失败返回 原来的value
     */
    Group createGroup(String name, Set<String> members);

    /**
     * 加入聊天组
     * @param name 组名
     * @param member 成员名
     * @return 如果组不存在返回 null, 否则返回组对象
     */
    Group joinMember(String name, String member);

    /**
     * 移除组成员
     * @param name 组名
     * @param member 成员名
     * @return 如果组不存在返回 null, 否则返回组对象
     */
    Group removeMember(String name, String member);

    /**
     * 移除聊天组
     * @param name 组名
     * @return 如果组不存在返回 null, 否则返回组对象
     */
    Group removeGroup(String name);

    /**
     * 获取组成员
     * @param name 组名
     * @return 成员集合, 没有成员会返回 empty set
     */
    Set<String> getMembers(String name);

    /**
     * 获取组成员的 channel 集合, 只有在线的 channel 才会返回
     * @param name 组名
     * @return 成员 channel 集合
     */
    List<Channel> getMembersChannel(String name);

    /**
     * 获取组成员的 channel 集合, 只有在线的 channel 才会返回
     * @param name 组名
     * @param fromUsername 发送者(过滤掉自己)
     * @return 成员 channel 集合
     */
    List<Channel> getMembersChannel(String name, String fromUsername);

    /**
     * 根据群聊名称判断群聊是否存在
     * @param name 组名
     * @return 存在 true, 不存在 false
     */
    boolean isGroupExist(String name);

    /**
     * 用户退出群聊
     * @param name 组名
     * @param username 退出用户
     * @return 成功 true
     */
    boolean quitGroup(String name, String username);
}
