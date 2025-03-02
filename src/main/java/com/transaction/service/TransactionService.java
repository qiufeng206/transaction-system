package com.transaction.service;

import com.transaction.config.CacheConfig;
import com.transaction.model.Transaction;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.transaction.exception.DuplicateTransactionException;
import com.transaction.exception.TransactionNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransactionService {
    private final ConcurrentHashMap<String, Transaction> transactions = new ConcurrentHashMap<>();

    private final CacheManager cacheManager; // 新增字段

    // 通过构造函数注入 CacheManager
    public TransactionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // 添加交易（带重复校验）
    public Transaction create(Transaction transaction) {
        if (transactions.containsKey(transaction.getId())) {
            throw new DuplicateTransactionException("Transaction ID重复: " + transaction.getId());
        }
        transactions.put(transaction.getId(), transaction);
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.TRANSACTION_CACHE_NAME)).clear();
        return transaction;
    }

    // 分页查询（带缓存）
    @Cacheable(value = CacheConfig.TRANSACTION_CACHE_NAME, key = "#pageable.pageNumber +'-'+ #pageable.pageSize")
    public Page<Transaction> listAll(Pageable pageable) {
        List<Transaction> list = new ArrayList<>(transactions.values());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        return new PageImpl<>(list.size() > start ? list.subList(start, end) : list, pageable, list.size());
    }

    // 删除交易
    @CacheEvict(value = CacheConfig.TRANSACTION_CACHE_NAME, key = "#id")
    public void delete(String id) {
        if (!transactions.containsKey(id)) {
            throw new TransactionNotFoundException("交易不存在: " + id);
        }
        transactions.remove(id);
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.TRANSACTION_CACHE_NAME)).clear();
    }
}
