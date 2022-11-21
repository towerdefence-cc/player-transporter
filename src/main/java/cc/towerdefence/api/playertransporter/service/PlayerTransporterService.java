package cc.towerdefence.api.playertransporter.service;

import cc.towerdefence.api.service.PlayerTransporterProto;
import cc.towerdefence.api.service.ServerDiscoveryGrpc;
import cc.towerdefence.api.service.ServerDiscoveryProto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// todo check for party information
@Service
@RequiredArgsConstructor
public class PlayerTransporterService {
    private final PlayerTransporterNotificationService notificationService;
    private final ServerDiscoveryGrpc.ServerDiscoveryBlockingStub serverDiscoveryService;

    public void onCommonMovePlayer(PlayerTransporterProto.MoveRequest request) {
        List<String> playerIds = request.getPlayerIdsList();
        PlayerTransporterProto.RestrictedServerType serverType = request.getServerType();

        switch (serverType) {
            case LOBBY -> {
                ServerDiscoveryProto.LobbyServer server = this.serverDiscoveryService.getSuggestedLobbyServer(
                        ServerDiscoveryProto.ServerRequest.newBuilder().setPlayerCount(playerIds.size()).build()
                );
                this.notificationService.notifyPlayerTransport(playerIds, server.getConnectableServer());
            }
        }
    }

    public void moveToTowerDefence(List<String> playerIds, boolean inProgress) {
        ServerDiscoveryProto.ConnectableServer serverResponse = this.serverDiscoveryService.getSuggestedTowerDefenceServer(
                ServerDiscoveryProto.TowerDefenceServerRequest.newBuilder()
                        .setServerRequest(ServerDiscoveryProto.ServerRequest.newBuilder()
                                .setPlayerCount(playerIds.size())
                                .build()
                        )
                        .setInProgress(inProgress)
                        .build());

        this.notificationService.notifyPlayerTransport(playerIds, serverResponse);
    }
}
