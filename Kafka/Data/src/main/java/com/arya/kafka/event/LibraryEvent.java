package com.arya.kafka.event;

import lombok.Data;

@Data
public class LibraryEvent {

    private Integer id;
    private LibraryEventType type;
    private Book book;
}
