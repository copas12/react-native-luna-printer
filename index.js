import { NativeModules } from 'react-native';
import EPToolkit from 'escpos-printer-toolkit';
const { LunaPrinter } = NativeModules;
const defaultOpt = {
  beep: false,
  cut: false,
  tailingLine: false,
  encoding: 'GBK'
};

const usbPrinter = {
  getList: LunaPrinter.usbPrinterList,
  connect: LunaPrinter.usbCloseConnection,
  disconnect: LunaPrinter.usbCloseConnection,
  raw: LunaPrinter.usbPrintRawData,
  printText: async text => {
    const opt = defaultOpt;
    const buffer = EPToolkit.exchange_text(text, opt);
    return await LunaPrinter.usbPrintRawData(buffer.toString('base64'));
  },
  printPicture: async base64 => {
    return await LunaPrinter.usbPrintPicture(base64);
  }
};
export { usbPrinter };
