package com.ruoyi.emr.domain.vo;

import java.math.BigDecimal;

/**
 * AI lesion box returned by detection service.
 */
public class AiLesionVo
{
    private Integer x1;
    private Integer y1;
    private Integer x2;
    private Integer y2;
    private BigDecimal confidence;

    public Integer getX1() { return x1; }
    public void setX1(Integer x1) { this.x1 = x1; }
    public Integer getY1() { return y1; }
    public void setY1(Integer y1) { this.y1 = y1; }
    public Integer getX2() { return x2; }
    public void setX2(Integer x2) { this.x2 = x2; }
    public Integer getY2() { return y2; }
    public void setY2(Integer y2) { this.y2 = y2; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
}
