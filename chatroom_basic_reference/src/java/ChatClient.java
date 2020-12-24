/*
 * Copyright 2019-2022 the original author or authors.
 */

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;
import javax.swing.JFrame;

/**
 * 聊天客户端
 */
public class ChatClient extends JFrame {

    private static final long serialVersionUID = -2270001423738681797L;

    /**
     * 这里获取系统用户名，如果获取失败，则通过UUID随机生成,取前八位
     */
    private static final String userName =
            System.getProperty("user.name",
                    "路人" + UUID.randomUUID().toString().substring(0, 8));

    /**
     * logged-in-ness的状态
     */
    protected boolean loggedIn;

    /**
     * 界面主框架
     */
    protected JFrame windows;

    /**
     * 默认端口号
     */
    protected static final int PORT_NUM = ChatProtocol.PORT_NUM;

    /**
     * 实际端口号
     */
    protected int port;

    /**
     * 网络客户端套接字
     */
    protected Socket sock;

    /**
     * 用于从套接字读取数据（读取其他聊友发送的消息）
     */
    protected BufferedReader is;

    /**
     * 用于在套接字上发送行数据（即用户发送消息到聊天室）
     */
    protected PrintWriter pw;

    /**
     * 用于输入TextField tf
     */
    protected TextField input;

    /**
     * 用于显示对话的TextArea（消息展示的界面）
     */
    protected TextArea messageView;

    /**
     * 登陆按钮
     */
    protected Button loginButton;

    /**
     * 注销按钮
     */
    protected Button logoutButton;

    /**
     * 应用程序的标题
     */
    protected static String TITLE = "Chat Room Client";

    /**
     * 这里设置服务器的地址，默认为本地
     */
    protected String serverHost = "localhost";

    /**
     * 设置GUI
     */
    public ChatClient() {
        windows = this;
        windows.setTitle(TITLE);
        windows.setLayout(new BorderLayout());
        port = PORT_NUM;

        // GUI，消息展示界面样式
        messageView = new TextArea(30, 80);
        messageView.setEditable(false);
        messageView.setFont(new Font("Monospaced", Font.PLAIN, 15));
        windows.add(BorderLayout.NORTH, messageView);

        // 创建一个板块
        Panel panel = new Panel();

        // 在板块上添加登陆按钮
        panel.add(loginButton = new Button("Login"));
        loginButton.setEnabled(true);
        loginButton.requestFocus();
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
                loginButton.setEnabled(false);
                logoutButton.setEnabled(true);
                input.requestFocus();
            }
        });

        // 在板块上添加注销按钮
        panel.add(logoutButton = new Button("Logout"));
        logoutButton.setEnabled(false);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
                loginButton.setEnabled(true);
                logoutButton.setEnabled(false);
                loginButton.requestFocus();
            }
        });

        //  消息输入框
        panel.add(new Label("Message here..."));
        input = new TextField(40);
        input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 判断有无登录，登录后才能发送消息
                if (loggedIn) {
                    // 以广播的方式发送出去，所有人可见
                    pw.println(ChatProtocol.CMD_BCAST + input.getText());
                    // 发送后，发消息输入框置空
                    input.setText("");
                }
            }
        });

        // 把消息输入框加入板块
        panel.add(input);

        // 把板块加入主界面的最下方
        windows.add(BorderLayout.SOUTH, panel);
        windows.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        windows.pack();
    }


    /**
     * 登陆到聊天室
     */
    public void login() {
        showStatus("准备登录!");
        // 如果已经的登录，则返回
        if (loggedIn) {
            return;
        }
        // 没有登录，开始尝试连接到聊天室服务器
        try {
            // 聊天室服务器地址使用了默认的localhost（127.0.0.1）地址
            sock = new Socket(serverHost, port);
            TITLE += userName;
            is = new BufferedReader(
                    new InputStreamReader(sock.getInputStream(), "utf-8"));
            pw = new PrintWriter(
                    new OutputStreamWriter(sock.getOutputStream(), "utf-8"), true);
            showStatus("获取到聊天服务器的socket");
            // 现在假登录，不需要输入密码
            pw.println(ChatProtocol.CMD_LOGIN + userName);
            loggedIn = true;
        } catch (IOException ex) {
            showStatus("获取不到服务器的socket " + serverHost + "/" + port + ": " + ex.toString());
            windows.add(new Label("获取socket失败: " + ex.toString()));
            return;
        }

        //构建和启动reader： 读取服务器的消息到消息展示区
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line;
                try {
                    // 只要登录并且服务器有消息可读
                    while (loggedIn && ((line = is.readLine()) != null)) {
                        // 每读取一行消息，换行
                        messageView.append(line + "\n");
                    }
                } catch (IOException ex) {
                    showStatus("与其他客户端连接中断!\n" + ex.toString());
                    return;
                }
            }
        }).start();
    }

    /**
     * 登出聊天室
     */
    public void logout() {
        // 如果已经登出了，则直接返回
        if (!loggedIn) {
            return;
        }
        // 修改登录状态，释放socket资源
        loggedIn = false;
        try {
            if (sock != null) {
                sock.close();
            }
        } catch (Exception ex) {
            // 处理异常
            System.out.println("聊天室关闭异常： " + ex.toString());
        }
    }

    /**
     * 控制输出状态，便于调试
     *
     * @param message 状态信息
     */
    public void showStatus(String message) {
        System.out.println(message);
    }

    /**
     * main方法 允许客户端作为一个应用程序
     */
    public static void main(String[] args) {
        ChatClient room = new ChatClient();
        room.pack();
        room.setVisible(true);
    }

}
