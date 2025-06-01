package com.liuzx.netty.asm.server.session;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GroupSessionMemoryImpl implements GroupSession {
    private final Map<String, Group> groupMap = new ConcurrentHashMap<>();

    @Override
    public Group createGroup(String name, Set<String> members) {
        Group group = new Group(name, members);
        return groupMap.putIfAbsent(name, group); // 没有则放入
    }

    @Override
    public Group joinMember(String name, String member) {
        return groupMap.computeIfPresent(name, (key, value) -> {
            value.getMembers().add(member);       // 指定 key 的值进行重新计算，前提是该 key 存在于 hashMap 中
            return value;
        });
    }

    @Override
    public Group removeMember(String name, String member) {
        return groupMap.computeIfPresent(name, (key, value) -> {
            value.getMembers().remove(member);
            return value;
        });
    }

    @Override
    public Group removeGroup(String name) {
        return groupMap.remove(name);
    }

    @Override
    public Set<String> getMembers(String name) {
        return groupMap.getOrDefault(name, Group.EMPTY_GROUP).getMembers();
    }

    @Override
    public List<Channel> getMembersChannel(String name) {
        return getMembers(name).stream()
                .map(member -> SessionFactory.getSession().getChannel(member))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 根据 【群聊名称】 -> 【用户名Set】 -> map遍历 -> 【用户名获取到 所有对应的 channel】 -> 【channel List】
    @Override
    public List<Channel> getMembersChannel(String name, String fromUsername) {
        return getMembers(name).stream()
                // 过滤发送人
                .filter(member -> !fromUsername.equals(member))
                .map(member -> SessionFactory.getSession().getChannel(member)) // 根据成员名 获得Channel
                .filter(Objects::nonNull)                                      // 不是 null 才会 被下面收集
                .collect(Collectors.toList());
    }



    @Override
    public boolean isGroupExist(String name) {
        return !groupMap.containsKey(name);
    }

    @Override
    public boolean quitGroup(String name, String username) {
        // 为空则删除失败
        return null != groupMap.computeIfPresent(name, (key, oldValue) -> {
            Set<String> members = oldValue.getMembers().stream()
                    // 过滤退出用户
                    .filter(member -> !username.equals(member))
                    .collect(Collectors.toSet());
            oldValue.setMembers(members);
            return oldValue;
        });
    }
}
