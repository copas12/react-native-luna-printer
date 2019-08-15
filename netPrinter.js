import { NativeModules } from 'react-native';
const { LunaNetworkPrinter } = NativeModules;
export const netPrinter = {
  // printText: LunaNetworkPrinter.printText,
  print: LunaNetworkPrinter.print
};
