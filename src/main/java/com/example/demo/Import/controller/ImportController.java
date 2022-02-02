package com.example.demo.Import.controller;

import com.example.demo.Import.service.CSVImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImportController {

  @Autowired
  CSVImport csvImport;

  @PostMapping("/import")
  public ResponseEntity<Void> processCSV() {
    csvImport.processFiles();
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
