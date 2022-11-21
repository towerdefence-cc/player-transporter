package cc.towerdefence.api.playertransporter.config;

import cc.towerdefence.api.service.ServerDiscoveryGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerDiscoveryConfig {

    @Bean
    public ServerDiscoveryGrpc.ServerDiscoveryBlockingStub serverDiscoveryFutureStub() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("server-discovery.towerdefence.svc", 9090)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();

        return ServerDiscoveryGrpc.newBlockingStub(managedChannel);
    }
}
