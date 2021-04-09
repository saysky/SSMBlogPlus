package com.example.blog.enums;

/**
 * @author 言曌
 * @date 2021/4/9 11:32 上午
 */

public enum  HeartValueEnum {
    GX("gx", "高兴"),
    FN("fn", "愤怒"),
    YL("yl", "忧虑"),
    PT("pt", "普通"),
    NG("ng", "难过"),
    KH("kh", "恐慌"),
    JQ("jq", "惊奇")
    ;


    private String code;

    private String desc;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    HeartValueEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取描述值
     * @param code
     * @return
     */
    public static String getDescByCode(String code){
        for (HeartValueEnum value : HeartValueEnum.values()) {
            if(value.getCode().equals(code)){
                return value.getDesc();
            }
        }
        return null;
    }
}
