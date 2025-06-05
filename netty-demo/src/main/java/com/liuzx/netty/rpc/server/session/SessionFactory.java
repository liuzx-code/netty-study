package com.liuzx.netty.rpc.server.session;

public abstract class SessionFactory {

    private static Session session = new SessionMemoryImpl();

    public static Session getSession() {
        return session;
    }
}
