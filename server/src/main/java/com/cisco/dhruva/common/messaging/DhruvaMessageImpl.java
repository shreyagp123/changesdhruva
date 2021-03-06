package com.cisco.dhruva.common.messaging;

import com.cisco.dhruva.common.CallType;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageHeaders;
import com.cisco.dhruva.util.log.LogContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DhruvaMessageImpl implements Serializable, IDhruvaMessage {

  protected final ExecutionContext context;
  protected MessageHeaders headers;
  private MessageBody messageBody;
  private String sessionID;
  private String correlationID;
  private CallType callType;
  private String reqURI;

  private boolean hasBody;
  private boolean isMidCall;
  private boolean isRequest;
  private String network;
  private LogContext loggingContext;

  public DhruvaMessageImpl(ExecutionContext context, MessageHeaders headers, MessageBody payload) {
    this.context = context == null ? new ExecutionContext() : context;
    this.headers = headers == null ? new MessageHeaders(new HashMap<>()) : headers;
    this.messageBody = payload;
  }

  @Override
  public ExecutionContext getContext() {
    return this.context;
  }

  @Override
  public MessageHeaders getHeaders() {
    return headers;
  }

  @Override
  public void setHeaders(MessageHeaders newHeaders) {
    this.headers = newHeaders;
  }

  @Override
  public void setHasBody(boolean hasBody) {
    this.hasBody = hasBody;
  }

  @Override
  public MessageBody getMessageBody() {
    return messageBody;
  }

  @Override
  public void setMessageBody(MessageBody body) {
    this.messageBody = body;
  }

  @Override
  public boolean hasBody() {
    return hasBody;
  }

  @Override
  public Map<String, String> getProperties() {
    return null;
  }

  @Override
  public void setProperties(Map<String, String> properties) {}

  @Override
  public String getCorrelationId() {
    return this.correlationID;
  }

  @Override
  public void setCorrelationId(String correlationId) {
    this.correlationID = correlationId;
  }

  @Override
  public String getSessionId() {
    return sessionID;
  }

  @Override
  public void setSessionId(String sessionId) {
    this.sessionID = sessionId;
  }

  @Override
  public String getReqURI() {
    return this.reqURI;
  }

  @Override
  public void setCallType(CallType callType) {
    this.callType = callType;
  }

  @Override
  public CallType getCallType() {
    return this.callType;
  }

  @Override
  public void setReqURI(String reqURI) {
    this.reqURI = reqURI;
  }

  @Override
  public boolean isMidCall() {
    return isMidCall;
  }

  @Override
  public void setMidCall(boolean isMidCall) {
    this.isMidCall = isMidCall;
  }

  @Override
  public boolean isRequest() {
    return isRequest;
  }

  @Override
  public void setRequest(boolean isRequest) {
    this.isRequest = isRequest;
  }

  @Override
  public void setNetwork(String network) {
    this.network = network;
  }

  @Override
  public String getNetwork() {
    return network;
  }

  @Override
  public IDhruvaMessage clone() {
    final DhruvaMessageImpl copy = new DhruvaMessageImpl(context.clone(), headers, messageBody);
    return copy;
  }

  @Override
  public LogContext getLogContext() {
    return this.loggingContext;
  }

  @Override
  public void setLoggingContext(LogContext loggingContext) {
    this.loggingContext = loggingContext;
  }
}
