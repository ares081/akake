package com.ares.transport;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ares.codec.protocol.Message;
import com.ares.codec.serialization.Serialization;
import com.ares.codec.serialization.SerializationFactory;
import com.ares.common.RpcRequest;
import com.ares.common.ServiceMeta;
import com.ares.common.config.ClientConfigProperties;
import com.ares.common.constant.Constants;
import com.ares.common.enums.CompressType;
import com.ares.common.enums.MsgType;
import com.ares.common.exception.RpcException;
import com.ares.common.exception.SerializationException;
import com.ares.registry.Registry;
import com.ares.registry.RegistryFactory;

public abstract class AbstractRpcClient extends AbstractRpcChannel implements RpcClient {

  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final int MAX_REQUEST_ID = Integer.MAX_VALUE;
  private final AtomicLong requestId = new AtomicLong(1);

  protected final ClientConfigProperties properties;
  private final Registry registry;

  public AbstractRpcClient(ClientConfigProperties properties) throws Exception {
    super();
    this.properties = properties;
    this.registry = new RegistryFactory(properties.getRegisterProperties()).getRegister();
  }

  protected abstract Object doSend(Message<byte[]> message) throws RpcException;

  @Override
  public void init(String host, Integer port) throws RpcException {
    if (!initialized.compareAndSet(false, true)) {
      logger.warn("Channel already initialized");
    }
    try {
      Objects.requireNonNull(host, "host cannot be null");
      Objects.requireNonNull(port, "port cannot be null");
      if (port <= 0 || port > 65535) {
        throw new IllegalArgumentException("Invalid port number: " + port);
      }
      start(host, port);
      active.set(true);
      logger.info("Client initialized successfully - {}:{}", host, port);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      initialized.set(false);
      active.set(false);
      logger.error("Client initialization failed - {}:{}", host, port, e);
      throw new RpcException("Failed to initialize client", e);
    }
  }

  public ServiceMeta serviceLookup(String group, String serviceName, String serviceVersion)
      throws RpcException {
    try {
      Objects.requireNonNull(serviceName, "serviceName cannot be null");
      return registry.lookup(group, serviceName, serviceVersion, 0);
    } catch (Exception e) {
      logger.error("Service lookup failed - group:{}, service:{}, version:{}",
          group, serviceName, serviceVersion, e);
      throw new RpcException("Service lookup failed", e);
    }
  }

  @Override
  public Object send(ServiceMeta serviceMeta) throws RpcException {
    if (!isActive()) {
      throw new IllegalStateException("Client is closed");
    }
    Objects.requireNonNull(serviceMeta, "serviceMeta cannot be null");
    try {
      Serialization codec = SerializationFactory.getSerializationByName(properties.getCodec());
      long reqId = generateRequestId();
      Message<byte[]> message = buildMessage(reqId, serviceMeta, codec);
      logger.debug("Sending request - id:{}, service:{}", reqId, serviceMeta.getServiceName());
      return doSend(message);
    } catch (Exception e) {
      logger.error("Failed to send request - service:{}", serviceMeta.getServiceName(), e);
      throw new RpcException("Failed to send request", e);
    }
  }

  private Message<byte[]> buildMessage(long reqId, ServiceMeta serviceMeta, Serialization codec)
      throws SerializationException {
    // Build request object
    RpcRequest request = RpcRequest.builder()
        .reqId(reqId)
        .methodName(serviceMeta.getMethodName())
        .serviceName(serviceMeta.getServiceName())
        .serviceVersion(serviceMeta.getVersion())
        .group(serviceMeta.getGroup())
        .params(serviceMeta.getParams())
        .paramTypes(serviceMeta.getParamTypes())
        .build();

    // Serialize request
    byte[] body = codec.serialize(request);

    // Build message
    return Message.<byte[]>builder()
        .magic(Constants.MAGIC)
        .version(Constants.PROTOCOL_VERSION)
        .codec(codec.getSerializationCode())
        .compress(CompressType.NONE.getCode())
        .msgType(MsgType.REQUEST.getCode())
        .state((byte) 0)
        .oneWay((byte) 0)
        .reqId(reqId)
        .bodyLen(body.length)
        .body(body)
        .build();
  }

  private long generateRequestId() {
    long nextId = requestId.getAndIncrement();
    if (nextId >= MAX_REQUEST_ID) {
      requestId.set(1);
      nextId = 1;
    }
    return nextId;
  }

  /**
   * close channel
   */
  @Override
  public void close() {
    if (initialized.compareAndSet(true, false)) {
      try {
        active.set(false);
        if (registry != null) {
          registry.close();
        }
        stop();
        logger.info("Client closed successfully");
      } catch (Exception e) {
        logger.error("Error while closing client", e);
      }
    }
  }
}
