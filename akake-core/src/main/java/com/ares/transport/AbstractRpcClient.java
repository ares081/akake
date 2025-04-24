package com.ares.transport;

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
import com.ares.registry.Registry;
import com.ares.registry.RegistryFactory;

public abstract class AbstractRpcClient extends AbstractRpcChannel implements RpcClient {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

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
  public void init(String host, Integer port) {
    try {
      start(host, port);
    } catch (InterruptedException e) {
      logger.error("clietn start failed, host:{}, port:{}", host, port);
    }
  }

  public ServiceMeta servieLookup(String group, String serviceName, String serviceVersion) throws RpcException {
    try {
      return registry.lookup(group, serviceName, serviceVersion, 0);
    } catch (Exception e) {
      logger.error("service register failed : {}", e.getMessage());
      throw new RpcException(e);
    }
  }

  @Override
  public Object send(ServiceMeta serviceMeta) throws RpcException {
    Serialization codec = SerializationFactory.getSerializationByName(properties.getCodec());
    long reqId = requestId.getAndIncrement();

    // 构建协议
    Message<byte[]> message = Message.<byte[]>builder()
        .magic(Constants.MAGIC)
        .version(Constants.PROTOCOL_VERSION)
        .codec(codec.getSerializationCode())
        .compress(CompressType.NONE.getCode())
        .msgType(MsgType.REQUEST.getCode())
        .state((byte) 0)
        .oneWay((byte) 0)
        .reqId(reqId)
        .build();

    // 构建请求参数
    RpcRequest request = RpcRequest.builder()
        .reqId(reqId)
        .methodName(serviceMeta.getMethodName())
        .serviceName(serviceMeta.getServiceName())
        .serviceVersion(serviceMeta.getVersion())
        .group(serviceMeta.getGroup())
        .params(serviceMeta.getParams())
        .paramTypes(serviceMeta.getParamTypes())
        .build();
    byte[] body = codec.serialize(request);
    message.setBodyLen(body.length);
    message.setBody(body);
    return doSend(message);
  }

  /**
   * close channel
   */
  @Override
  public void close() {

  }
}
