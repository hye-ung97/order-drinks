package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Wallet.Card, Integer> {
}
