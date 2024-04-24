package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.service.UserService;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {

        Integer userId = userService.createUser(payload);

        return ResponseEntity.status(HttpStatus.OK).body(userId);
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {


         userService.deleteUser(userId);

        return ResponseEntity.status(HttpStatus.OK).body(String.format("user with ID=%d deleted successfully", userId));
    }
}
