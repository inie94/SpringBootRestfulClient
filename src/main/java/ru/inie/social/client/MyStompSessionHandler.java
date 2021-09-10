package ru.inie.social.client;

import net.minidev.json.JSONObject;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;

public class MyStompSessionHandler extends StompSessionHandlerAdapter {

    private final String username;
    private StompSession session;

    public MyStompSessionHandler(String username) {
        this.username = username;
    }

    @Override
    public void afterConnected(StompSession session,
                               StompHeaders connectedHeaders) {
        this.session = session;
        session.subscribe("/topic/public", this);
        session.send("/app/chat.addUser",
                        new JSONObject()
                                .appendField("sender", username)
                                .appendField("type", "JOIN")
        );
    }
    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        ChatMessage message = (ChatMessage) payload;
        switch (message.getType()) {
            case JOIN:
                System.out.println(message.getSender() + " joined!");
                break;
            case LEAVE:
                System.out.println(message.getSender() + " left!");
                break;
            case CHAT:
                System.out.println("[from: " + message.getSender() + "] " + message.getContent());
        }

        System.out.print("Write message: ");
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return ChatMessage.class;
    }

    public StompSession getSession() {
        return session;
    }
}
