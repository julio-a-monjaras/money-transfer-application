package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private UserDao userDao;

    public UserController(JdbcUserDao userDao) {
        this.userDao = userDao;
    }


    //Controllers that communicates with the client UserService class to execute queries
    @GetMapping(path = "/id")
    public int findIdByUserName(Principal principal) {
        if (principal.getName() != null) {
            return userDao.findIdByUsername(principal.getName());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found");
    }

    @GetMapping(path = "/search_user/{id}")
    public User getUserById(@PathVariable int id) {
        User user = userDao.getUserById(id);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction Not Found");
        } else {
            return user;
        }

    }

    @GetMapping(path = "/user_from_account/{id}")
    public String getUsernameByAccountId(@PathVariable int id) {
        return userDao.getUserByAccount(id);
    }

    @GetMapping
    public List<User> getUsers() {
        return userDao.findAll();
    }
}