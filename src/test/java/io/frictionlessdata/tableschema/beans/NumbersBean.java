package io.frictionlessdata.tableschema.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.util.concurrent.AtomicDouble;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@JsonPropertyOrder({
   "id", "byteVal", "shortVal", "intVal", "longClassVal", "longVal", "floatVal",
        "doubleVal", "floatClassVal", "doubleClassVal", "bigIntVal", "bigDecimalVal",
        "atomicIntegerVal", "atomicLongVal", "atomicDoubleVal"
})
public class NumbersBean {

 private Integer id;

 private byte byteVal;

 private short shortVal;

 private int intVal;

 private Long longClassVal;

 private long longVal;

 private float floatVal;

 private double doubleVal;

 private float floatClassVal;

 private double doubleClassVal;

 private BigInteger bigIntVal;

 private BigDecimal bigDecimalVal;

 private AtomicInteger atomicIntegerVal;

 private AtomicLong atomicLongVal;

 private AtomicDouble atomicDoubleVal;

 public Integer getId() {
  return id;
 }

 public void setId(Integer id) {
  this.id = id;
 }

 public byte getByteVal() {
  return byteVal;
 }

 public void setByteVal(byte byteVal) {
  this.byteVal = byteVal;
 }

 public short getShortVal() {
  return shortVal;
 }

 public void setShortVal(short shortVal) {
  this.shortVal = shortVal;
 }

 public int getIntVal() {
  return intVal;
 }

 public void setIntVal(int intVal) {
  this.intVal = intVal;
 }

 public Long getLongClassVal() {
  return longClassVal;
 }

 public void setLongClassVal(Long longClassVal) {
  this.longClassVal = longClassVal;
 }

 public long getLongVal() {
  return longVal;
 }

 public void setLongVal(long longVal) {
  this.longVal = longVal;
 }

 public float getFloatVal() {
  return floatVal;
 }

 public void setFloatVal(float floatVal) {
  this.floatVal = floatVal;
 }

 public double getDoubleVal() {
  return doubleVal;
 }

 public void setDoubleVal(double doubleVal) {
  this.doubleVal = doubleVal;
 }

 public BigInteger getBigIntVal() {
  return bigIntVal;
 }

 public void setBigIntVal(BigInteger bigIntVal) {
  this.bigIntVal = bigIntVal;
 }

 public BigDecimal getBigDecimalVal() {
  return bigDecimalVal;
 }

 public void setBigDecimalVal(BigDecimal bigDecimalVal) {
  this.bigDecimalVal = bigDecimalVal;
 }

 public AtomicInteger getAtomicIntegerVal() {
  return atomicIntegerVal;
 }

 public void setAtomicIntegerVal(AtomicInteger atomicIntegerVal) {
  this.atomicIntegerVal = atomicIntegerVal;
 }

 public AtomicLong getAtomicLongVal() {
  return atomicLongVal;
 }

 public void setAtomicLongVal(AtomicLong atomicLongVal) {
  this.atomicLongVal = atomicLongVal;
 }

 public AtomicDouble getAtomicDoubleVal() {
  return atomicDoubleVal;
 }

 public void setAtomicDoubleVal(AtomicDouble atomicDoubleVal) {
  this.atomicDoubleVal = atomicDoubleVal;
 }


 public float getFloatClassVal() {
  return floatClassVal;
 }

 public void setFloatClassVal(float floatClassVal) {
  this.floatClassVal = floatClassVal;
 }

 public double getDoubleClassVal() {
  return doubleClassVal;
 }

 public void setDoubleClassVal(double doubleClassVal) {
  this.doubleClassVal = doubleClassVal;
 }

 @Override
 public String toString() {
  return "NumbersBean{" +
          "id=" + id +
          ", byteVal=" + byteVal +
          ", shortVal=" + shortVal +
          ", intVal=" + intVal +
          ", longClassVal=" + longClassVal +
          ", longVal=" + longVal +
          ", floatVal=" + floatVal +
          ", doubleVal=" + doubleVal +
          ", floatClassVal=" + floatClassVal+
          ", doubleClassVal=" + doubleClassVal+
          ", bigIntVal=" + bigIntVal +
          ", bigDecimalVal=" + bigDecimalVal +
          ", atomicIntegerVal=" + atomicIntegerVal +
          ", atomicLongVal=" + atomicLongVal +
          ", atomicDoubleVal=" + atomicDoubleVal +
          '}';
 }

 @Override
 public boolean equals(Object o) {
  if (this == o) return true;
  if (!(o instanceof NumbersBean)) return false;
  NumbersBean that = (NumbersBean) o;
  return byteVal == that.byteVal &&
          shortVal == that.shortVal &&
          intVal == that.intVal &&
          longVal == that.longVal &&
          Float.compare(that.floatVal, floatVal) == 0 &&
          Double.compare(that.doubleVal, doubleVal) == 0 &&
          Objects.equals(id, that.id) &&
          Objects.equals(longClassVal, that.longClassVal) &&
          Objects.equals(floatClassVal, that.floatClassVal)&&
          Objects.equals(doubleClassVal, that.doubleClassVal)&&
          Objects.equals(bigIntVal, that.bigIntVal) &&
          Objects.equals(bigDecimalVal, that.bigDecimalVal) &&
          Objects.equals(atomicIntegerVal.intValue(), that.atomicIntegerVal.intValue()) &&
          Objects.equals(atomicLongVal.longValue(), that.atomicLongVal.longValue()) &&
          Objects.equals(atomicDoubleVal.doubleValue(), that.atomicDoubleVal.doubleValue());
 }

 @Override
 public int hashCode() {
  return Objects.hash(id, byteVal, shortVal, intVal, longClassVal, longVal, floatVal, doubleVal, bigIntVal, bigDecimalVal, atomicIntegerVal, atomicLongVal, atomicDoubleVal);
 }
}
