/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsProxyStatelessTransaction.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.controller.util.ParseProxyParamUtil;
import com.cisco.dhruva.sip.hostPort.HostPortUtil;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;

/**
 * Represents a stateless proxy transaction. Stateless transaction only allows a very limited set of
 * operations to be performed on it, namely, it allows to proxy the request to a single destination
 * and nothing else, really. It will also report errors that occur synchronously with forwarding the
 * request DsProxyTransaction inherits from from StatelessTransaction and defines lots of additional
 * operations
 */
public class DsProxyStatelessTransaction implements DsProxyInterface {

  // internal state constants
  /** The transaction is in the initial state when created */
  private static final int PROXY_INITIAL = 0;
  /** Once request is proxied, the transaction is in DONE state */
  private static final int PROXY_DONE = 1;

  // for NAT Traversal
  public static final DsByteString RPORT = new DsByteString("rport");
  public static final DsByteString RPORT_COOKIE_START = new DsByteString("0000");
  public static final DsByteString RPORT_COOKIE_END = new DsByteString("1111");

  /** is this transaction handles a stray ACK or CANCEL? */
  protected static final int NOT_STRAY = 0;

  protected static final int STRAY_ACK = 1;
  protected static final int STRAY_CANCEL = 2;

  private int strayRequest = NOT_STRAY;

  /** the request received from the initial server transaction */
  private DsSipRequest originalRequest;

  // private DsProxyCookieInterface cookie;

  /**
   * the controller for this ProxyStatelessTransaction In stateless mode, only used to report
   * request forwarding errors
   */
  protected DsControllerInterface controller;

  /**
   * Holds default values for various proxy settings This allows for dynamic proxy reconfiguration
   */
  private DsProxyParamsInterface defaultParams;

  private int state = PROXY_INITIAL;

  private static final int[] Transports_UDP = {
    DsSipTransportType.UDP, DsSipTransportType.TCP, DsSipTransportType.TLS
  };

  private static final int[] Transports_TCP = {
    DsSipTransportType.TCP, DsSipTransportType.UDP, DsSipTransportType.TLS
  };

  private static final int[] Transports_TLS = {
    DsSipTransportType.TLS, DsSipTransportType.UDP, DsSipTransportType.TCP
  };

  /**
   * the total number of branches forked up to now. This is different from branchesOutstanding and
   * is used to compute branch IDs of the branches; this variable is more important for
   * DsProxyTransaction
   */
  protected int n_branch = 0;

  private static final Logger Log =
      DhruvaLoggerFactory.getLogger(DsProxyStatelessTransaction.class);

  //  private static final boolean m_simpleResolver =
  // DsConfigManager.getProperty(DsConfigManager.PROP_SIMPLE_RESOLVER,
  //                DsConfigManager.PROP_SIMPLE_RESOLVER_DEFAULT);

  // some private strings
  private static final String colon = ":";

  /** To be used with object pooling only */
  public DsProxyStatelessTransaction() {}

  public DsProxyStatelessTransaction(
      DsControllerInterface controller, DsProxyParamsInterface config, DsSipRequest request)
      throws DsInternalProxyErrorException {
    init(controller, config, request);
  }

  /** Used for object pooling */
  public synchronized void init(
      DsControllerInterface controller, DsProxyParamsInterface config, DsSipRequest request) {

    this.controller = controller;
    originalRequest = request;

    state = PROXY_INITIAL;
    defaultParams = config; // save the configuration

    switch (request.getMethodID()) {
      case DsSipConstants.ACK:
        strayRequest = STRAY_ACK;
        break;
      case DsSipConstants.CANCEL:
        strayRequest = STRAY_CANCEL;
        break;
      default:
        strayRequest = NOT_STRAY;
        break;
    }

    n_branch = 0;

    Log.info("ProxyStatelessTransaction created");
  }

  /**
   * This method allows a controller to proxy to a specified URL. The proxy will take the received
   * request, perform the appropriate header processing, and send it
   *
   * @param url Url to proxy to
   */
  public synchronized void proxyTo(DsSipURL url, DsProxyCookieInterface cookie) {
    proxyTo(url, cookie, null);
  }

  /**
   * This method allows a controller to proxy to a specified URL. The proxy will take the received
   * request, perform the appropriate header processing, and send it
   *
   * @param url Url to proxy to
   */
  public synchronized void proxyTo(
      DsURI url, DsProxyCookieInterface cookie, DsProxyBranchParamsInterface params) {

    Log.debug("Entering DsProxyStatelessTransaction outer proxyTo()");

    // clone the original request and URL?
    DsSipRequest msg = cloneRequest();
    DsURI urlClone = cloneURI(url);

    if (urlClone.isSipURL()) processURLHeaders((DsSipURL) urlClone, msg);

    try {
      msg.setURI(urlClone); // !!!!!!!!!!!!
    } catch (Exception ignored) {
    }

    proxyTo(msg, cookie, params);
  }

  /**
   * This method allows the controller to proxy to a specified URL the code will not check to make
   * sure the controller is not adding or removing critical headers like To, From, Call-ID.
   *
   * @param request request to send
   */
  public synchronized void proxyTo(DsSipRequest request, DsProxyCookieInterface cookie) {
    proxyTo(request, cookie, null);
  }

  /**
   * This method allows the controller to proxy to a specified URL using specified parameters the
   * code will not check to make sure the controller is not adding or removing critical headers like
   * To, From, Call-ID.
   *
   * @param request request to send
   * @param params extra params to set for this branch
   */
  public synchronized void proxyTo(
      DsSipRequest request, DsProxyCookieInterface cookie, DsProxyBranchParamsInterface params) {

    Log.debug("Entering DsProxyStatelessTransaction proxyTo()");

    if (state != PROXY_INITIAL && strayRequest == NOT_STRAY) {
      controller.onProxyFailure(
          this,
          cookie,
          DsControllerInterface.INVALID_PARAM,
          "Cannot fork stateless transaction!",
          null);
    }

    try {
      prepareRequest(request, params);

      Log.debug("proxying request");

      DsByteString conid = null;

      if (request.getBindingInfo() != null) {
        conid = request.getBindingInfo().getConnectionId();
      }

      DsSipConnection ret_connection = null;

      if (conid != null) {
        ret_connection = DsSipConnectionAssociations.getConnection(conid);
      }
      //      if (ret_connection == null) {
      //        if (DsConfigManager.getSimpleResolverDefault()) {
      //          DsSipSimpleResolver simpleResolver = new DsSipSimpleResolver(request);
      //          ret_connection = DsSipTransactionManager.getRequestConnection(request,
      // simpleResolver);
      //          if (ret_connection == null) {
      //            ret_connection = simpleResolver.tryConnect();
      //          }
      //        } else {
      //          //  Create a "deterministic" server locator. For stateless transactions,
      //          //  for a given a URI, we want to consistently return to the same server.
      //          // !!!!!!!!!!!!!!!!!!
      //          // this is a cut&paste from Low Level and must be changed
      //          // in the next release
      //          DsSipServerLocator server_locator = new DsSipDetServerLocator(request);
      //          ret_connection = DsSipTransactionManager.getRequestConnection(request,
      // server_locator);
      //          if (ret_connection == null) {
      //            ret_connection = server_locator.tryConnect();
      //          }
      //        }
      //      }
      if (ret_connection == null) {
        try {
          ret_connection = DsSipTransactionManager.getRequestConnection(request, null);
        } catch (Exception e) {
          throw new IOException("failed to find connection");
        }
      }

      // update Via based on the protocol of the Connection obtained
      DsSipViaHeader via = request.getViaHeaderValidate();
      Transport connection_protocol = ret_connection.getTransportType();
      if (via.getTransport() != connection_protocol) {
        Log.debug("Replacing protocol in Via");

        DsViaListenInterface lif =
            getDefaultParams().getViaInterface(connection_protocol, request.getNetwork().getName());
        via.setTransport(connection_protocol);
        via.setPort(lif.getPort());
        via.setHost(lif.getAddress());

        forceRequestSource(request, lif.getSourcePort(), lif.getSourceAddress());
      }

      ret_connection.send(request);

      // Since we're sending directly on the connection, we have to log the request ourselves.
      // Fix for CR7834
      int reason =
          getStrayStatus() == NOT_STRAY
              ? DsMessageLoggingInterface.REASON_REGULAR
              : DsMessageLoggingInterface.REASON_STRAY;

      state = PROXY_DONE;

    } catch (IOException e) {
      controller.onProxyFailure(
          this, cookie, DsControllerInterface.DESTINATION_UNREACHABLE, e.getMessage(), e);
      return;
    } catch (DsException e) {
      Log.error("Got DsException in proxyTo()!", e);
      controller.onProxyFailure(
          this, cookie, DsControllerInterface.INVALID_PARAM, e.getMessage(), e);
      return;
    }

    controller.onProxySuccess(this, cookie, null);

    Log.debug("Leaving DsProxyStatelessTransaction  proxyTo()");
  }

  /**
   * Forces the specified source IP address and port onto the request to be forwarded
   *
   * @param request request to be forwarded
   * @param sourcePort source port to force; if <=0, the source port won't be forced
   * @param sourceAddress source address to force; if null, the source address won't be forced
   */
  protected void forceRequestSource(
      DsSipRequest request, int sourcePort, InetAddress sourceAddress) {

    // source port is allowed to take 0 . This would allow
    // to bind to random ports on fly and send the request out.
    // otherwise it is not possible to use a fixed port to
    // send it to different address...Fixed for OATS

    if (sourcePort >= 0) request.setLocalBindingPort(sourcePort);

    if (sourceAddress != null) request.setLocalBindingAddress(sourceAddress);
  }

  protected void processURLHeaders(DsSipURL url, DsSipRequest request) {

    DsByteString hname = null, hvalue = null;

    Log.debug("processing URL headers");

    try {
      if (url.hasHeaders()) {

        Log.debug("Found URL headers in " + url);

        DsParameters headers = url.getHeaders();

        // HACK !?!
        Iterator iter = headers.iterator();
        DsParameter entry;

        while (iter.hasNext()) {
          entry = (DsParameter) iter.next();
          hname = entry.getKey();
          hvalue = entry.getValue();

          Log.debug("processing URL header " + hname + ", value " + hvalue);

          request.addHeaders(constructHeaderList(hname, hvalue), false);
          iter.remove();
        }
      }
    } catch (Exception e) {
      Log.error("Exception parsing URL header: " + hname + "=" + hvalue, e);
    }
  }

  private DsSipHeaderList constructHeaderList(DsByteString hname, DsByteString hvalue)
      throws DsException {
    int type = DsSipMsgParser.getMethod(hname);
    return DsSipHeader.createHeaderList(type, hvalue.data(), hvalue.offset(), hvalue.length());
  }

  public synchronized void addProxyRecordRoute(
      DsSipRequest request, DsProxyBranchParamsInterface params) {
    if (!params.doRecordRoute()) {
      return;
    }
    addRecordRoute(request, getOriginalRequest().getURI(), params);
  }

  /**
   * This is an internal implementation of proxying code. It's necessary to allow easy subclassing
   * in DsProxyTransaction, which will need to create DsProxyClientTransaction etc.
   *
   * @param request request to send
   * @param params extra params to set for this branch
   */
  protected synchronized void prepareRequest(
      DsSipRequest request, DsProxyBranchParamsInterface params)
      throws DsInvalidParameterException {
    Log.debug("Entering prepareRequest()");

    DsSipRouteHeader route;

    if (params == null) params = getDefaultParams();

    // make sure that we know how to handle this URL
    try {
      route = (DsSipRouteHeader) request.getHeaderValidate(DsSipRouteHeader.sID);
    } catch (DsException e) {
      Log.error("Error getting Route header", e);
      route = null;
    }
    if (route == null) {
      if (!request.getURI().isSipURL()) {
        if (params.getProxyToAddress() == null
            || params.getProxyToPort() <= 0
            || params.getProxyToProtocol() == Transport.NONE)
          throw new DsInvalidParameterException("Cannot proxy non-SIP URL!");
      }
    }

    // determine destination based on transport params etc
    Transport destTransport = Transport.NONE;

    destTransport = setRequestDestination(request, params);

    if (processVia()) {
      // invoke branch constructor with the URL and
      // add a Via field with this branch
      DsByteString branch =
          DsViaHandler.getInstance().getBranchID(++n_branch, getOriginalRequest());
      Log.debug("branch=" + branch);

      // The following block fixes up Via protocol in case Route is used
      DsSipViaHeader via; // !!!!!!!!!!!!!!! double check this is correct
      int viaTransport = DsSipTransportType.UDP;

      if (route == null) viaTransport = destTransport.getValue();
      else {
        if (route.getURI().isSipURL()) {
          DsSipURL routeURI = (DsSipURL) route.getURI();
          viaTransport =
              routeURI.hasTransport()
                  ? routeURI.getTransportParam().getValue()
                  : ParseProxyParamUtil.getNetworkTransport(request.getNetwork()).getValue();
          request.getBindingInfo().setTransport(Transport.valueOf(viaTransport).get());
        }
      }

      // get the listen interface to put into Via.
      // Note that it doesn't really matter what we put in there
      // since the Low Level will try to update it when doing SRV
      DsViaListenInterface listenIf =
          getPreferredListenIf(viaTransport, request.getNetwork().getName());
      Log.debug("Got interface " + listenIf + " for transport protocol " + viaTransport);

      if (listenIf != null) viaTransport = listenIf.getProtocol().getValue();

      assert listenIf != null;

      // if 'attachExternalIP' toggle is enabled, pass hostIp
      // else pass actual Ip address
      via =
          new DsSipViaHeader(
              HostPortUtil.convertLocalIpToHostInfo(listenIf), listenIf.getPort(), viaTransport);

      forceRequestSource(request, listenIf.getSourcePort(), listenIf.getSourceAddress());

      // !!!!!!!!!! The above must be changed to allow reuse of client
      // TCP connections for responses!!!!
      // !!!!!! Also, the Via will need to be updated for SRV stuff!!!

      if (via.hasParameter(RPORT)) {
        branch = setRPORTCookie(request.getBindingInfo(), branch);
      }

      via.setBranch(branch);

      if (request.shouldCompress()) {
        via.setComp(DsSipConstants.BS_SIGCOMP);
      } else {
        DsTokenSipDictionary tokDic = request.shouldEncode();
        if (null != tokDic) via.setParameter(DsTokenSipConstants.s_TokParamName, tokDic.getName());
      }
      request.addHeader(via, true, false);

      Log.info("Via field added: " + via);
    }

    Log.debug("Leaving prepareRequest()");
  }

  protected DsViaListenInterface getPreferredListenIf(int protocol, String direction) {
    Log.debug("Entering getPreferredListenIf()");
    int[] transports;
    switch (protocol) {
      case DsSipTransportType.UDP:
        transports = Transports_UDP;
        break;
      case DsSipTransportType.TCP:
        transports = Transports_TCP;
        break;
      case DsSipTransportType.TLS:
        transports = Transports_TLS;
        break;
      case DsSipTransportType.NONE:
        // defaulting to UDP
        transports = Transports_UDP;
        break;
      default:
        transports = Transports_UDP;
        Log.warn("Unknown transport requested for Via: " + protocol);
        break;
    }

    DsViaListenInterface listenIf = null;
    for (int transport : transports) {
      listenIf = getDefaultParams().getViaInterface(Transport.valueOf(transport).get(), direction);
      if (listenIf != null) break;
    }
    Log.debug("Leaving getPreferredListenIf(), returning " + listenIf);
    return listenIf;
  }

  /**
   * Looks at the ProxyParams and Request URI and sets request's destination (through
   * setConnection*) based on them
   *
   * @param request request to be forwarded
   * @param params BranchParamsInterface object
   * @return transport protocol (UDP or TCP) to be tried
   */
  protected Transport setRequestDestination(
      DsSipRequest request, DsProxyBranchParamsInterface params) {
    Log.debug("Entering setRequestDestination()");
    Transport destTransport = Transport.NONE;
    DsByteString destAddress = null;
    int destPort = -1; // , destTransport = DsSipTransportType.NONE;
    String direction;

    if (params.getProxyToAddress() != null) {
      destAddress = params.getProxyToAddress();
    }

    if (params.getProxyToPort() > 0) {
      destPort = params.getProxyToPort();
    }

    if (params.getProxyToProtocol() != Transport.NONE) {
      destTransport = params.getProxyToProtocol();
    }

    DsBindingInfo binfo = request.getBindingInfo();
    Log.debug("Binding Info is " + binfo);
    if (destAddress != null) {
      try {
        binfo.setRemoteAddress(destAddress.toString());
        Log.info("Request destination address is set to " + destAddress);
      } catch (Exception e) {
        Log.error("Cannot set destination address!", e);
      }
    }

    if (destPort > 0) {
      binfo.setRemotePort(destPort);

      Log.info("Request destination port is set to " + destPort);
    }

    if (destTransport == Transport.NONE) {
      destTransport = getDefaultParams().getDefaultProtocol(); // for Via!!!
    } else {
      binfo.setTransport(destTransport);
      Log.info("Request destination transport is set to " + destTransport);
    }

    direction = params.getRequestDirection();
    if (binfo.getNetwork() == null) {
      binfo.setNetwork(DsNetwork.findNetwork(direction));
    }

    Log.debug("Leaving setRequestDestination()");
    return destTransport;
  }

  /** @return the default configuration settings used by this ProxyTransaction */
  protected DsProxyParamsInterface getDefaultParams() {
    return defaultParams;
  }

  /**
   * @return STRAY_ACK if the request is a stray ACK, STRAY_CANCEL if the request is a stray CANCEL
   *     NOT_STRAY - otherwise
   */
  protected int getStrayStatus() {
    return strayRequest;
  }

  /**
   * @return the original request that caused the creation of this transaction This method is not
   *     strictly necessary but it makes application's life somewhat easier as the application is
   *     not required to save the request for later reference NOTE: modifying this request will have
   *     unpredictable consequences on further operation of this transaction
   */
  public DsSipRequest getOriginalRequest() {
    return originalRequest;
  }
  /*
    protected void addRecordRoute(DsSipRequest request, DsURI _requestURI, int protocol) {
      addRecordRoute(request, _requestURI, protocol, DsProxyParamsInterface.LISTEN_INTERNAL);
    }
  */
  // REDDY_RR_CHANGE
  // Insert itself in Record-Route if required
  protected void addRecordRoute(
      DsSipRequest request, DsURI _requestURI, DsProxyBranchParamsInterface params) {
    Log.debug("Entering addRecordRoute()");
    if (request.getMethodID() == DsSipConstants.INVITE
        || request.getMethodID() == DsSipConstants.SUBSCRIBE
        || request.getMethodID() == DsSipConstants.NOTIFY) {

      DsSipRecordRouteHeader rr =
          getDefaultParams().getRecordRouteInterface(request.getNetwork().getName());

      if (rr != null) {
        DsSipURL rrURL = (DsSipURL) rr.getURI();
        boolean cloned = false;
        if (_requestURI.isSipURL()) {
          DsSipURL url = (DsSipURL) _requestURI;
          if (url.isSecure()) {
            rr = (DsSipRecordRouteHeader) rr.clone();
            cloned = true;
            rrURL = (DsSipURL) rr.getURI();
            rrURL.setSecure(true);
          }
        }

        if (request.shouldCompress()) {
          if (!cloned) rr = (DsSipRecordRouteHeader) rr.clone();
          cloned = true;
          rrURL = (DsSipURL) rr.getURI();
          rrURL.setCompParam(DsSipConstants.BS_SIGCOMP);
        }
        DsTokenSipDictionary tokDic = request.shouldEncode();
        if (null != tokDic) {
          if (!cloned) rr = (DsSipRecordRouteHeader) rr.clone();
          cloned = true;
          rrURL = (DsSipURL) rr.getURI();
          rrURL.setParameter(DsTokenSipConstants.s_TokParamName, tokDic.getName());
        } else {
          if (rrURL.hasParameter(DsTokenSipConstants.s_TokParamName)) {
            if (!cloned) rr = (DsSipRecordRouteHeader) rr.clone();
            cloned = true;
            rrURL = (DsSipURL) rr.getURI();
            rrURL.removeParameter(DsTokenSipConstants.s_TokParamName);
          }
        }
        DsSipURL uri = (DsSipURL) rr.getURI();
        uri.setUser(params.getRecordRouteUserParams());

        // replace Record-Route localIP with externalIP for public network
        uri.setHost(HostPortUtil.convertLocalIpToHostInfo(uri));

        Log.info("Adding " + rr);
        request.addHeader(rr, true, false);
      }
    }
  }

  /**
   * This is a utility methods that creates a copy of the request to make sure that forking does not
   * get broken
   *
   * <p>It is a NOOP for stateless transaction because no forking can be done if we are stateless
   */
  protected DsSipRequest cloneRequest() {

    // 	// HACK!!! Kludge to work around a certain issue in Low Level
    // 	originalRequest.setBindingInfo(new DsBindingInfo());
    // 	return originalRequest;

    DsSipRequest clone = (DsSipRequest) originalRequest.clone();

    // Added - JPS
    clone.setBindingInfo(new DsBindingInfo());

    return clone;
  }

  /**
   * This is a utility methods that creates a copy of the URL to make sure that forking does not get
   * broken
   */
  protected DsURI cloneURI(DsURI url) {

    return url;
  }

  public boolean processVia() {
    return true;
  }

  /**
   * Extracts a propritary cookie from the branch parameter of this server's Via header. The cookie
   * is at the end of the branch and is delimited by <code>RPORT_COOKIE_START</code> and <code>
   * RPORT_COOKIE_END</code>.
   *
   * @param branch The branch parameter of a via
   * @return The rport cookie formatted as <code>&lt;ip&gt;:&lt;port&gt;</code>
   * @see DsSipProxyManager#proxyStrayResponse(DsSipResponse)
   */
  public static DsByteString getRPORTCookie(DsByteString branch) {
    Log.debug("Entering getRPORTCookie(" + branch + ")");
    DsByteString rportCookie = null;
    if (branch != null) {
      if (branch.endsWith(RPORT_COOKIE_END)) {
        int i = -1;
        int lastIndex = -1;
        while ((i = branch.indexOf(RPORT_COOKIE_START, lastIndex + 1)) != -1) {
          lastIndex = i;
        }
        rportCookie =
            branch.substring(
                lastIndex + RPORT_COOKIE_START.length(),
                branch.length() - RPORT_COOKIE_END.length());
      }
    }
    Log.debug("Leaving getRPORTCookie(), returning " + rportCookie);
    return rportCookie;
  }

  /**
   * Appends the rport cookie to the given Via branch id.
   *
   * @param bindingInfo The binding info from the request. The address from <code>
   *     DsBindingInfo.getLocalAddress()</code> and the port from <code>DsBindingInfo.getLocalPort()
   *     </code> are used to create the rport cookie.
   * @param branch The current branch id of this server's Via header. This object is not modified. A
   *     copy of the <code>DsByteString</code> is created and all modifications are performed on the
   *     copy.
   * @return the modified branch id consisting of the original branch followed by <code>
   *     RPORT_COOKIE_START</code>&lt;ip&gt;:&lt;port&gt;<code>RPORT_COOKIE_END</code>
   */
  public static DsByteString setRPORTCookie(DsBindingInfo bindingInfo, DsByteString branch) {
    Log.debug("Entering setRPORTCookie(" + bindingInfo + ", " + branch + ")");
    DsByteString newBranch = branch.copy();
    String localAddress = bindingInfo.getLocalAddress().getHostAddress();
    int port = bindingInfo.getLocalPort();
    StringBuffer rportCookie = new StringBuffer(localAddress);
    rportCookie.append(colon).append(port);
    DsByteString rportCookieBS = new DsByteString(rportCookie);
    Log.debug("Adding the rport cookie " + rportCookie + " to the branch id for NAT traversal");

    newBranch = newBranch.append(RPORT_COOKIE_START);
    newBranch = newBranch.append(rportCookieBS);
    newBranch = newBranch.append(RPORT_COOKIE_END);
    Log.debug("Leaving setRPORTCookie(), returning " + newBranch);
    return newBranch;
  }
}
