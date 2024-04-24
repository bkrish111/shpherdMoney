package com.shepherdmoney.interviewproject.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class Balance{

private LocalDate balanceDate;

private double balanceAmount;
}