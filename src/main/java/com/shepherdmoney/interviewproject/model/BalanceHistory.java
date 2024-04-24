package com.shepherdmoney.interviewproject.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;


    private LocalDate date;

    private double balance;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "credit_card_id")
    private CreditCard creditCard;

    @Override
    public String toString() {
        return "BalanceHistory{" +
                "id=" + id +
                ", date=" + date +
                ", balance=" + balance +
                '}';
    }
}
