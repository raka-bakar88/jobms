package com.raka.jobms.external;

import lombok.Data;

@Data
public class Review {
    private Long reviewId;
    private String title;
    private double rating;
}
