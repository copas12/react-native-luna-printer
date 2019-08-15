import { NativeModules } from 'react-native';
const { BluetoothManager, LunaBTPrinter } = NativeModules;
const { default: PQueue } = require('p-queue');

const queue = new PQueue({ concurrency: 1 });
let count = 0;
queue.on('active', () => {
  console.log(
    `Working on item #${++count}.  Size: ${queue.size}  Pending: ${
      queue.pending
    }`
  );
});

export const btPrinter = {
  enable: BluetoothManager.enableBluetooth,
  connect: BluetoothManager.connect,
  disconnect: BluetoothManager.disconnect,
  printText: (text, opt = {}) => {
    LunaBTPrinter.printText(text, opt);
  },
  printPicture: (base64Image, opt = {}) => {
    LunaBTPrinter.printPic(base64Image, opt);
  },
  openCashDrawer: () => {
    LunaBTPrinter.openCashDrawer(0, 25, 250);
  },
  print
};

const print = async (macAddress, lines, base64Image, openCashDrawer = false) => {
  console.log({ macAddress, lines, base64Image });

  const job = async () => {
    await btPrinter.connect(macAddress);
    if (base64Image) {
      await LunaBTPrinter.picBase64(base64Image, 250, 0);
    }
    lines.forEach(async text => {
      await btPrinter.printText(text + '\n');
    });
    await btPrinter.printText('\n');
    if (openCashDrawer) LunaBTPrinter.openCashDrawer(0, 25, 250);
    // await btPrinter.disconnect();
  };
  queue.add(job);
};
