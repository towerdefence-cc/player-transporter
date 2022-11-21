package cc.towerdefence.api.playertransporter.config;

import cc.towerdefence.api.service.PlayerTrackerGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlayerTrackerConfig {

    @Bean
    public PlayerTrackerGrpc.PlayerTrackerBlockingStub playerTrackerBlockingStub() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("player-tracker.towerdefence.svc", 9090)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();

        return PlayerTrackerGrpc.newBlockingStub(managedChannel);
    }
}
