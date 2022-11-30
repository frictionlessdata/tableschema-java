package io.frictionlessdata.tableschema.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"pink_field", "is_red", "is_white", "blue", "black"})
public class ExplicitNamingBean {

    @JsonProperty("pink_field")
    public boolean isPink;

    @JsonProperty("is_red")
    public boolean isRed;

    @JsonProperty("is_white")
    public boolean isWhite;

    @JsonProperty("blue")
    public boolean isBlue;

    @JsonProperty("black")
    boolean isBlack;

    public boolean isBlack() {
        return isBlack;
    }

    public void setBlack(boolean black) {
        isBlack = black;
    }
}
