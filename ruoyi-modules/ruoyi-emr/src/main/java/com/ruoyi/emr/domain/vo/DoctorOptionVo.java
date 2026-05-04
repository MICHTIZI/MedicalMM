package com.ruoyi.emr.domain.vo;

/**
 * Doctor option sourced from sys_user.
 */
public class DoctorOptionVo
{
    private Long userId;

    private String nickName;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }
}
