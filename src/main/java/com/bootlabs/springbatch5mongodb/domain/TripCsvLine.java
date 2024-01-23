package com.bootlabs.springbatch5mongodb.domain;

public record TripCsvLine(
        Integer bikeId,
        Integer age,
        String gender,
        String durationTime,
        String startStationName,
        String endStationName
) {
}



