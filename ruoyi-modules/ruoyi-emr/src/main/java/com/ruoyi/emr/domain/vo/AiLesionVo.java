package com.ruoyi.emr.domain.vo;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AI lesion box returned by detection service (supports fractional pixel coords).
 */
public class AiLesionVo
{
    private Double x1;
    private Double y1;
    private Double x2;
    private Double y2;
    private BigDecimal confidence;
    private Double width;
    private Double height;
    private Double area;
    private String position;
    private String lobe;
    @JsonProperty("full_position")
    private String fullPosition;

    public Double getX1() { return x1; }
    public void setX1(Double x1) { this.x1 = x1; }
    public Double getY1() { return y1; }
    public void setY1(Double y1) { this.y1 = y1; }
    public Double getX2() { return x2; }
    public void setX2(Double x2) { this.x2 = x2; }
    public Double getY2() { return y2; }
    public void setY2(Double y2) { this.y2 = y2; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public Double getWidth() { return width; }
    public void setWidth(Double width) { this.width = width; }
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getLobe() { return lobe; }
    public void setLobe(String lobe) { this.lobe = lobe; }
    public String getFullPosition() { return fullPosition; }
    public void setFullPosition(String fullPosition) { this.fullPosition = fullPosition; }
}
