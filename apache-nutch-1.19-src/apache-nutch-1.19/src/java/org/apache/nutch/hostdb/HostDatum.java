/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.hostdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;
import java.text.SimpleDateFormat;

import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 */
public class HostDatum implements Writable, Cloneable {
  protected long failures = 0;
  protected float score = 0;
  protected Date lastCheck = new Date(0);
  protected String homepageUrl = new String();

  protected MapWritable metaData = null;
  protected static final byte[] emptyMetaDataWritableSerialized;
  static {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream outStream = new DataOutputStream(buffer);
    MapWritable emptyMetaData = new MapWritable();
    try {
      emptyMetaData.write(outStream);
      outStream.close();
    } catch (IOException e) {}
    emptyMetaDataWritableSerialized = buffer.toByteArray();
  }

  // Records the number of times DNS look-up failed, may indicate host no longer exists
  protected long dnsFailures = 0;

  // Records the number of connection failures, may indicate our network being blocked by firewall
  protected long connectionFailures = 0;

  protected long unfetched = 0;
  protected long fetched = 0;
  protected long notModified = 0;
  protected long redirTemp = 0;
  protected long redirPerm = 0;
  protected long gone = 0;

  public HostDatum() {
  }

  public HostDatum(float score) {
    this(score, new Date());
  }

  public HostDatum(float score, Date lastCheck) {
    this(score, lastCheck, new String());
  }

  public HostDatum(float score, Date lastCheck, String homepageUrl) {
    this.score =  score;
    this.lastCheck = lastCheck;
    this.homepageUrl = homepageUrl;
  }

  public void resetFailures() {
    setDnsFailures(0l);
    setConnectionFailures(0l);
  }

  public void setDnsFailures(Long dnsFailures) {
    this.dnsFailures = dnsFailures;
  }

  public void setConnectionFailures(Long connectionFailures) {
    this.connectionFailures = connectionFailures;
  }

  public void incDnsFailures() {
    this.dnsFailures++;
  }

  public void incConnectionFailures() {
    this.connectionFailures++;
  }

  public Long numFailures() {
    return getDnsFailures() + getConnectionFailures();
  }

  public Long getDnsFailures() {
    return dnsFailures;
  }

  public Long getConnectionFailures() {
    return connectionFailures;
  }

  public void setScore(float score) {
    this.score = score;
  }

  public void setLastCheck() {
    setLastCheck(new Date());
  }

  public void setLastCheck(Date date) {
    lastCheck = date;
  }

  public boolean isEmpty() {
    return (lastCheck.getTime() == 0) ? true : false;
  }

  public float getScore() {
    return score;
  }

  public Long numRecords() {
    return unfetched + fetched + gone + redirPerm + redirTemp + notModified;
  }

  public Date getLastCheck() {
    return lastCheck;
  }

  public boolean hasHomepageUrl() {
    return homepageUrl.isEmpty();
  }

  public String getHomepageUrl() {
    return homepageUrl;
  }

  public void setHomepageUrl(String homepageUrl) {
    this.homepageUrl = homepageUrl;
  }

  public void setUnfetched(long val) {
    unfetched = val;
  }

  public long getUnfetched() {
    return unfetched;
  }

  public void setFetched(long val) {
    fetched = val;
  }

  public long getFetched() {
    return fetched;
  }

  public void setNotModified(long val) {
    notModified = val;
  }

  public long getNotModified() {
    return notModified;
  }

  public void setRedirTemp(long val) {
    redirTemp = val;
  }

  public long getRedirTemp() {
    return redirTemp;
  }

  public void setRedirPerm(long val) {
    redirPerm = val;
  }

  public long getRedirPerm() {
    return redirPerm;
  }

  public void setGone(long val) {
    gone = val;
  }

  public long getGone() {
    return gone;
  }

  public void resetStatistics() {
    setUnfetched(0);
    setFetched(0);
    setGone(0);
    setRedirTemp(0);
    setRedirPerm(0);
    setNotModified(0);
  }

   public void setMetaData(MapWritable mapWritable) {
     this.metaData = new MapWritable(mapWritable);
   }

   /**
    * Add all metadata from other HostDatum to this HostDatum.
    *
    * @param other HostDatum
    */
   public void putAllMetaData(HostDatum other) {
     if (other.hasMetaData()) {
       for (Entry<Writable, Writable> e : other.getMetaData().entrySet()) {
         getMetaData().put(e.getKey(), e.getValue());
       }
     }
   }

  /**
   * Get Host metadata.
   * @return a {@link MapWritable} if it was set or read in {@link #readFields(DataInput)},
   * OR returns empty map in case {@link HostDatum} was freshly created (lazily instantiated).
   */
  public MapWritable getMetaData() {
    if (this.metaData == null)
      this.metaData = new MapWritable();
    return this.metaData;
  }

  /** @return true if host has (non-empty) metadata */
  public boolean hasMetaData() {
    return this.metaData != null && !this.metaData.isEmpty();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    HostDatum result = (HostDatum)super.clone();
    result.score = score;
    result.lastCheck = lastCheck;
    result.homepageUrl = homepageUrl;

    result.dnsFailures = dnsFailures;
    result.connectionFailures = connectionFailures;

    result.unfetched = unfetched;
    result.fetched = fetched;
    result.notModified = notModified;
    result.redirTemp = redirTemp;
    result.redirPerm = redirPerm;
    result.gone = gone;

    result.metaData = metaData;

    return result;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    score = in.readFloat();
    lastCheck = new Date(in.readLong());
    homepageUrl = Text.readString(in);

    dnsFailures = in.readLong();
    connectionFailures = in.readLong();

    unfetched= in.readLong();
    fetched= in.readLong();
    notModified= in.readLong();
    redirTemp= in.readLong();
    redirPerm = in.readLong();
    gone = in.readLong();

    if (metaData == null) {
      metaData = new MapWritable();
    } else {
      metaData.clear();
    }
    metaData.readFields(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeFloat(score);
    out.writeLong(lastCheck.getTime());
    Text.writeString(out, homepageUrl);

    out.writeLong(dnsFailures);
    out.writeLong(connectionFailures);

    out.writeLong(unfetched);
    out.writeLong(fetched);
    out.writeLong(notModified);
    out.writeLong(redirTemp);
    out.writeLong(redirPerm);
    out.writeLong(gone);

    if (hasMetaData()) {
      metaData.write(out);
    } else {
      out.write(emptyMetaDataWritableSerialized);
    }
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(Long.toString(getUnfetched()));
    buf.append("\t");
    buf.append(Long.toString(getFetched()));
    buf.append("\t");
    buf.append(Long.toString(getGone()));
    buf.append("\t");
    buf.append(Long.toString(getRedirTemp()));
    buf.append("\t");
    buf.append(Long.toString(getRedirPerm()));
    buf.append("\t");
    buf.append(Long.toString(getNotModified()));
    buf.append("\t");
    buf.append(Long.toString(numRecords()));
    buf.append("\t");
    buf.append(Long.toString(getDnsFailures()));
    buf.append("\t");
    buf.append(Long.toString(getConnectionFailures()));
    buf.append("\t");
    buf.append(Long.toString(numFailures()));
    buf.append("\t");
    buf.append(Float.toString(score));
    buf.append("\t");
    buf.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastCheck));
    buf.append("\t");
    buf.append(homepageUrl);
    buf.append("\t");
    if (hasMetaData()) {
      for (Entry<Writable, Writable> e : getMetaData().entrySet()) {
        buf.append(e.getKey().toString());
        buf.append(':');
        buf.append(e.getValue().toString());
        buf.append("|||");
      }
    }
    return buf.toString();
  }

}
