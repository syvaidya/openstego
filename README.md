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
*Note:* This will fail on non-windows environment, as one of the distribution tasks is to generate windows installer. On other platforms, you can skip the same using:
```
./gradlew clean dist -x createInstaller
```

## Author
Samir Vaidya (syvaidya [at] gmail)

## Homepage
https://www.openstego.com

## License
GNU General Public License 2.0 (GPL) (see ```LICENSE``` file)

## Acknowledgement
The digital watermarking code in this product is based on the code provided by Peter Meerwald. Refer to his excellent thesis on [watermarking](http://www.cosy.sbg.ac.at/~pmeerw/Watermarking/): Peter Meerwald, Digital Image Watermarking in the Wavelet Transfer Domain, Master's Thesis, Department of Scientific Computing, University of Salzburg, Austria, January 2001.
