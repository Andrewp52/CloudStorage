package com.pae.cloudstorage.common;

/**
 * Common for client and server Commands enum.
 */
public enum Command {
    //             User authentication, registration and profile commands
    AUTH_REQ,
    AUTH_OK,
    AUTH_FAIL,
    AUTH_OUT,
    PROFILE_REQ,
    PROFILE_UPD,
    REG_REQ,
    REG_OK,
    REG_FAIL,

    //              Filesystem actions commands
    FILE_UPLOAD,
    FILE_DOWNLOAD,
    FILE_LIST,
    FILE_CD,
    FILE_MKDIR,
    FILE_REMOVE,
    FILE_REMOVEREC,
    FILE_DNE,
    FILE_COPY,
    FILE_SEARCH,
    FILE_PATHS,
    FILE_MOVE,

    CMD_SUCCESS,
    CMD_FAIL;
}
