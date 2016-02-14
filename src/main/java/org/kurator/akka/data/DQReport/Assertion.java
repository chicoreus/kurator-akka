package org.kurator.akka.data.DQReport;

import java.util.Map;
import java.util.HashMap;
// TODO: Move to DQReport inside "data" directory
public abstract class Assertion {
  private Map<String,String> dataResource;
  private String specification;
  private String mechanism;
  // supporting a key-value pair list of results
  //private Map<String,String> result;
  private String result;

  public Map<String,String> getDataResource(){
    return this.dataResource;
  }
  public String getSpecification(){
    return this.specification;
  }
  public String getMechanism(){
    return this.mechanism;
  }
  public String getResult(){
    return this.result;
  }
  public void setDataResource(Map<String,String> dataResource){
    this.dataResource = dataResource;
  }
  public void setSpecification(String specification){
    this.specification = specification;
  }
  public void setMechanism(String mechanism){
    this.mechanism = mechanism;
  }
  public void setResult(String result){
    this.result = result;
  }
}
