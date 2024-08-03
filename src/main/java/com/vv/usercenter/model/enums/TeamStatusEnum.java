package com.vv.usercenter.model.enums;

/**
 * 队伍状态枚举
 * @author vv先森
 * @create 2024-07-28 10:36
 */
public enum TeamStatusEnum {
    TEAM_PRIVATE(1,"私有"),
    TEAM_PUBLIC(0,"公开"),
    TEAM_SECRET(2,"加密");

    private int code;

    private String text;

    TeamStatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static TeamStatusEnum getEnumByValue(Integer value){
        if (value == null){
            return null;
        }
        TeamStatusEnum[] statusEnums = TeamStatusEnum.values();
        for (TeamStatusEnum statusEnum : statusEnums) {
            if (statusEnum.code == value){
                return statusEnum;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
