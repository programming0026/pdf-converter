package com.work.pdf.tools;

/**
 *
 * @author linux
 */
public enum Angles {
    DEGREE_45(45),
    DEGREE_90(90),
    DEGREE_180(180),
    DEGREE_270(270),
    DEGREE_360(360);

    Angles(Integer degree) {
        this.degree = degree;
    }

    public Integer getDegree() {
        return degree;
    }

    private final Integer degree;
}
