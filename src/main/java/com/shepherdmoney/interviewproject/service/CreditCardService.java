package com.shepherdmoney.interviewproject.service;

import com.shepherdmoney.interviewproject.exception.InValidRequestException;
import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.BalanceHistoryRepository;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.Balance;
import com.shepherdmoney.interviewproject.vo.response.BalanceHistoryView;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Service
public class CreditCardService {
    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;
    public Integer addCreditCardToUser(AddCreditCardToUserPayload payload) {

        Optional<User> optionalUser = userRepository.findById(payload.getUserId());

        return optionalUser.map(user -> creditCardRepository.save(CreditCard.builder()
                .owner(user)
                .number(payload.getCardNumber())
                .issuanceBank(payload.getCardIssuanceBank())
                .build()).getId())
                .orElseThrow(() -> new InValidRequestException("user-not-found",
                String.format("given user with userId=%d does not exists",
                        payload.getUserId()), HttpStatus.BAD_REQUEST));

    }

    public List<CreditCardView> getAllCardOfUser(int userId) {

        Optional<User> optionalUser = userRepository.findById(userId);
        List<CreditCardView> creditCardViews = new ArrayList<>();
        if(optionalUser.isPresent()){

            List<CreditCard> creditCards = optionalUser.get().getCreditCards();
            creditCards.forEach(creditCard -> {
                creditCardViews.add(CreditCardView.builder().issuanceBank(creditCard.getIssuanceBank()).number(creditCard.getNumber()).build());
            });
        }

        return creditCardViews;
    }

    public Integer getUserIdForCreditCard(String creditCardNumber) {

        Optional<CreditCard> optionalCreditCard= creditCardRepository.findByNumber(creditCardNumber);

        if(optionalCreditCard.isPresent()){
            return optionalCreditCard.get().getOwner().getId();
        }
        throw new
                InValidRequestException("user-not-found",String.format("given creditCardNumber=%s is not associated with any user", creditCardNumber), HttpStatus.BAD_REQUEST);
    }

    public List<BalanceHistoryView> updateBalanceOfCreditCard(UpdateBalancePayload[] payload) {
        List<BalanceHistoryView> balanceHistoryViews = new ArrayList<>();
        Map<String, List<UpdateBalancePayload>> balancePerCreditCard = Stream.of(payload).collect(groupingBy(UpdateBalancePayload::getCreditCardNumber));

        if(balancePerCreditCard.values().stream().flatMap(Collection::stream)
                .anyMatch(balance -> balance.getBalanceDate().compareTo(LocalDate.now()) > 0)){
            throw new InValidRequestException("invalid-balance-date", "can not update balance for future date", HttpStatus.BAD_REQUEST);
        }


        LocalDate minBalanceDateFromPayload = balancePerCreditCard.values().stream().flatMap(Collection::stream)
                .sorted(Comparator.comparing(UpdateBalancePayload::getBalanceDate)).toList().get(0).getBalanceDate();

        balancePerCreditCard.keySet().forEach(creditCardNumber -> {

            Optional<CreditCard> optionalCreditCard= creditCardRepository.findByNumber(creditCardNumber);

            if(optionalCreditCard.isPresent()){
                 CreditCard creditCard = optionalCreditCard.get();
                List<BalanceHistory> balanceHistories = new ArrayList<>(creditCard.getBalanceHistory().stream()
                        .sorted(Comparator.comparing(BalanceHistory::getDate)).toList());

                LocalDate currentDate = LocalDate.now();

                LocalDate firstTransactionDate = minBalanceDateFromPayload.compareTo(balanceHistories.get(0).getDate()) < 0? minBalanceDateFromPayload : balanceHistories.get(0).getDate();

                fillEmptyDatesWithPreviousDateBalance(creditCard, balanceHistories, currentDate, firstTransactionDate);

                List<BalanceHistory> updatedBalanceHistory = calculateAndUpdateBalanceDifference(balancePerCreditCard, creditCardNumber, balanceHistories);

                updatedBalanceHistory.stream().sorted(Comparator.comparing(BalanceHistory::getDate)).forEach(balanceHistory -> {
                            BalanceHistoryView balanceHistoryView;
                            Optional<BalanceHistoryView> optionalBalanceHistoryView = balanceHistoryViews.stream().filter(balance -> balance.getCreditCardNumber()
                                    .equals(balanceHistory.getCreditCard().getNumber())).findFirst();
                            if (optionalBalanceHistoryView.isPresent()){

                                (optionalBalanceHistoryView.get().getBalance()).add(Balance.builder().balanceAmount(balanceHistory.getBalance()).balanceDate(balanceHistory.getDate()).build());

                        }else{

                                balanceHistoryView = BalanceHistoryView.builder().creditCardNumber(balanceHistory.getCreditCard().getNumber())
                                        .balance(new ArrayList<>(Arrays.asList(Balance.builder().balanceAmount(balanceHistory.getBalance()).balanceDate(balanceHistory.getDate()).build()))).build();
                                balanceHistoryViews.add(balanceHistoryView);

                            }

                });


            }else{

                throw new
                        InValidRequestException("credit-card-not-found",String.format("given creditCardNumber=%s is not associated with a card", creditCardNumber), HttpStatus.BAD_REQUEST);
            }

        });

     return balanceHistoryViews;
    }

    private List<BalanceHistory> calculateAndUpdateBalanceDifference(Map<String, List<UpdateBalancePayload>> balancePerCreditCard, String creditCardNumber, List<BalanceHistory> balanceHistories) {
        balancePerCreditCard.get(creditCardNumber).stream().filter(balance ->
                balanceHistories.stream().map(BalanceHistory::getDate).anyMatch(date -> date.equals(balance.getBalanceDate()))).forEach(creditBalance -> {

            Optional<BalanceHistory> BalanceHistoryFromDB = balanceHistories.stream()
                    .filter(balanceHistory -> balanceHistory.getDate().equals(creditBalance.getBalanceDate())).findFirst();

            if(BalanceHistoryFromDB.isPresent()){
                double balanceDifference = creditBalance.getBalanceAmount() - BalanceHistoryFromDB.get().getBalance();

                    balanceHistories.stream().filter(balance -> balance.getDate().compareTo(creditBalance.getBalanceDate()) >= 0)
                            .forEach(balance -> balance.setBalance(balance.getBalance()+balanceDifference));


            }
            balanceHistoryRepository.saveAll(balanceHistories);
        });
        return balanceHistories;
    }

    private static void fillEmptyDatesWithPreviousDateBalance(CreditCard creditCard, List<BalanceHistory> balanceHistories, LocalDate currentDate, LocalDate firstTransactionDate) {
        Stream.iterate(firstTransactionDate, date -> date.plusDays(1))
                 .limit(ChronoUnit.DAYS.between(firstTransactionDate, currentDate)).forEach(date ->{
                     Optional<BalanceHistory> optionalBalanceHistory = balanceHistories.stream()
                             .filter(balanceHistory -> balanceHistory.getDate().equals(date)).findFirst();
                     if(!optionalBalanceHistory.isPresent()){
                         Optional<BalanceHistory> previousDayBalanceHistory = balanceHistories.stream()
                                 .filter(balanceHistory -> balanceHistory.getDate().equals(date.minusDays(1))).findFirst();
                         if(previousDayBalanceHistory.isPresent()) {
                             balanceHistories.add(BalanceHistory.builder().creditCard(creditCard).balance(previousDayBalanceHistory.get().getBalance()).date(date).build());
                         }else{
                             balanceHistories.add(BalanceHistory.builder().creditCard(creditCard).balance(0.0).date(date).build());
                         }

                     }
                 });
    }
}
