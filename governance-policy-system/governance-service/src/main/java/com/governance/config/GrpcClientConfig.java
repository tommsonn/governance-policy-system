package com.governance.config;

import com.governance.grpc.AuditServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean
    public AuditServiceGrpc.AuditServiceBlockingStub auditServiceStub(
            @Value("${grpc.client.audit-service.address}") String target) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        return AuditServiceGrpc.newBlockingStub(channel);
    }
}