package com.transaction.service;

import com.transaction.exception.DuplicateTransactionException;
import com.transaction.exception.TransactionNotFoundException;
import com.transaction.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    private TransactionService transactionService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionService(cacheManager);
        lenient().when(cacheManager.getCache("transactions")).thenReturn(cache);
    }

    @Test
    void testCreateTransaction_Success() {
        Transaction transaction = new Transaction("1",  BigDecimal.valueOf(100.50), "CREDIT", "FOOD");
        Transaction result = transactionService.create(transaction);
        assertEquals(transaction, result);
    }

    @Test
    void testCreateTransaction_DuplicateId() {
        Transaction transaction = new Transaction("1",  BigDecimal.valueOf(100.00), "CREDIT", "FOOD");
        transactionService.create(transaction);
        assertThrows(DuplicateTransactionException.class, () -> transactionService.create(transaction));
    }

    @Test
    void testListAllTransactions_Success() {
        Transaction transaction1 = new Transaction("1", BigDecimal.valueOf(100.00), "CREDIT", "FOOD");
        Transaction transaction2 = new Transaction("2", BigDecimal.valueOf(200.00), "CREDIT", "FOOD");
        transactionService.create(transaction1);
        transactionService.create(transaction2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> result = transactionService.listAll(pageable);

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().contains(transaction1));
        assertTrue(result.getContent().contains(transaction2));
    }

    @Test
    void testDeleteTransaction_Success() {
        // 准备测试数据
        String id = "1";
        Transaction transaction = new Transaction(id, BigDecimal.valueOf(100.50), "CREDIT", "FOOD");
        transactionService.create(transaction);

        // 执行删除操作
        transactionService.delete(id);

        // 验证交易是否被删除
        assertFalse(transactionService.listAll(PageRequest.of(0, 10)).getContent().contains(transaction));

    }

    @Test
    void testDeleteTransaction_NotFound() {
        // 准备测试数据
        String id = "1";

        // 执行删除操作并验证异常
        assertThrows(TransactionNotFoundException.class, () -> transactionService.delete(id));

        // 验证缓存未被清除
        verify(cache, never()).clear();
    }
}