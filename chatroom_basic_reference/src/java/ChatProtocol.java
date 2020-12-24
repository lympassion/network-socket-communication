/*
 * Copyright 2019-2022 the original author or authors.
 */

public class ChatProtocol {

  /** 服务端口号 */
  public static final int PORT_NUM = 8080;

  /** 消息类型为登录 */
  public static final char CMD_LOGIN = 'A';

  /** 消息类型为私发信息，暂未用上 */
  public static final char CMD_MESG = 'B';

  /** 消息类型为登出 */
  public static final char CMD_QUIT = 'C';

  /** 消息类型为广播（目前所有消息都为广播） */
  public static final char CMD_BCAST = 'D';

  /** 分隔符，用于分隔消息里的不同部分，识别各种信息*/
  public static final int SEPARATOR = '|';

  /**
   * 判断消息体里面是否含有登录名
   * @param message 消息
   * @return 是否含有登录名
   */
  public static boolean isValidLoginName(String message) {
   return message != null && message.length() != 0;
  }
}

