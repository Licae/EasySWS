using System;
using System.IO;
using System.Text;
using InTheHand.Net.Bluetooth;
using InTheHand.Net.Sockets;

namespace swsServer
{
    class Program
    {
        static void Main(string[] args)
        {

            Guid UUID = new Guid("00001101-0000-1000-8000-00805F9B34FB");
            var bluetoothClient = new BluetoothClient();
            var bluetoothRadio = BluetoothRadio.Default;
            var bluetoothListener = new BluetoothListener(UUID);
            Joystick.Init();

            if(bluetoothRadio != null)
            {
                getDeviceInfo(bluetoothRadio, bluetoothClient);
                bluetoothListener.Start();
                while (true)
                {
                    bluetoothClient = bluetoothListener.AcceptBluetoothClient();
                    Console.WriteLine($"{bluetoothClient.RemoteMachineName} 已经连接");
                    var mStream = bluetoothClient.GetStream();
                    mStream.Write(Encoding.UTF8.GetBytes("connectedddddddddddddddd\ndddddddddddddddd"));

                    while (bluetoothClient.Client.Connected)
                    {
                        try
                        {
                            byte[] received = new byte[1024];
                            var resultCode = mStream.Read(received, 0, received.Length);

                            if (resultCode == 0)
                            {
                                throw new IOException();
                            }
                            else
                            {
                                var receivedString = Encoding.UTF8.GetString(received);
                                Console.WriteLine($"服务器端接收到消息:{receivedString}");
                            }
                        }
                        catch (Exception ex)
                        {
                            Console.WriteLine($"设备 {bluetoothClient.RemoteMachineName} 已断开连接\n {ex.ToString()}");
                            break;
                        }
                    }
                }

            }
        }

        static public void getDeviceInfo(BluetoothRadio bluetoothRadio, BluetoothClient bluetoothClient)
        {
            Console.WriteLine($"name is {bluetoothRadio.Name}\n" +
                $"address is {bluetoothRadio.LocalAddress}\n" +
                $"Mode is {bluetoothRadio.Mode}");

            Console.WriteLine("\n");

            foreach (BluetoothDeviceInfo device in bluetoothClient.PairedDevices)
            {
                Console.WriteLine($"已配对的设备 {device.DeviceName}");
            }
        }
    }
}
