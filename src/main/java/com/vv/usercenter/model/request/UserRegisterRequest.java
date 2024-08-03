package com.vv.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author vv先森
 * @create 2024-07-07 16:35
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 4003921038147102228L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}

