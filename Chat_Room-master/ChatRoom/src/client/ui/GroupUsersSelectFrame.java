package client.ui;

import client.DataBuffer;
import client.model.entity.MyCellRenderer;
import client.model.entity.OnlineUserListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GroupUsersSelectFrame extends JFrame {
    /** 在线用户列表 */
    public static JList onlineList;
    /** 在线用户数统计Lbl */
    public static JLabel onlineCountLbl;
    /** 以选择的组通信用户列表 */
    public static List groupUserSelectedList;

    /**确认按钮*/
    private JButton okBtn;

    public GroupUsersSelectFrame(){
        this.init();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    public void init(){
        this.setTitle("选择组通信用户");
        this.setSize(450, 600);
        this.setResizable(false);

        //设置默认窗体在屏幕中央
        int x = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int y = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        this.setLocation((x - this.getWidth()) / 2, (y-this.getHeight())/ 2);

        //在线用户列表面板
        JPanel onlineListPane = new JPanel();
        onlineListPane.setLayout(new BorderLayout());

        //确认面板
        JPanel okPane = new JPanel();
        okPane.setLayout(new BorderLayout());


        // 右边用户列表创建一个分隔窗格
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                onlineListPane, okPane);
        splitPane.setDividerLocation(500);
        splitPane.setDividerSize(1);
        this.add(splitPane, BorderLayout.CENTER);


        //获取在线用户并缓存
        DataBuffer.onlineUserListModel = new OnlineUserListModel(DataBuffer.onlineUsers);
        //在线用户列表
        onlineList = new JList(DataBuffer.onlineUserListModel);
        onlineList.setCellRenderer(new MyCellRenderer());
        //设置为复选模式
        onlineList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        onlineListPane.add(new JScrollPane(onlineList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        //确认Label
        okBtn = new JButton("确认");
        okPane.add(okBtn);


        //确认按钮的事件
        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                groupUserSelectedList = onlineList.getSelectedValuesList();
//                System.out.println(groupSelectedList);

                //关闭（隐藏）窗口
                setVisible(false);
            }
        });


    }
}
