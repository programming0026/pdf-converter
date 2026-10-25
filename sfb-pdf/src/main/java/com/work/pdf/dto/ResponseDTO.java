package com.work.pdf.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author AJO
 * @param <T>
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResponseDTO<T> {

    /**
     * The content.
     */
    private T content;

    /**
     * The message.
     */
    private String message;
}
