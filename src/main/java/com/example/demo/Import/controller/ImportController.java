package com.example.demo.Import.controller;

import com.example.demo.Import.service.CSVImport;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImportController {

  @Autowired
  CSVImport csvImport;

  @PostMapping("/import")
  public void processCSV() {
    csvImport.processFiles();
  }
}
