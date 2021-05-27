package io.frictionlessdata.tableschema.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.frictionlessdata.tableschema.annotations.FieldFormat;
import org.locationtech.jts.geom.Coordinate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

@JsonPropertyOrder({
   "id", "name", "dateOfBirth", "isAdmin", "addressCoordinates", "contractLength", "info"
})
public class EmployeeBeanWithAnnotation {

 private Integer id;

 private String name;

 @FieldFormat(format = "%m/%d/%y")
 private LocalDate dateOfBirth;

 @JsonProperty("isAdmin")
 private Boolean admin;

 /**
  * Using https://locationtech.github.io/jts/javadoc/org/locationtech/jts/geom/Coordinate.html
  * for location here. Alternative would be
  * https://docs.geotools.org/latest/javadocs/org/geotools/geometry/DirectPosition2D.html
  */
 private Coordinate addressCoordinates;

 private Duration contractLength;

 private Map info;

 @Override
 public String toString() {
  return "EmployeeBean{" +
          "id=" + id +
          ", name='" + name + '\'' +
          ", dateOfBirth=" + dateOfBirth +
          ", admin=" + admin +
          ", addressCoordinates=" + addressCoordinates +
          ", contractLength=" + contractLength +
          ", info=" + info +
          '}';
 }

 public Integer getId() {
  return id;
 }

 public void setId(Integer id) {
  this.id = id;
 }

 public String getName() {
  return name;
 }

 public void setName(String name) {
  this.name = name;
 }

 public LocalDate getDateOfBirth() {
  return dateOfBirth;
 }

 public void setDateOfBirth(LocalDate dateOfBirth) {
  this.dateOfBirth = dateOfBirth;
 }

 public Boolean getAdmin() {
  return admin;
 }

 public void setAdmin(Boolean admin) {
  this.admin = admin;
 }

 public Coordinate getAddressCoordinates() {
  return addressCoordinates;
 }

 public void setAddressCoordinates(Coordinate addressCoordinates) {
  this.addressCoordinates = addressCoordinates;
 }

 public Duration getContractLength() {
  return contractLength;
 }

 public void setContractLength(Duration contractLength) {
  this.contractLength = contractLength;
 }

 public Map getInfo() {
  return info;
 }

 public void setInfo(Map info) {
  this.info = info;
 }
}
