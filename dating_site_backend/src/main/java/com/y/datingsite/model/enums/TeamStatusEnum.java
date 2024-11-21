package com.y.datingsite.model.enums;

/**
 * 队伍-状态枚举
 * @author : Yuan
 * @date :2024/8/14
 */
public enum TeamStatusEnum {

    PUBLIC (0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    public  static TeamStatusEnum getEnumByValue(Integer value){
        if(value == null){
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStautsEnum: values) {
            if(teamStautsEnum.getValue() == value){
                return teamStautsEnum;
            }
        }
        return null;

    }

    private int value;

    private String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
