package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

public final class NetObjectsFactory {

  static NetObjectProviderInterface netObjectProvider;

  private NetObjectsFactory() {}

  public static NetObjectProviderInterface getNetObjectProvider() {
    return netObjectProvider;
  }

  public static void setNetObjectProvider(NetObjectProviderInterface netObjectProvider) {
    NetObjectsFactory.netObjectProvider = netObjectProvider;
  }

  public static InetAddress getByName(String host) throws UnknownHostException {
    if (netObjectProvider != null) {
      return netObjectProvider.getByName(host);
    } else {
      return InetAddress.getByName(host);
    }
  }

  public static InetAddress getLocalHost() throws UnknownHostException {
    if (netObjectProvider != null) {
      return netObjectProvider.getLocalHost();
    } else {
      return InetAddress.getLocalHost();
    }
  }

  public static InetSocketAddress getInetSocketAddress(String hostname, int port) {
    if (netObjectProvider != null) {
      return netObjectProvider.getInetSocketAddress(hostname, port);
    } else {
      return new InetSocketAddress(hostname, port);
    }
  }

  public static InetSocketAddress getInetSocketAddress(InetAddress hostname, int port) {
    if (netObjectProvider != null) {
      return netObjectProvider.getInetSocketAddress(hostname, port);
    } else {
      return new InetSocketAddress(hostname, port);
    }
  }

  @SuppressFBWarnings(value = {"UNENCRYPTED_SOCKET"})
  public static Socket getSocket() {
    if (netObjectProvider != null) {
      return netObjectProvider.getSocket();
    } else {
      return new Socket();
    }
  }

  public static DatagramSocket getDatagramSocket(int port, InetAddress laddr)
      throws SocketException {
    if (netObjectProvider != null) {
      return netObjectProvider.getDatagramSocket(port, laddr);
    } else {
      return new DatagramSocket(port, laddr);
    }
  }

  public static DatagramPacket getDatagramPacket(
      byte[] buf, int length, InetAddress address, int port) {
    if (netObjectProvider != null) {
      return netObjectProvider.getDatagramPacket(buf, length, address, port);
    } else {
      return new DatagramPacket(buf, length, address, port);
    }
  }

  public static InitialDirContext getJndiDnsInitialDirContext(Hashtable env)
      throws NamingException {
    if (netObjectProvider != null) {
      return netObjectProvider.getJndiDnsInitialDirContext(env);
    } else {
      return new InitialDirContext(env);
    }
  }

  public static InputStream getInputStream(String responseStr) throws IOException {
    if (netObjectProvider != null) {
      return netObjectProvider.getInputStream();
    } else {
      return new ByteArrayInputStream(responseStr.getBytes(StandardCharsets.UTF_8));
    }
  }

  // REFACTOR
  //  public static void setRegister(PingObject po) {
  //    if (netObjectProvider != null) {
  //      try {
  //        //				netObjectProvider.setRegister(po);
  //        DsSelector.register(po);
  //      } catch (Exception e) {
  //        // TODO Auto-generated catch block
  //        e.printStackTrace();
  //      }
  //    } else {
  //      //            return DsSocketChannelFactory.getSocketChannel();
  //    }
  //  }

  public static ServerSocketChannel getServerSocketChannel() throws IOException {
    if (netObjectProvider != null) {
      return netObjectProvider.getServerSocketChannel();
    } else {
      return ServerSocketChannel.open();
    }
  }
}
