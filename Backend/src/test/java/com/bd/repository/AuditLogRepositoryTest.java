package com.bd.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import com.bd.model.AuditLog;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryTest {

    @Mock private AuditLogRepository auditLogRepository;

    @Test
    void mockRepositoryWorks() {
        AuditLog log = new AuditLog();
        log.setAction("TEST");
        when(auditLogRepository.save(any())).thenReturn(log);
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(log));

        AuditLog saved = auditLogRepository.save(log);
        assertEquals("TEST", saved.getAction());
        assertFalse(auditLogRepository.findAllByOrderByTimestampDesc().isEmpty());
    }
}
