package com.shepherdmoney.interviewproject.service;

import com.shepherdmoney.interviewproject.exception.InValidRequestException;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Integer createUser(CreateUserPayload payload) {

       return userRepository.save(User.builder().name(payload.getName()).email(payload.getEmail()).build()).getId();

    }

    public void deleteUser(int userId) {

        Optional<User> optionalUser = userRepository.findById(userId);

        if(optionalUser.isPresent()) {
            userRepository.deleteById(userId);
        }else {
            throw new
                    InValidRequestException("user-not-found",
                    String.format(String.format("user with ID=%d does not exists", userId)), HttpStatus.BAD_REQUEST);
        }

    }

}
