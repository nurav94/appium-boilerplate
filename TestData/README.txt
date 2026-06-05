Place your Excel test data files (.xlsx) in this folder.

Default file configured in config.properties:
  testdata.path=TestData/sample_data.xlsx
  testdata.sheet=LoginData

Excel Sheet Structure Example (LoginData sheet):
  Column A: email
  Column B: password
  Column C: expectedResult

Row 1 = Header (skipped by ExcelUtils)
Row 2+ = Test data rows

Usage in test:
  Object[][] data = ExcelUtils.getTableArray(config.getTestDataPath(), config.getTestDataSheet());
