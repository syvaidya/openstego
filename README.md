# OpenStego
OpenStego is a steganography application that provides two functionalities:

1. Data Hiding: It can hide any data within an image file.

2. Watermarking: Watermarking image files with an invisible signature. It can be used to detect unauthorized file copying.

## Usage

### For GUI:
Use menu shortcut for OpenStego if you used installer. For zip downloads, use the bundled batch file or shell script to launch the GUI.
```
openstego.bat                (Windows)
```
```
./openstego.sh               (Linux / MacOS)
```

### For command line interface:
Refer to [online documentation](https://www.openstego.com/cmdline.html).

## Development
Fork the repository, clone it locally and execute following to build it fully:
```
gradlew clean dist           (Windows)
```
```
./gradlew clean dist         (Linux / MacOS)
```
*Note:* Windows installer will be generated only if you execute build on Windows environment. It needs [Inno Setup](https://jrsoftware.org/isdl.php) to be installed, and `iscc.exe` to be on `PATH`. If you don't want to generate Windows installer, you can skip the same using following command:
```
./gradlew clean dist -x distWin
```

## Author
Samir Vaidya (syvaidya [at] gmail)

## Homepage
https://www.openstego.com

## License
GNU General Public License 2.0 (GPL) (see ```LICENSE``` file)

## Acknowledgement
The digital watermarking code in this product is based on the code provided by Peter Meerwald. Refer to his excellent thesis on [watermarking](http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/): Peter Meerwald, Digital Image Watermarking in the Wavelet Transfer Domain, Master's Thesis, Department of Scientific Computing, University of Salzburg, Austria, January 2001.
