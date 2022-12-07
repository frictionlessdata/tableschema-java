package io.frictionlessdata.tableschema.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.frictionlessdata.tableschema.annotations.FieldFormat;
import org.locationtech.jts.geom.Coordinate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// we need to give the property order to get a valid schema, as Java reflection tosses this.
@JsonPropertyOrder({
        "first_name", "last_name", "gender", "dateOfBirth", "age", "period_employed", "employment_start", "daily_start",
        "daily_end", "is_management", "photo", "interests", "home_location", "position_title",
        "extra", "notes"
})
public class EmployeeBeanWithAnnotation {

 @JsonProperty("first_name")
 private String firstName;

 @JsonProperty("last_name")
 private String lastName;

 private String gender;

 private Integer age;

 @JsonProperty("period_employed")
 private Float employmentDuration;

 @JsonProperty("employment_start")
 private LocalDate employmentStart;

 @JsonProperty("daily_start")
 private LocalTime dailyStart;

 @JsonProperty("daily_end")
 private LocalTime dailyEnd;

 @FieldFormat(format = "%m/%d/%Y")
 private LocalDate dateOfBirth;

 @JsonProperty("is_management")
 private Boolean manager;

 private byte[] photo;

 private List<String> interests = new ArrayList<>();
 /**
  * Using https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Coordinate.html
  * for location here. Alternative would be
  * https://docs.geotools.org/latest/javadocs/org/geotools/geometry/DirectPosition2D.html
  */
 @JsonProperty("home_location")
 private Coordinate addressCoordinates;

 @JsonProperty("position_title")
 private String positionTitle;

 @JsonProperty("extra")
 private Map extra;

 @JsonProperty("notes")
 private Object info;

 @Override
 public String toString() {
  return "EmployeeBean{" +
          ", name='" + lastName + '\'' +
          ", dateOfBirth=" + dateOfBirth +
          ", admin=" + manager +
          ", addressCoordinates=" + addressCoordinates +
          ", info=" + info +
          '}';
 }

 public String getFirstName() {
  return firstName;
 }

 public void setFirstName(String firstName) {
  this.firstName = firstName;
 }

 public String getLastName() {
  return lastName;
 }

 public void setLastName(String lastName) {
  this.lastName = lastName;
 }

 public LocalDate getDateOfBirth() {
  return dateOfBirth;
 }

 public void setDateOfBirth(LocalDate dateOfBirth) {
  this.dateOfBirth = dateOfBirth;
 }

 public Boolean getManager() {
  return manager;
 }

 public void setManager(Boolean manager) {
  this.manager = manager;
 }

 public Coordinate getAddressCoordinates() {
  return addressCoordinates;
 }

 public void setAddressCoordinates(Coordinate addressCoordinates) {
  this.addressCoordinates = addressCoordinates;
 }

 public Object getInfo() {
  return info;
 }

 public void setInfo(Object info) {
  this.info = info;
 }

 public List<String> getInterests() {
  return interests;
 }

 public void setInterests(List<String> interests) {
  this.interests = interests;
 }

 public String getGender() {
  return gender;
 }

 public void setGender(String gender) {
  this.gender = gender;
 }

 public Integer getAge() {
  return age;
 }

 public void setAge(Integer age) {
  this.age = age;
 }

 public Float getEmploymentDuration() {
  return employmentDuration;
 }

 public void setEmploymentDuration(Float employmentDuration) {
  this.employmentDuration = employmentDuration;
 }

 public LocalDate getEmploymentStart() {
  return employmentStart;
 }

 public void setEmploymentStart(LocalDate employmentStart) {
  this.employmentStart = employmentStart;
 }

 public LocalTime getDailyStart() {
  return dailyStart;
 }

 public void setDailyStart(LocalTime dailyStart) {
  this.dailyStart = dailyStart;
 }

 public LocalTime getDailyEnd() {
  return dailyEnd;
 }

 public void setDailyEnd(LocalTime dailyEnd) {
  this.dailyEnd = dailyEnd;
 }

 public byte[] getPhoto() {
  return photo;
 }

 public void setPhoto(byte[] photo) {
  this.photo = photo;
 }

 public String getPositionTitle() {
  return positionTitle;
 }

 public void setPositionTitle(String positionTitle) {
  this.positionTitle = positionTitle;
 }

 public Map getExtra() {
  return extra;
 }

 public void setExtra(Map extra) {
  this.extra = extra;
 }
}
