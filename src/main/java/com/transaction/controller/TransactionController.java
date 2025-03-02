package com.transaction.controller;

import com.transaction.model.Transaction;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import com.transaction.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
@Slf4j
public class TransactionController {

    @Autowired
    private TransactionService service;

    // 创建交易
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Transaction transaction) {
        log.info("创建交易: {}", transaction);
        return ResponseEntity.ok(service.create(transaction));
    }

    // 分页查询
    @GetMapping
    public ResponseEntity<Page<Transaction>> listAll(@RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                                     @RequestParam(name = "pageSize", required = false, defaultValue = "20") Integer pageSize) {
        log.info("分页查询交易: pageNumber={}, pageSize={}", pageNumber, pageSize);
        return ResponseEntity.ok(service.listAll(PageRequest.of(pageNumber, pageSize)));
    }

    // 删除交易
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        log.info("删除交易: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
