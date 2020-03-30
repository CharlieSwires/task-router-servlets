package com.twilio.taskrouter.domain.model;

import com.twilio.taskrouter.domain.common.Utils;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

/**
 * Gathers information about a phone call
 */
@Embeddable
public class PhoneNumber {

  @Size(max = 15)
  private String phoneNumber;

  @Transient
  private String internationalPhoneNumber;

  private PhoneNumber() {
  }

  public PhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getInternationalPhoneNumber() {
    if (internationalPhoneNumber == null) {
      internationalPhoneNumber = Utils.formatPhoneNumberToUSInternational(phoneNumber);
    }
    return internationalPhoneNumber;
  }

  @Override
  public String toString() {
    return getPhoneNumber();
  }
  @Override
  public boolean equals(Object o) {

      if (o == this) {
          return true;
      }
      if (!(o instanceof PhoneNumber)) {
          return false;
      }

      PhoneNumber phoneNumber = (PhoneNumber) o;

      return phoneNumber.phoneNumber.equals(phoneNumber);
  }

  //Idea from effective Java : Item 9
  @Override
  public int hashCode() {
      int result = 17;
      result = 31 * result + phoneNumber.hashCode();
      return result;
  }
}
