package com.companyz.ems.model;

/** Row of the {@code addresses} table, with city and state names resolved. */
public record Address(String street, String city, String stateAbbreviation, String zip) {
}
