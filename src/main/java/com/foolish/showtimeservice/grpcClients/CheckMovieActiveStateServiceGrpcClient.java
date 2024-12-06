package com.foolish.showtimeservice.grpcClients;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.examples.lib.ActiveStateRequest;
import net.devh.boot.grpc.examples.lib.ActiveStateResponse;
import net.devh.boot.grpc.examples.lib.CheckMovieActiveStateServiceGrpc;
import org.springframework.stereotype.Service;


@Service
public class CheckMovieActiveStateServiceGrpcClient {

    @GrpcClient("checkActiveStateService")
    private final CheckMovieActiveStateServiceGrpc.CheckMovieActiveStateServiceBlockingStub checkActiveStateServiceBlockingStub;

    public CheckMovieActiveStateServiceGrpcClient() {
      ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9091).usePlaintext().build();
      checkActiveStateServiceBlockingStub = CheckMovieActiveStateServiceGrpc.newBlockingStub(channel);
    }

    public ActiveStateResponse doCheckActiveState(int id) {
      ActiveStateRequest request = ActiveStateRequest.newBuilder().setId(id).build();
      return checkActiveStateServiceBlockingStub.doCheckMovieActiveState(request);
    }
}
