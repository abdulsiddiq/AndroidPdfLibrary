# AndroidPdfLibrary
A port of Apache's PdfBox 2.0.8 library to be usable on Android. Most features should be implemented by now. You can manipulate and render the pdf. At present rendering only supports bitmap rendering from android pdfrenderer API.

### Features

```
1. Compatible with Android Go Devices
2. Dynamic pdf rendering from server
3. Working with password protected pdf
```

## Usage

Import the library to your project

Before calls to library are made, it is highly recommended to initialize the library. Add the following line before calling library methods

```
PDFResourceLoader.init(getApplicationContext());
```

# License
This project is licensed under the GPL.3.0 License - see the LICENSE.md file for details

# Acknowledgments
TomRoush/PdfBox-Android based on PDF Box 1.8.1

Apache PDFBox 2.0.8

