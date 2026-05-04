package com.ruoyi.emr.domain.vo;

/**
 * Chest X-ray option for structured record binding.
 */
public class XrayOptionVo
{
    private Long id;

    private String imagePath;

    private String imageName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    public String getImageName()
    {
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }
}
