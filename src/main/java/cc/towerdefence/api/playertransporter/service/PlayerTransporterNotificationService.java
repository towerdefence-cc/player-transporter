package cc.towerdefence.api.playertransporter.service;

import cc.towerdefence.api.service.PlayerTrackerGrpc;
import cc.towerdefence.api.service.PlayerTrackerProto;
import cc.towerdefence.api.service.ServerDiscoveryProto;
import cc.towerdefence.api.service.velocity.VelocityPlayerTransporterGrpc;
import cc.towerdefence.api.service.velocity.VelocityPlayerTransporterProto;
import com.google.common.collect.Sets;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlayerTransporterNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerTransporterNotificationService.class);

    private final PlayerTrackerGrpc.PlayerTrackerBlockingStub playerTrackerService;
    private final CoreV1Api kubernetesClient;

    public void notifyPlayerTransport(List<String> playerIds, ServerDiscoveryProto.ConnectableServer server) {
        PlayerTrackerProto.GetPlayerServersResponse response = this.playerTrackerService.getPlayerServers(PlayerTrackerProto.PlayersRequest.newBuilder()
                .addAllPlayerIds(playerIds)
                .build());

        // proxy id, player ids
        Map<String, Set<String>> serverPlayers = new HashMap<>();
        for (Map.Entry<String, PlayerTrackerProto.OnlineServer> entry : response.getPlayerServersMap().entrySet()) {
            String proxyId = entry.getValue().getProxyId();
            String playerId = entry.getKey();

            if (serverPlayers.containsKey(proxyId)) {
                serverPlayers.get(proxyId).add(playerId);
            } else {
                serverPlayers.put(proxyId, Sets.newHashSet(playerId));
            }
        }

        for (Map.Entry<String, Set<String>> entry : serverPlayers.entrySet()) {
            this.getServerIpForProxyId(entry.getKey()).ifPresent(proxyIp -> {
                ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(proxyIp, 9090)
                        .usePlaintext()
                        .build();

                VelocityPlayerTransporterGrpc.VelocityPlayerTransporterBlockingStub velocityPlayerTransporterService =
                        VelocityPlayerTransporterGrpc.newBlockingStub(managedChannel);

                velocityPlayerTransporterService.sendToServer(VelocityPlayerTransporterProto.TransportRequest.newBuilder()
                        .addAllPlayerIds(entry.getValue())
                        .setServer(server)
                        .build());
            });
        }
    }

    public Optional<String> getServerIpForProxyId(String proxyId) {
        try {
            V1Pod pod = this.kubernetesClient.readNamespacedPod(proxyId, "towerdefence", null);
            return Optional.ofNullable(pod.getStatus().getPodIP());
        } catch (ApiException e) {
            LOGGER.error("Failed to get pod for proxy id {}:\nK8s Error: ({}) {}\n{}", proxyId, e.getCode(), e.getResponseBody(), e);
            return Optional.empty();
        }
    }
}
