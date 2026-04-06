package com.ruoyi.emr.domain.vo;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;

public class EmrExtractRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 待抽取的正文（HTML 或纯文本） */
    @NotBlank(message = "正文内容不能为空")
    private String content;

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
