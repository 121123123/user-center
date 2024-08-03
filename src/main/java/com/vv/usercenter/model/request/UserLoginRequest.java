package com.vv.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author vv先森
 * @create 2024-07-07 16:20
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 862748177736693294L;
    private String userAccount;
    private String userPassword;
}
