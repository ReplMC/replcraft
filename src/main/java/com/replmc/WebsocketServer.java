package com.replmc;


import io.javalin.Javalin;

public class WebsocketServer {
    private final Javalin app;

    public WebsocketServer() {
        app = Javalin.create();

        app.get("/", ctx -> ctx.result("Hello from ReplCraft"));

        app.start(4680);
    }

    public void shutdown() {
        this.app.stop();
    }
}
