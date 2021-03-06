/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsLoopCheckInterface.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

/** An interface used to do loop detection in Proxy Core. */
public interface DsLoopCheckInterface {

  /**
   * Checks if the request is looped, normally by examining the list of Via headers and request URI
   * but other algorithms may also be implemented
   *
   * @param request the SIP request in question
   */
  boolean isLooped(DsSipRequest request);

  /**
   * returns the branch ID to be used in the Via inserted by the proxy core.
   *
   * @param n_branch an integer representing the branch of this request (starts with 1 and gets
   *     incremented every time we fork)
   * @param request request being forwarded
   */
  DsByteString getBranchID(int n_branch, DsSipRequest request);
}
