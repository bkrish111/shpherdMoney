package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.service.CreditCardService;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.BalanceHistoryView;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {

        Integer creditCardId = creditCardService.addCreditCardToUser(payload);
        return ResponseEntity.status(HttpStatus.OK).body(creditCardId);
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {

        List<CreditCardView> creditCards = creditCardService.getAllCardOfUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(creditCards);
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {

        Integer userId = creditCardService.getUserIdForCreditCard(creditCardNumber);
        return ResponseEntity.status(HttpStatus.OK).body(userId);
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<List<BalanceHistoryView>> updateBalanceOfCreditCard(@RequestBody UpdateBalancePayload[] payload) {

        return ResponseEntity.status(HttpStatus.OK).body(creditCardService.updateBalanceOfCreditCard(payload));
    }
    
}