package com.foolish.showtimeservice.service;

import com.foolish.showtimeservice.grpcClients.IdentifyServiceGrpcClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.devh.boot.grpc.examples.lib.IdentifyResponse;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IdentifyService {
  private final IdentifyServiceGrpcClient identifyServiceGrpcClient;

  public IdentifyResponse doIdentify(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    String token = header.substring(7);
    return identifyServiceGrpcClient.doIdentify(token);
  }
}
