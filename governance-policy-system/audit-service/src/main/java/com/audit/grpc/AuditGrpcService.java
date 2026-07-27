package com.audit.grpc;

import com.audit.entity.AuditLog;
import com.audit.repository.AuditRepository;
import com.governance.grpc.AuditRequest;
import com.governance.grpc.AuditResponse;
import com.governance.grpc.AuditServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuditGrpcService extends AuditServiceGrpc.AuditServiceImplBase {

    private final AuditRepository auditRepository;

    @Override
    public void logAction(AuditRequest request, StreamObserver<AuditResponse> responseObserver) {
        try {
            log.info("gRPC: Received audit request for policy: {}", request.getPolicyId());

            AuditLog auditLog = new AuditLog();
            auditLog.setEventType(request.getEventType());
            auditLog.setPolicyId(request.getPolicyId());
            auditLog.setActor(request.getActor());

            Instant instant = Instant.parse(request.getTimestamp());
            LocalDateTime timestamp = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            auditLog.setTimestamp(timestamp);

            auditRepository.save(auditLog);

            AuditResponse response = AuditResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Audit log saved successfully via gRPC")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC: Failed to save audit log", e);

            AuditResponse response = AuditResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to save audit log: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}