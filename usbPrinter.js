import { NativeModules } from 'react-native';
import EPToolkit from 'escpos-printer-toolkit';
const { LunaUsbPrinter } = NativeModules;
const defaultOpt = {
  beep: false,
  cut: false,
  tailingLine: false,
  encoding: 'GBK'
};

export const usbPrinter = {
  getList: LunaUsbPrinter.usbPrinterList,
  connect: LunaUsbPrinter.usbPrinterConnect,
  disconnect: LunaUsbPrinter.usbCloseConnection,
  printRaw: LunaUsbPrinter.usbPrintRawData,
  printText,
  printPicture: async base64 => {
    return await LunaUsbPrinter.usbPrintPicture(base64);
  },
  print
};
const printText = async text => {
    const opt = defaultOpt;
    const buffer = EPToolkit.exchange_text(text, opt);
    return await LunaUsbPrinter.usbPrintRawData(buffer.toString('base64'));
  }

const print = async (vendorId, productId, lines, base64Image, openCashDrawer = false) => {
    await LunaUsbPrinter.usbPrinterConnect(vendorId, productId);
    if (base64Image) await LunaUsbPrinter.usbPrintPicture(base64Image, {});
    lines.forEach( async text => {
        await printText(text);
    });
    if (openCashDrawer) {
        await LunaUsbPrinter.openCashDrawer();
    }
    await LunaUsbPrinter.usbCloseConnection();
}