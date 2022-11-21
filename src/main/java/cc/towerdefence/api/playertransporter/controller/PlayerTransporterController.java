package cc.towerdefence.api.playertransporter.controller;

import cc.towerdefence.api.playertransporter.service.PlayerTransporterService;
import cc.towerdefence.api.service.PlayerTransporterGrpc;
import cc.towerdefence.api.service.PlayerTransporterProto;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Controller;

@GrpcService
@Controller
@RequiredArgsConstructor
public class PlayerTransporterController extends PlayerTransporterGrpc.PlayerTransporterImplBase {
    private final PlayerTransporterService playerTransporterService;

    @Override
    public void commonMovePlayer(PlayerTransporterProto.MoveRequest request, StreamObserver<Empty> responseObserver) {
        this.playerTransporterService.onCommonMovePlayer(request);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void towerDefenceGameMovePlayer(PlayerTransporterProto.TowerDefenceGameMoveRequest request, StreamObserver<Empty> responseObserver) {
        this.playerTransporterService.moveToTowerDefence(request.getPlayerIdsList(), request.getFastJoin());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
