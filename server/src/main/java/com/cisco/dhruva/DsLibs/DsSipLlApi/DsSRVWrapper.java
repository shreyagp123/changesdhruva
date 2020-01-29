// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsUtil.DsBindingInfo;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class DsSRVWrapper {
  public abstract DsBindingInfo getBindingInfo();

  public abstract void setRunningSum(int sum);

  public abstract int getRunningSum();

  public abstract InetAddress getIPAddress() throws UnknownHostException;

  public abstract int getProtocol();

  public abstract int getWeight();

  public abstract int getLevel();

  public abstract String getHostName();

  public abstract int getPort();

  public abstract boolean isEnabled();

  public abstract void setEnabled(boolean flag);
}
