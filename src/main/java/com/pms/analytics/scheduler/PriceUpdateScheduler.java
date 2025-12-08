package com.pms.analytics.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pms.analytics.dao.TransactionsDao;
import com.pms.analytics.externalRedis.ExternalPriceClient;
import com.pms.analytics.externalRedis.RedisPriceCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceUpdateScheduler {

    private final ExternalPriceClient priceClient;
    private final RedisPriceCache priceCache;
    private final TransactionsDao transactionsDao;

    @Scheduled(fixedRate = 10000)
    public void refreshPrices() {

        List<String> symbols = transactionsDao.findAllActiveSymbols();
        if (symbols.isEmpty()) return;

        symbols.forEach(symbol ->
            priceClient.fetchPriceAsync(symbol)
                    .subscribe(price -> priceCache.updatePrice(symbol, price))
        );
    }
}
