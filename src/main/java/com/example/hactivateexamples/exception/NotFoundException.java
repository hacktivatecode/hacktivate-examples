package com.example.hactivateexamples.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Integer errorCode;

    public NotFoundException(String errorMessage, Integer errorCode) {
        super(errorMessage);
    }

}
