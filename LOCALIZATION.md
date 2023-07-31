# XMLJava Localization

Localizing XMLJava requires processing Java `.properties` files. [JavaPM](https://www.maxprograms.com/products/javapm.html) is used to generate XLIFF from `/src` folder.

Use a command like this to generate XLIFF:

```bash
/path-to-Javapm/createxliff.sh -srcLang en -tgtLang es -enc UTF-8 -reuse -2.0 -src /path-to-XMLJava/src -xliff yourXliffFile.xlf 
```

XMLJava .properties are encoded in UTF-8; translated versions must be generated using UTF-8 character set.

You can find XLIFF and TMX sample files for XMLJava's `.properties` in `/i18n` folder.
