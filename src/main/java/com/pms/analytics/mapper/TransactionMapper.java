package com.pms.analytics.mapper;

import com.pms.analytics.dto.TransactionDto;
import com.pms.analytics.dto.TransactionOuterClass.Transaction;
import com.pms.analytics.dto.TransactionOuterClass.TradeSide;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionMapper {


    public static Transaction toProto(TransactionDto dto) {
        return Transaction.newBuilder()
                .setTransactionId(dto.getTransactionId().toString())
                .setPortfolioId(dto.getPortfolioId().toString())
                .setSymbol(dto.getSymbol())
                .setSide(dto.getSide() != null ? TradeSide.valueOf(dto.getSide().name()) : TradeSide.UNKNOWN)
                .setBuyPrice(dto.getBuyPrice() != null ? dto.getBuyPrice().toString() : "0")
                .setSellPrice(dto.getSellPrice() != null ? dto.getSellPrice().toString() : "0")
                .setQuantity(dto.getQuantity())
                .build();
    }

    public static TransactionDto fromProto(Transaction proto) {
        return new TransactionDto(
                UUID.fromString(proto.getTransactionId()),
                UUID.fromString(proto.getPortfolioId()),
                proto.getSymbol(),
                com.pms.analytics.utilities.TradeSide.valueOf(proto.getSide().name()),
                new BigDecimal(proto.getBuyPrice()),
                new BigDecimal(proto.getSellPrice()),
                proto.getQuantity()
        );
    }
}
