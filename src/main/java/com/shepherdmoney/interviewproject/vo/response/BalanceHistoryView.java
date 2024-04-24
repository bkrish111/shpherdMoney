package com.shepherdmoney.interviewproject.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;


@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class BalanceHistoryView {

    private String creditCardNumber;

    private ArrayList<Balance> balance;


}
