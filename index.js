import { NativeModules } from 'react-native';
import EPToolkit from 'escpos-printer-toolkit';
const { LunaUsbPrinter, BluetoothManager, LunaBTPrinter, LunaNetworkPrinter, } = NativeModules;
const defaultOpt = {
  beep: false,
  cut: false,
  tailingLine: false,
  encoding: 'GBK'
};

const usbPrinter = {
  getList: LunaUsbPrinter.usbPrinterList,
  connect: LunaUsbPrinter.usbPrinterConnect,
  disconnect: LunaUsbPrinter.usbCloseConnection,
  printRaw: LunaUsbPrinter.usbPrintRawData,
  printText: async text => {
    const opt = defaultOpt;
    const buffer = EPToolkit.exchange_text(text, opt);
    return await LunaUsbPrinter.usbPrintRawData(buffer.toString('base64'));
  },
  printPicture: async base64 => {
    return await LunaUsbPrinter.usbPrintPicture(base64);
  }
};

const netPrinter = {
  printText: LunaNetworkPrinter.printText,
  print: LunaNetworkPrinter.print,
}

const btPrinter = {
  enable: BluetoothManager.enableBluetooth,
  connect: BluetoothManager.connect,
  disconnect: BluetoothManager.disconnect,
  printText: LunaBTPrinter.printText,
  printPicture: LunaBTPrinter.printPic,
  openCashDrawer: () => {
    LunaBTPrinter.openCashDrawer(0, 25, 250);
  }
}
export { usbPrinter, btPrinter, netPrinter };
