package com.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.controller.TransactionController;
import com.transaction.exception.DuplicateTransactionException;
import com.transaction.exception.TransactionNotFoundException;
import com.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    // 测试创建交易API（成功）
    @Test
    void createTransactionAPI_Success() throws Exception {
        Transaction transaction = new Transaction("1", BigDecimal.TEN, "DEPOSIT", "Salary");
        when(transactionService.create(any(Transaction.class))).thenReturn(transaction);
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    // 测试创建交易API（重复ID冲突）
    @Test
    void createTransactionAPI_Duplicate_ReturnsConflict() throws Exception {
        Transaction transaction = new Transaction("1", BigDecimal.TEN, "DEPOSIT", "Salary");
        when(transactionService.create(any(Transaction.class))).thenThrow(new DuplicateTransactionException("重复ID"));
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("重复ID"));
    }

    // 测试分页查询API
    @Test
    void listAllTransactionsAPI_Success() throws Exception {
        // 准备测试数据
        Transaction transaction = new Transaction("1", BigDecimal.TEN, "DEPOSIT", "Salary");

        Pageable pageable = PageRequest.of(0, 10);

        Page<Transaction> page = new PageImpl<>(Collections.singletonList(transaction), pageable, 1);

        // 模拟Service行为
        when(transactionService.listAll(any(Pageable.class))).thenReturn(page);

        // 执行请求并验证结果
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("1"));
    }

    // 测试删除交易API（成功）
    @Test
    void deleteTransactionAPI_Success() throws Exception {
        // 模拟Service行为
        doNothing().when(transactionService).delete("1");

        // 执行请求并验证结果
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transactions/1"))
                .andExpect(status().isNoContent());
    }

    // 测试删除交易API（不存在）
    @Test
    void deleteTransactionAPI_NotFound() throws Exception {
        // 模拟Service行为
        doThrow(new TransactionNotFoundException("交易不存在")).when(transactionService).delete("1");

        // 执行请求并验证结果
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transactions/1"))
                .andExpect(status().isNotFound());
    }
}
