import { NativeModules } from 'react-native';
import EPToolkit from 'escpos-printer-toolkit';
const { LunaUSBPrinter } = NativeModules;
const defaultOpt = {
  beep: false,
  cut: false,
  tailingLine: false,
  encoding: 'GBK'
};

const usbPrinter = {
  getList: LunaUSBPrinter.usbPrinterList,
  connect: LunaUSBPrinter.usbCloseConnection,
  disconnect: LunaUSBPrinter.usbCloseConnection,
  raw: LunaUSBPrinter.usbPrintRawData,
  printText: async text => {
    const opt = defaultOpt;
    const buffer = EPToolkit.exchange_text(text, opt);
    return await LunaUSBPrinter.usbPrintRawData(buffer.toString('base64'));
  },
  printPicture: async base64 => {
    return await LunaUSBPrinter.usbPrintPicture(base64);
  }
};
export { usbPrinter };
