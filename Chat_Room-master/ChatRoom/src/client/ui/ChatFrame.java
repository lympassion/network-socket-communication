package client.ui;

import client.ClientThread;
import client.DataBuffer;
import client.model.entity.MyCellRenderer;
import client.model.entity.OnlineUserListModel;
import client.util.ClientUtil;
import common.model.entity.FileInfo;
import common.model.entity.Message;
import common.model.entity.Request;
import common.model.entity.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatFrame extends JFrame{
    private static final long serialVersionUID = -2310785591507878535L;
    /**聊天对方的信息Label*/
    private JLabel otherInfoLbl;
    /** 当前用户信息Lbl */
    private JLabel currentUserLbl;
    /**聊天信息列表区域*/
    public static JTextArea msgListArea;
    /**要发送的信息区域*/
    public static JTextArea sendArea;
    /** 在线用户列表 */
    public static JList onlineList;
    /** 在线用户数统计Lbl */
    public static JLabel onlineCountLbl;
    /** 准备发送的文件 */
    public static FileInfo sendFile;

    /** 私聊复选框 */
    public JCheckBox rybqBtn;

    /** 组通信复选框 */
    public JCheckBox groupBtn;

    /**组通信选人确认按钮*/
    private JButton okBtn;

    /**消息接收用户基数*/
    public static int UserNum = 2;


    public ChatFrame(){
        this.init();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    public void init(){
        this.setTitle("主面板");
        this.setSize(550, 500);
        this.setResizable(false);

        //设置默认窗体在屏幕中央
        int x = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int y = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        this.setLocation((x - this.getWidth()) / 2, (y-this.getHeight())/ 2);

        //左边主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        //右边用户面板
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());

        // 创建一个分隔窗格
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                mainPanel, userPanel);
        splitPane.setDividerLocation(380);
        splitPane.setDividerSize(10);
        splitPane.setOneTouchExpandable(true);
        this.add(splitPane, BorderLayout.CENTER);

        //左上边信息显示面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        //左下边发送消息面板
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BorderLayout());

        // 创建一个分隔窗格
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                infoPanel, sendPanel);
        splitPane2.setDividerLocation(300);
        splitPane2.setDividerSize(1);
        mainPanel.add(splitPane2, BorderLayout.CENTER);

        otherInfoLbl = new JLabel("对话框");
        infoPanel.add(otherInfoLbl, BorderLayout.NORTH);

        msgListArea = new JTextArea();
        msgListArea.setLineWrap(true);
        infoPanel.add(new JScrollPane(msgListArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BorderLayout());
        sendPanel.add(tempPanel, BorderLayout.NORTH);

        // 聊天按钮面板
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(btnPanel, BorderLayout.CENTER);

        //组通信按钮
        groupBtn = new JCheckBox("组通信");
        btnPanel.add(groupBtn, BorderLayout.AFTER_LAST_LINE);

        //私聊按钮
        rybqBtn = new JCheckBox("私聊");
        btnPanel.add(rybqBtn, BorderLayout.EAST);

        //发送文件按钮
        JButton sendFileBtn = new JButton("发送文件");
        btnPanel.add(sendFileBtn);

        //要发送的信息的区域
        sendArea = new JTextArea();
        sendArea.setLineWrap(true);
        sendPanel.add(new JScrollPane(sendArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        // 聊天按钮面板
        JPanel btn2Panel = new JPanel();
        btn2Panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        this.add(btn2Panel, BorderLayout.SOUTH);
        JButton closeBtn = new JButton("关闭");
        btn2Panel.add(closeBtn);
        JButton submitBtn = new JButton("发送");
        btn2Panel.add(submitBtn);
        sendPanel.add(btn2Panel, BorderLayout.SOUTH);

        //在线用户列表面板
        JPanel onlineListPane = new JPanel();
        onlineListPane.setLayout(new BorderLayout());
        onlineCountLbl = new JLabel("在线用户列表(1)");
        onlineListPane.add(onlineCountLbl, BorderLayout.NORTH);

        //当前用户面板
        JPanel currentUserPane = new JPanel();
        currentUserPane.setLayout(new BorderLayout());

        // 右边用户列表创建一个分隔窗格
        JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                onlineListPane, currentUserPane);
        splitPane3.setDividerLocation(340);
        splitPane3.setDividerSize(1);
        userPanel.add(splitPane3, BorderLayout.CENTER);

        //获取在线用户并缓存
        DataBuffer.onlineUserListModel = new OnlineUserListModel(DataBuffer.onlineUsers);
        //在线用户列表
        onlineList = new JList(DataBuffer.onlineUserListModel);
        onlineList.setCellRenderer(new MyCellRenderer());
        //设置为单选模式
        onlineList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        onlineListPane.add(new JScrollPane(onlineList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        //当前用户信息Label
        currentUserLbl = new JLabel();
        currentUserPane.add(currentUserLbl);

        //关闭窗口
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        //关闭按钮的事件
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout();
            }
        });

        //选择某个用户私聊
        rybqBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(rybqBtn.isSelected()){
                    User selectedUser = (User)onlineList.getSelectedValue();
                    if(null == selectedUser){
                        otherInfoLbl.setText("选择一位好友，开始聊天");
                    }else if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                        otherInfoLbl.setText("无法与自己通信");
                    }else{
                        otherInfoLbl.setText("正在和 "+ selectedUser.getNickname()
                                +"(" + selectedUser.getId() + ") 聊天");
                    }
                }else if(groupBtn.isSelected()){
                    otherInfoLbl.setText("正在组通信");
                }
            }
        });

        //选择某个用户
        onlineList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                User selectedUser = (User)onlineList.getSelectedValue();
                if(rybqBtn.isSelected()){
                    if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                        otherInfoLbl.setText("无法与自己通信");
                    }else{
                        otherInfoLbl.setText("正在和 "+ selectedUser.getNickname()
                                +"(" + selectedUser.getId() + ") 聊天");
                    }
                }
            }
        });

        groupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new GroupUsersSelectFrame();

            }
        });

        //发送文本消息
        sendArea.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() == Event.ENTER){
                    sendTxtMsg();
                }
            }
        });
        submitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendTxtMsg();
            }
        });

        //发送文件
        sendFileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendFile();
            }
        });

        this.loadData();  //加载初始数据
    }

    /**  加载数据 */
    public void loadData(){
        //加载当前用户数据
        if(null != DataBuffer.currentUser){
            currentUserLbl.setIcon(
                    new ImageIcon("images/" + DataBuffer.currentUser.getHead() + ".png"));
            currentUserLbl.setText(DataBuffer.currentUser.getNickname()
                    + "(" + DataBuffer.currentUser.getId() + ")");
        }
        //设置在线用户列表
        onlineCountLbl.setText("在线用户列表("+ DataBuffer.onlineUserListModel.getSize() +")");
        //启动监听服务器消息的线程
        new ClientThread(this).start();
    }

    /** 发送文本消息 */
    public void sendTxtMsg(){
        String content = sendArea.getText();
        if ("".equals(content)) { //无内容
            JOptionPane.showMessageDialog(ChatFrame.this, "不能发送空消息!",
                    "不能发送", JOptionPane.ERROR_MESSAGE);
        } else { //发送
            Message msg = new Message();
            //私聊或群聊
            if(rybqBtn.isSelected()){  //私聊
                User selectedUser = (User)onlineList.getSelectedValue();
                if(null == selectedUser){
                    JOptionPane.showMessageDialog(ChatFrame.this, "没有选择私聊对象!",
                            "不能发送", JOptionPane.ERROR_MESSAGE);
                    return;
                }else if (DataBuffer.currentUser.getId() == selectedUser.getId()){
                    JOptionPane.showMessageDialog(ChatFrame.this, "不能给自己发送消息!",
                            "不能发送", JOptionPane.ERROR_MESSAGE);
                    return;
                }else{
                    msg.setToUser(selectedUser);
                }
            }

            msg.setFromUser(DataBuffer.currentUser);
            msg.setSendTime(new Date());

            DateFormat df = new SimpleDateFormat("HH:mm:ss");
            StringBuffer sb = new StringBuffer();
            sb.append(" ").append(df.format(msg.getSendTime())).append(" ")
                    .append(msg.getFromUser().getNickname())
                    .append("(").append(msg.getFromUser().getId()).append(") ");
            if(groupBtn.isSelected()){//群聊
                msg.setToListUser(GroupUsersSelectFrame.groupUserSelectedList);
                sb.append("对大家说");
            }
            sb.append("\n  ").append(content).append("\n");
            msg.setMessage(sb.toString());

            Request request = new Request();
            request.setAction("chat");
            request.setAttribute("msg", msg);
            try {
                ClientUtil.sendTextRequest2(request);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //JTextArea中按“Enter”时，清空内容并回到首行
            InputMap inputMap = sendArea.getInputMap();
            ActionMap actionMap = sendArea.getActionMap();
            Object transferTextActionKey = "TRANSFER_TEXT";
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),transferTextActionKey);
            actionMap.put(transferTextActionKey,new AbstractAction() {
                private static final long serialVersionUID = 7041841945830590229L;
                public void actionPerformed(ActionEvent e) {
                    sendArea.setText("");
                    sendArea.requestFocus();
                }
            });
            sendArea.setText("");
            ClientUtil.appendTxt2MsgListArea(msg.getMessage());
        }
    }

    /** 发送文件 */
    private void sendFile() {
        File file = new File("");
        List selectedList = onlineList.getSelectedValuesList();
        if(null != selectedList){

            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(ChatFrame.this) == JFileChooser.APPROVE_OPTION) {
                 file = jfc.getSelectedFile();
            }

            for (int i = 0; i < selectedList.size(); i++){
                User selectedUser = (User) selectedList.get(i);

                if(DataBuffer.currentUser.getId() == selectedUser.getId()){
                    JOptionPane.showMessageDialog(ChatFrame.this, "不能给自己发送文件!",
                            "不能发送", JOptionPane.ERROR_MESSAGE);
                    continue;
                }else{
                    if (file != null) {
                        sendFile = new FileInfo();
                        sendFile.setFromUser(DataBuffer.currentUser);
                        sendFile.setToUser(selectedUser);
                        try {
                            sendFile.setSrcName(file.getCanonicalPath());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        sendFile.setSendTime(new Date());

                        Request request = new Request();
                        request.setAction("toSendFile");
                        request.setAttribute("file", sendFile);
                        try {
                            ClientUtil.sendTextRequest2(request);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ClientUtil.appendTxt2MsgListArea("【文件消息】向 "
                                + selectedUser.getNickname() + "("
                                + selectedUser.getId() + ") 发送文件 ["
                                + file.getName() + "]，等待对方接收...\n");
                    }
                }
            }
        }
    }

    /** 关闭客户端 */
    private void logout() {
        int select = JOptionPane.showConfirmDialog(ChatFrame.this,
                "确定关闭吗？", "关闭客户端",
                JOptionPane.YES_NO_OPTION);
        if (select == JOptionPane.YES_OPTION) {
            Request req = new Request();
            req.setAction("exit");
            req.setAttribute("user", DataBuffer.currentUser);
            try {
                ClientUtil.sendTextRequest(req);
            } catch (IOException ex) {
                ex.printStackTrace();
            }finally{
                System.exit(0);
            }
        }else{
            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }
    }
}
