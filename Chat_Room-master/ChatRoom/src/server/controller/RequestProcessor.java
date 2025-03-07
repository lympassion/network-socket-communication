package server.controller;

import common.model.entity.*;
import server.DataBuffer;
import server.OnlineClientIOCache;
import server.model.service.UserService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RequestProcessor implements Runnable {
    private Socket currentClientSocket;  //当前正在请求服务器的客户端Socket

    public RequestProcessor(Socket currentClientSocket){
        this.currentClientSocket = currentClientSocket;
    }

    /**组通信列表*/
    public static List<User> groupList = null;

    public void run() {
        boolean flag = true; //是否不间断监听
        try{
            OnlineClientIOCache currentClientIOCache = new OnlineClientIOCache(
                    new ObjectInputStream(currentClientSocket.getInputStream()),
                    new ObjectOutputStream(currentClientSocket.getOutputStream()));
            while(flag){ //不停地读取客户端发过来的请求对象
                //从请求输入流中读取到客户端提交的请求对象
                Request request = (Request)currentClientIOCache.getOis().readObject();
                String actionName = request.getAction();   //获取请求中的动作
                if(actionName.equals("userRegiste")){      //用户注册
                    registe(currentClientIOCache, request);
                }else if(actionName.equals("userLogin")){  //用户登录
                    login(currentClientIOCache, request);
                }else if("exit".equals(actionName)){       //请求断开连接
                    flag = logout(currentClientIOCache, request);
                }else if("chat".equals(actionName)){       //聊天
                    chat(request);
                }else if("toSendFile".equals(actionName)){ //准备发送文件
                    toSendFile(request);
                }else if("agreeReceiveFile".equals(actionName)){ //同意接收文件
                    agreeReceiveFile(request);
                }else if("refuseReceiveFile".equals(actionName)){ //拒绝接收文件
                    refuseReceiveFile(request);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /** 拒绝接收文件 */
    private void refuseReceiveFile(Request request) throws IOException {
        FileInfo sendFile = (FileInfo)request.getAttribute("sendFile");
        Response response = new Response();  //创建一个响应对象
        response.setType(ResponseType.REFUSERECEIVEFILE);
        response.setData("sendFile", sendFile);
        response.setStatus(ResponseStatus.OK);
        //向请求方的输出流输出响应
        OnlineClientIOCache ocic = DataBuffer.onlineUserIOCacheMap.get(sendFile.getFromUser().getId());
        this.sendResponse(ocic, response);
    }

    /** 同意接收文件 */
    private void agreeReceiveFile(Request request) throws IOException {
        FileInfo sendFile = (FileInfo)request.getAttribute("sendFile");
        //向请求方(发送方)的输出流输出响应
        Response response = new Response();  //创建一个响应对象
        response.setType(ResponseType.AGREERECEIVEFILE);
        response.setData("sendFile", sendFile);
        response.setStatus(ResponseStatus.OK);
        OnlineClientIOCache sendIO = DataBuffer.onlineUserIOCacheMap.get(sendFile.getFromUser().getId());
        this.sendResponse(sendIO, response);

        //向接收方发出接收文件的响应
        Response response2 = new Response();  //创建一个响应对象
        response2.setType(ResponseType.RECEIVEFILE);
        response2.setData("sendFile", sendFile);
        response2.setStatus(ResponseStatus.OK);
        OnlineClientIOCache receiveIO = DataBuffer.onlineUserIOCacheMap.get(sendFile.getToUser().getId());
        this.sendResponse(receiveIO, response2);
    }

    /** 客户端退出 */
    public boolean logout(OnlineClientIOCache oio, Request request) throws IOException{
        System.out.println(currentClientSocket.getInetAddress().getHostAddress()
                + ":" + currentClientSocket.getPort() + "走了");

        User user = (User)request.getAttribute("user");
        //把当前上线客户端的IO从Map中删除
        DataBuffer.onlineUserIOCacheMap.remove(user.getId());
        //从在线用户缓存Map中删除当前用户
        DataBuffer.onlineUsersMap.remove(user.getId());

        Response response = new Response();  //创建一个响应对象
        response.setType(ResponseType.LOGOUT);
        response.setData("logoutUser", user);
        oio.getOos().writeObject(response);  //把响应对象往客户端写
        oio.getOos().flush();
        currentClientSocket.close();  //关闭这个客户端Socket

        DataBuffer.onlineUserTableModel.remove(user.getId()); //把当前下线用户从在线用户表Model中删除
        iteratorResponse(response);//通知所有其它在线客户端

        return false;  //断开监听
    }
    /** 注册 */
    public void registe(OnlineClientIOCache oio, Request request) throws IOException {
        User user = (User)request.getAttribute("user");
        UserService userService = new UserService();
        userService.addUser(user);

        Response response = new Response();  //创建一个响应对象
        response.setStatus(ResponseStatus.OK);
        response.setData("user", user);

        oio.getOos().writeObject(response);  //把响应对象往客户端写
        oio.getOos().flush();

        //把新注册用户添加到RegistedUserTableModel中
        DataBuffer.registedUserTableModel.add(new String[]{
                String.valueOf(user.getId()),
                user.getPassword(),
                user.getNickname(),
                String.valueOf(user.getSex())
        });
    }

    /** 登录 */
    public void login(OnlineClientIOCache currentClientIO, Request request) throws IOException {
        String idStr = (String)request.getAttribute("id");
        String password = (String) request.getAttribute("password");
        UserService userService = new UserService();
        User user = userService.login(Long.parseLong(idStr), password);

        Response response = new Response();  //创建一个响应对象
        if(null != user){
            if(DataBuffer.onlineUsersMap.containsKey(user.getId())){ //用户已经登录了
                response.setStatus(ResponseStatus.OK);
                response.setData("msg", "该 用户已经在别处上线了！");
                currentClientIO.getOos().writeObject(response);  //把响应对象往客户端写
                currentClientIO.getOos().flush();
            }else { //正确登录
                DataBuffer.onlineUsersMap.put(user.getId(), user); //添加到在线用户

                //设置在线用户
                response.setData("onlineUsers",
                        new CopyOnWriteArrayList<User>(DataBuffer.onlineUsersMap.values()));

                response.setStatus(ResponseStatus.OK);
                response.setData("user", user);
                currentClientIO.getOos().writeObject(response);  //把响应对象往客户端写
                currentClientIO.getOos().flush();

                //通知其它用户有人上线了
                Response response2 = new Response();
                response2.setType(ResponseType.LOGIN);
                response2.setData("loginUser", user);
                iteratorResponse(response2);

                //把当前上线的用户IO添加到缓存Map中
                DataBuffer.onlineUserIOCacheMap.put(user.getId(),currentClientIO);

                //把当前上线用户添加到OnlineUserTableModel中
                DataBuffer.onlineUserTableModel.add(
                        new String[]{String.valueOf(user.getId()),
                                user.getNickname(),
                                String.valueOf(user.getSex())});
            }
        }else{ //登录失败
            response.setStatus(ResponseStatus.OK);
            response.setData("msg", "账号或密码不正确！");
            currentClientIO.getOos().writeObject(response);
            currentClientIO.getOos().flush();
        }
    }

    private void loadGroupMembers(){
        long c1 = 2, c2 = 3, c3 = 4;
        DataBuffer.groupMemberMap.put(c1, c1);
        DataBuffer.groupMemberMap.put(c2, c2);
        DataBuffer.groupMemberMap.put(c3, c3);
    }

    /** 聊天 */
    public void chat(Request request) throws IOException {

        Message msg = (Message)request.getAttribute("msg");
        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.CHAT);
        response.setData("txtMsg", msg);

        if(msg.getToUser() != null){
            OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
            sendResponse(io, response);
        } else {
            groupList = msg.getToListUser();
            User user = null;
            for(int i = 0; i < groupList.size(); i++){
                user = groupList.get(i);
                Long id  = user.getId();
                if(msg.getFromUser().getId() == id){ continue; }
                sendResponse(DataBuffer.onlineUserIOCacheMap.get(id), response);
            }
            groupList.clear();
        }
    }

    /** 准备发送文件 */
    public void toSendFile(Request request)throws IOException{
        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.TOSENDFILE);
        FileInfo sendFile = (FileInfo)request.getAttribute("file");
        response.setData("sendFile", sendFile);
        //给文件接收方转发文件发送方的请求
        if(sendFile.getToUser() != null){ //私发文件
            OnlineClientIOCache ioCache = DataBuffer.onlineUserIOCacheMap.get(sendFile.getToUser().getId());
            sendResponse(ioCache, response);
        }else{  //群发文件
            loadGroupMembers();
            for(Long id : DataBuffer.groupMemberMap.keySet()){
                if(sendFile.getFromUser().getId() == id){ continue; }
                sendResponse(DataBuffer.onlineUserIOCacheMap.get(id), response);
            }
        }

    }

    /** 给所有在线客户都发送响应 */
    private void iteratorResponse(Response response) throws IOException {
        for(OnlineClientIOCache onlineUserIO : DataBuffer.onlineUserIOCacheMap.values()){
            ObjectOutputStream oos = onlineUserIO.getOos();
            oos.writeObject(response);
            oos.flush();
        }
    }

    /** 向指定客户端IO的输出流中输出指定响应 */
    private void sendResponse(OnlineClientIOCache onlineUserIO, Response response)throws IOException {
        ObjectOutputStream oos = onlineUserIO.getOos();
        oos.writeObject(response);
        oos.flush();
    }

    /** 向指定客户端IO的输出流中输出指定响应 */
    private static void sendResponse_sys(OnlineClientIOCache onlineUserIO, Response response)throws IOException {
        ObjectOutputStream oos = onlineUserIO.getOos();
        oos.writeObject(response);
        oos.flush();
    }
}
