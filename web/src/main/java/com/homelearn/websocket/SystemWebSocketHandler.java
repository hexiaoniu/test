/**
 * Copyright(C) 2017 CEIEC All rights reserved.
 * Original Author: zhuzhiyuan@ceiec.com.cn, 2017/5/11
 */
package com.homelearn.websocket;

import com.homelearn.utils.Constant;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;

/**
 * @author zhuzhiyuan@ceiec.com.cn
 */
public class SystemWebSocketHandler implements WebSocketHandler, Observer {

    private class UserInfo {
        public String username;
        public WebSocketSession session;
        public RequestContext context;

        public UserInfo(String username, WebSocketSession session, RequestContext context) {
            this.username = username;
            this.session = session;
            this.context = context;
        }
    }

    private static final HashMap<String, List<UserInfo>> USERS;
    private static final HashMap<String, List<String>> RECEIVER_MAP;

    private static SystemWebSocketHandler instance = null;

    static {
        USERS = new HashMap<String, List<UserInfo>>();
        RECEIVER_MAP = new HashMap<>();
    }

    public SystemWebSocketHandler() {
        if (instance == null) {
            instance = this;
        }

    }

    public static SystemWebSocketHandler getInstance() {
        if (instance == null) {
            instance = new SystemWebSocketHandler();
        }
        return instance;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        try {
            String username = (String) webSocketSession.getAttributes().get(Constant.WEBSOCKET_USERNAME);

            RequestContext context = (RequestContext) webSocketSession.getAttributes().get(Constant.WEBSOCKET_CONTEXT);
            UserInfo userInfo = new UserInfo(username, webSocketSession, context);
            System.out.println("Message Add user " + username);
            List<UserInfo> list = USERS.get(username);
            if (list == null) {
                list = new ArrayList<UserInfo>();
            }
            list.add(userInfo);
            USERS.put(username, list);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void handleMessage(WebSocketSession webSocketSession,
                              WebSocketMessage<?> webSocketMessage) throws Exception {

    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession,
                                     Throwable throwable) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession,
                                      CloseStatus closeStatus) throws Exception {

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public synchronized void updateMessageReceiverByCameraId(String cameraId, String userName, String flag) {
        if ("open".equals(flag)) {
            if (cameraId != null && !cameraId.trim().isEmpty()) {
                if (RECEIVER_MAP.containsKey(cameraId)) {
                    if (!RECEIVER_MAP.get(cameraId).contains(userName)) {
                        RECEIVER_MAP.get(cameraId).add(userName);
                    }
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(userName);
                    RECEIVER_MAP.put(cameraId, list);
                }
            }
        }
        if ("close".equals(flag)){
            for (Map.Entry<String, List<String>> entry : RECEIVER_MAP.entrySet()) {
                if (entry.getKey().equals(cameraId)){
                    entry.getValue().remove(userName);
                }
            }
        }
    }

    public void sendMessageToReceivers(String cameraId, Object data) {
        if (RECEIVER_MAP.containsKey(cameraId)) {
            List<String> userList = RECEIVER_MAP.get(cameraId);
            for (String username : userList) {
                sendMessageToUser(username, data);
            }
        }
    }

    /**
     * Send a message to the web browser of all users
     *
     * @param message
     */
    public void sendMessageToUsers(TextMessage message) {
        Collection<List<UserInfo>> colInfos = USERS.values();
        for (List<UserInfo> userInfoList : colInfos) {
            for (UserInfo userInfo : userInfoList) {
                WebSocketSession session = userInfo.session;
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Send a message to the web browser of a user. The user can receive the
     * message only when he is online. If he is offline, he will receive the
     * message when he logged in next time.
     *
     * @param username
     * @param message
     */
    public void sendMessageToUser(String username, Object message) {
        System.out.println("--------------USERS--------------------"+USERS.containsKey(username));
        if (USERS.containsKey(username)) {
            List<UserInfo> userInfoList = USERS.get(username);
            for (UserInfo userInfo : userInfoList) {
                if (userInfo != null) {
                    sendMessageTo(userInfo, message);
                }
            }
        }
    }

    /**
     * Send a message to the web browser of users with a roleType.
     */


    private void sendMessageTo(UserInfo userInfo, Object message) {
        WebSocketSession session = userInfo.session;
        try {
            if (session.isOpen()) {

                TextMessage textMessage;
                if (null != message) {
                    textMessage = messageToTextMessage(message);

                } else {
                    textMessage = messageToTextMessage(null);
                }
                session.sendMessage(textMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Observable o, Object data) {
        //MQMessage message = (MQMessage) data;
        HashSet<String> usernames = new HashSet<String>();
        Collection<List<UserInfo>> colInfos = USERS.values();
        System.out.println("Message user count " + colInfos.size());
        int i = 0;
        for (List<UserInfo> userInfoList : colInfos) {
            for (UserInfo userInfo : userInfoList) {
                i++;
                System.out.println("    " + i + " : " + userInfo.username);
                usernames.add(userInfo.username);
            }
        }

        Iterator<String> itr = usernames.iterator();
        while (itr.hasNext()) {
            String username = itr.next();
            sendMessageToUser(username, data);
        }

    }

    private TextMessage messageToTextMessage(Object message) {
        return new TextMessage(message.toString());
    }

    public void removeWebSocketSession(String user) {
        try {
            USERS.remove(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * 将没有切换到web抓拍窗口的用户从集合中进行移除
     */
    public void removeCameraUser(String userName){
        for (Map.Entry<String, List<String>> entry : RECEIVER_MAP.entrySet()) {
            List<String> users = entry.getValue();
            if (users.contains(userName)){
                users.remove(userName);
            }
        }
    }
}
