package com.example.zipcodelookup.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ZipCodeResponse {
    
    private String country;
    
    @JsonProperty("country abbreviation")
    private String countryAbbreviation;
    
    @JsonProperty("post code")
    private String postCode;
    
    private List<Place> places;
    
    public ZipCodeResponse() {}
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getCountryAbbreviation() {
        return countryAbbreviation;
    }
    
    public void setCountryAbbreviation(String countryAbbreviation) {
        this.countryAbbreviation = countryAbbreviation;
    }
    
    public String getPostCode() {
        return postCode;
    }
    
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }
    
    public List<Place> getPlaces() {
        return places;
    }
    
    public void setPlaces(List<Place> places) {
        this.places = places;
    }
    
    public static class Place {
        @JsonProperty("place name")
        private String placeName;
        
        private String longitude;
        private String latitude;
        private String state;
        
        @JsonProperty("state abbreviation")
        private String stateAbbreviation;
        
        public Place() {}
        
        public String getPlaceName() {
            return placeName;
        }
        
        public void setPlaceName(String placeName) {
            this.placeName = placeName;
        }
        
        public String getLongitude() {
            return longitude;
        }
        
        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }
        
        public String getLatitude() {
            return latitude;
        }
        
        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }
        
        public String getState() {
            return state;
        }
        
        public void setState(String state) {
            this.state = state;
        }
        
        public String getStateAbbreviation() {
            return stateAbbreviation;
        }
        
        public void setStateAbbreviation(String stateAbbreviation) {
            this.stateAbbreviation = stateAbbreviation;
        }
    }
}