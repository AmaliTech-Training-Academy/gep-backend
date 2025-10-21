package com.event_service.event_service.exceptions;

public class ResourceNotFound extends RuntimeException {
  public ResourceNotFound(String message) {
    super(message);
  }
}
