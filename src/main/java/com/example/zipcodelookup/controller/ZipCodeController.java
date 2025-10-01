package com.example.zipcodelookup.controller;

import com.example.zipcodelookup.model.ZipCodeRequest;
import com.example.zipcodelookup.model.ZipCodeResponse;
import com.example.zipcodelookup.service.ZipCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ZipCodeController {
    
    @Autowired
    private ZipCodeService zipCodeService;
    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("zipCodeRequest", new ZipCodeRequest());
        return "index";
    }
    
    @PostMapping("/lookup")
    public String lookup(@Valid ZipCodeRequest zipCodeRequest, BindingResult bindingResult, Model model) {
        model.addAttribute("zipCodeRequest", zipCodeRequest);
        
        if (bindingResult.hasErrors()) {
            return "index";
        }
        
        try {
            ZipCodeResponse response = zipCodeService.getZipCodeInfo(zipCodeRequest.getZipCode());
            
            if (response == null) {
                model.addAttribute("errorMessage", "Zip code not found. Please enter a valid US zip code.");
                return "index";
            }
            
            model.addAttribute("zipCodeResponse", response);
            return "index";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while fetching zip code information. Please try again.");
            return "index";
        }
    }
}