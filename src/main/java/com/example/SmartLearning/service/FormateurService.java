package com.example.SmartLearning.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SmartLearning.Repository.FormateurRepository;
import com.example.SmartLearning.model.Formateur;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class FormateurService {

    @Autowired
    private FormateurRepository formateurRepository;

    
      public Formateur getFormateurById(Long formateurId) {
        return formateurRepository.findById(formateurId)
                .orElseThrow(() ->
                        new RuntimeException("Formateur not found with id : " + formateurId)
                );
    }


}
