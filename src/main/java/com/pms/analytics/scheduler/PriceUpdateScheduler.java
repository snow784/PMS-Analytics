package com.pms.analytics.scheduler;

import java.util.List;

import com.pms.analytics.dao.AnalysisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pms.analytics.dao.TransactionsDao;
import com.pms.analytics.externalRedis.ExternalPriceClient;
import com.pms.analytics.externalRedis.RedisPriceCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceUpdateScheduler {

    @Autowired
    ExternalPriceClient priceClient;

    @Autowired
    RedisPriceCache priceCache;

    @Autowired
    AnalysisDao analysisDao;

    @Scheduled(fixedRate = 2000)
    public void refreshPrices() {

        List<String> symbols = analysisDao.findAllSymbols();
        if (symbols.isEmpty()) return;

        symbols.forEach(symbol ->
            priceClient.fetchPriceAsync(symbol)
                    .subscribe(price -> priceCache.updatePrice(symbol, price))
        );
    }
}
