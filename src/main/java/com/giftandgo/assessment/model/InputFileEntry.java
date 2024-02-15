package com.giftandgo.assessment.model;

import java.util.UUID;

public record InputFileEntry(UUID uuid, String id, String name, String likes, String transport, Double avgSpeed, Double topSpeed) {
}
