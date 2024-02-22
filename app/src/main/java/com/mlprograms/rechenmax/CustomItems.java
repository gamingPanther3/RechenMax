package com.mlprograms.rechenmax;

public class CustomItems {

    private String spinnerText;
    private int spinnerImage;
    private int textColor;
    private int backgroundColor;

    public CustomItems(String spinnerText, int spinnerImage) {
        this.spinnerText = spinnerText;
        this.spinnerImage = spinnerImage;
    }

    public String getSpinnerText() {
        return spinnerText;
    }

    public void setSpinnerText(String spinnerText) {
        this.spinnerText = spinnerText;
    }

    public int getSpinnerImage() {
        return spinnerImage;
    }

    public void setSpinnerImage(int spinnerImage) {
        this.spinnerImage = spinnerImage;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }
}
