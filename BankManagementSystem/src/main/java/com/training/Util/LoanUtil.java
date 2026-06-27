package com.training.Util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LoanUtil {

    public BigDecimal getInterestRate(BigDecimal loanAmount)
    {
        if (loanAmount.compareTo(BigDecimal.valueOf(100000)) <= 0)
        {
            return BigDecimal.valueOf(8);
        }
        if (loanAmount.compareTo(BigDecimal.valueOf(500000)) <= 0)
        {
            return BigDecimal.valueOf(10);
        }
        if (loanAmount.compareTo(BigDecimal.valueOf(1000000)) <= 0)
        {
            return BigDecimal.valueOf(12);
        }
        return BigDecimal.valueOf(14);
    }
}