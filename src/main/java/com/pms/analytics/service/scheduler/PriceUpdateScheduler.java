package com.pms.analytics.service.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pms.analytics.dao.PnlDao;
import com.pms.analytics.service.currentPrice.ExternalPriceClient;
import com.pms.analytics.service.currentPrice.RedisPriceCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceUpdateScheduler {

    private final ExternalPriceClient priceClient;
    private final RedisPriceCache priceCache;
    private final PnlDao pnlDao;

    @Scheduled(fixedRate = 10000) // every 2 seconds
    public void refreshPrices() {

        List<String> symbols = pnlDao.findAllActiveSymbols();

        symbols.forEach(symbol ->
            priceClient.fetchPriceAsync(symbol)
                    .subscribe(price -> priceCache.updatePrice(symbol, price))
        );
    }
}

