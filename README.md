# OpenStego

OpenStego is a steganography application that provides two functionalities:

1. Data Hiding: It can hide any data within a cover file (e.g. images).

2. Watermarking: Watermarking files (e.g. images) with an invisible signature. It can be used to detect unauthorized file copying.

## Usage

* For GUI:

> `java -jar <path>\openstego.jar`

OR

> Use the bundled batch file or shell script to launch the GUI.

* For command line interface:

> Refer to [online documentation](http://www.openstego.com/cmdline.html).

## Plugins help
Please use the following command to get plugin specific help:

`java -jar <path>\openstego.jar -help -a <algorithm_name>`


## Developing new plugin

To add a new plugin, the following abstract class must be implemented:

`com.openstego.desktop.OpenStegoPlugin`

Read the API documentation for the details of the methods to be implemented. In addition, the following utility class can be used to handle multilingual string labels for the plugin:

`com.openstego.desktop.util.LabelUtil`

A new namespace should be added to `LabelUtil` class for each new plugin. Same namespace can also be used for exception messages while throwing `OpenStegoException`.

After implementing the plugin classes, create new file named `OpenStegoPlugins.external` and put the fully qualified name of the class which implements `OpenStegoPlugin` in the file. Make sure that this file is put directly under the CLASSPATH while invoking the application.

Please refer to the `com.openstego.desktop.plugin.lsb` package sources for sample plugin implementation.

### Author

Samir Vaidya (syvaidya [at] gmail)


### See Also

Project homepage: http://www.openstego.com

Blog: http://syvaidya.blogspot.com

### License

GNU General Public License 2.0 (GPL) (see ```LICENSE``` file)
