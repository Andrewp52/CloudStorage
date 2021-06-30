package com.pae.common;

public enum Command {
    AUTH_REQ("auth"),
    AUTH_OK("auth_ok"),
    AUTH_FAIL("auth_fail"),
    PROFILE_REQ("prof_req"),

    REG_REQ("reg_req"),
    REG_OK("reg_ok"),
    REG_FAIL("reg_fail"),

    FILE_UP("file_up"),
    FILE_DOWN("file_down"),
    FILE_LIST("file_list"),
    FILE_REMOVE("file_rm"),
    FILE_COPY("file_copy");


    String val;
    Command(String val) {
        this.val = val;
    }
}
