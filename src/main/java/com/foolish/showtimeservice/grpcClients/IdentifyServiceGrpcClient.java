package com.foolish.showtimeservice.grpcClients;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.examples.lib.IdentifyRequest;
import net.devh.boot.grpc.examples.lib.IdentifyResponse;
import net.devh.boot.grpc.examples.lib.IdentifyServiceGrpc;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class IdentifyServiceGrpcClient {
  @GrpcClient("identifyService")
  private final IdentifyServiceGrpc.IdentifyServiceBlockingStub identifyServiceBlockingStub;

  public IdentifyServiceGrpcClient(Environment env) {
    String name = env.getProperty("AUTH_GRPC_SERVER_NAME");
    int port = Integer.parseInt(Objects.requireNonNull(env.getProperty("AUTH_GRPC_SERVER_PORT")));
    ManagedChannel channel = ManagedChannelBuilder.forAddress(name, port).usePlaintext().build();
    identifyServiceBlockingStub = IdentifyServiceGrpc.newBlockingStub(channel);
  }

  public IdentifyResponse doIdentify(String token) {
    IdentifyRequest request = IdentifyRequest.newBuilder().setToken(token).build();
    ;
    try {
      return identifyServiceBlockingStub.doIdentify(request);
    } catch (RuntimeException e) {
      log.error("RPC failed: {}", String.valueOf(e.getCause()));
      return null;
    }
  }
}
