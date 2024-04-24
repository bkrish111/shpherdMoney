package com.shepherdmoney.interviewproject.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditCardView {

    private String issuanceBank;

    private String number;
}
