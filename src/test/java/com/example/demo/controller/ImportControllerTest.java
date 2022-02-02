package com.example.demo.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.example.demo.Import.controller.ImportController;
import com.example.demo.Import.service.CSVImport;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ImportControllerTest {

  @InjectMocks
  private ImportController mockImportController;

  @Mock
  private CSVImport csvImport;

  @BeforeMethod(alwaysRun = true)
  public void initMock() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void proceeCSVTest() {
    doNothing().when(csvImport).processFiles();
    final ResponseEntity<Void> responseEntity = this.mockImportController.processCSV();

    assertNotNull(responseEntity);
    assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
    verify(csvImport).processFiles();
    verifyNoMoreInteractions(csvImport);
  }
}
