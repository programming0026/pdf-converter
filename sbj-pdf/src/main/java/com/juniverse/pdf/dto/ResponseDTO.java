package com.juniverse.pdf.dto;

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
     * Contenido.
     */
    private T content;

    /**
     * Mensaje.
     */
    private String message;
}
