package teamwork.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import teamwork.dto.coinTransaction.CoinTransactionDto;
import teamwork.entity.CoinTransactionEntity;
import teamwork.entity.ProfileEntity;
import teamwork.repository.CoinTransactionRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
public class CoinTransactionService {
    @Autowired
    private CoinTransactionRepository coinTransactionRepository;

    public boolean save(CoinTransactionDto dto, ProfileEntity profile) {
        CoinTransactionEntity entity = new CoinTransactionEntity();
        entity.setProfile(profile);
        entity.setAmount(dto.getAmount());
        entity.setCoinType(dto.getCoinType());
        entity.setDescription(dto.getDescription());
        entity.setCreatedDate(LocalDateTime.now());

        coinTransactionRepository.save(entity);
        log.info("Coin Transaction created");
        return true;
    }
}
