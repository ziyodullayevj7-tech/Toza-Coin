package teamwork.dto.coinTransaction;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import teamwork.enums.CoinType;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinTransactionDto {
    private Integer amount;
    private CoinType coinType;
    private String description;
}
