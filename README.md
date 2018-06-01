# AndroidPdfLibrary
A port of Apache's PdfBox 2.0.8 library to be usable on Android. Most features should be implemented by now. You can manipulate and render the pdf. At present rendering only supports bitmap rendering from android pdfrenderer API.

### Features

```
1. Compatible with All Android OS from lollipop
2. Compatible with Android Go as well
3. Split and save the pdf
4. Dynamic pdf rendering from server
5. Working with password protected pdf
```

## Usage

Import the library to your project

Before calls to library are made, it is highly recommended to initialize the library. Add the following line before calling library methods

```
PDFResourceLoader.init(getApplicationContext());
```

# Save the pdf

```
            PDFHandler handler = new PDFHandler(Looper.getMainLooper());
            try
            {
                handler.prepareForDroidNative("originalpdfPath", "password of the pdf")
                        .setProgressListener(new OnManipulateProgressListener()
                        {
                            @Override
                            public void onProgress( int i )
                            {
                                Log.d("progress", "pdf progress" + i);
                            }

                            @Override
                            public void onProcessComplete( int i, int numberOfFiles, int numberOfPages )
                            {
                                switch (i)
                                {
                                    case OnManipulateProgressListener.STATUS_COMPLETE:
                                Log.d("download", "number of files generated" + numberOfFiles);
                                    break;
                                }
                            }
                        }).splitAndSave("filepath to store the pdf", "number of pages to split at", "password to encrypt with");
            } catch (IOException e)
            {
                e.printStackTrace();
            }

```
# To open pdf

### Intialize the PDFPageExtractor
```
                    PDFInfo[] pdfInfos = new PDFInfo["number of files generated"];
                for (int i = 0; i < pdfInfos.length; i++) {
                    pdfInfos[i] = new PDFInfo("file path"+ (i + 1) + ".pdf", "password used for encrypting");
                }
                final PDFPageExtractor _extractor = new PDFPageExtractor(number of pages to split while splitting, pdfInfos);
                
```
### Use the extractor instance to render the page
```
// TO intiate programatically
            PDFPageView pageView = new PDFPageView(container.getContext(),isLandscape);
//  Can also inflate from layout
            PDFPageView pageView = findviewbyid("your id");           

            pageView.setPageWidth(1116);
            pageView.setPageHeight(1546);

// using the below lines will render and set the page image to the pdfpageview
            pageView.setPageIndex(position);
            _extractor.loadPage(pageView);
```

# Progressive Rendering

### Progressive rendering
Provide base url in download base link and url to append in the download link. The library will append page number sequentially 
on its own.

# Pre-Requisite 
The pdf should not have more than one page.

### Example
`````
basedownload url = "http://www.pdfexample.com/"
appendurl = "mypath/pdfname"
final url by library = basedownload url +appendurl +"1.pdf"
`````
```
            Intent intent = new Intent(this,ProgressiveExtractor.class);
            intent.setAction(IntentActions.ACTION_RENDER);
            intent.putExtra(IntentKeys.ITEM_ID,_issueId);
            intent.putExtra(IntentKeys.DOWNLOAD_LINK,appendDownloadUrl);
            intent.putExtra(IntentKeys.DOWNLOAD_BASE_LINK,baseDownloadUrl);
            intent.putExtra(IntentKeys.TOTAL_PAGE,_totalPages);
            bindservice......
            ...
            ..
            
                ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected( ComponentName name, IBinder service )
        {
            mExtractor = ((ProgressiveExtractor.ExtractorBinder) service).getProgressiveExtractor();
        }

        @Override
        public void onServiceDisconnected( ComponentName name )
        {
            MagsLogHelper.debugLog("extractorser","service is unbound");
            mExtractor = null;
        }
    };
    
    ....
    ....
    ....
    


```

```
           pageView.setPageIndex("pagenumber");
            mExtractor.renderPage(_issueId,pdfpageview);
```



# License
This project is licensed under the GPL.3.0 License - see the LICENSE.md file for details

# Acknowledgments
TomRoush/PdfBox-Android based on PDF Box 1.8.1

Apache PDFBox 2.0.8

