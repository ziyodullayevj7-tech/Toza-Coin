package teamwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamwork.entity.CoinTransactionEntity;

public interface CoinTransactionRepository extends JpaRepository<CoinTransactionEntity, String> {
}
