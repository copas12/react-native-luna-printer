# react-native-luna-printer

## Getting started

`$ npm install github:copas12/react-native-luna-printer --save`

### Mostly automatic installation

`$ react-native link react-native-luna-printer`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-luna-printer` and add `LunaPrinter.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libLunaPrinter.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.lunaprinter.LunaPrinterPackage;` to the imports at the top of the file
  - Add `new LunaPrinterPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-luna-printer'
  	project(':react-native-luna-printer').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-luna-printer/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-luna-printer')
  	```


## Usage
```javascript
import { usbPrinter, btPrinter, netPrinter } from 'react-native-luna-printer';

** USB
// Get List 
usbPrinter.getList().then(devices => console.log(devices))

// Print
usbPrinter.print(vendorId: string, productId string, lines: string[], base64Image?: string, openCashDrawer: boolean = false) 

** Bluetooth
// Get List
btPrinter.enable().then(devices => console.log(devices))

// Print
btPrinter.print(macAddress: string, lines: string[], base64Image?: string, openCashDrawer: boolean = false)

** Network
// Print
netPrinter.print(ip: string,port: number,lines: string[], base64logo: string, openCashDrawer: boolean = false)

```
