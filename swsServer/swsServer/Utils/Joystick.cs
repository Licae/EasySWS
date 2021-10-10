using System;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using vJoyInterfaceWrap;

namespace swsServer
{
    static class Joystick
    {
        static vJoy joystick = new ();
        static public void Init()
        {

            if (!joystick.vJoyEnabled())
            {
                Console.WriteLine("VJoy 驱动未开启\n正在自动帮你寻找并尝试开启驱动");
                ProcessStartInfo enablevjoy = new()
                {
                    FileName = Path.Combine(Environment.CurrentDirectory, "enablevjoy.exe"),
                    WorkingDirectory = Environment.CurrentDirectory,
                    Verb = "runas",
                    UseShellExecute = true
                };
                try
                {
                    var vjoy = Process.Start(enablevjoy);
                    vjoy.WaitForExit();
                    switch(vjoy.ExitCode)
                    {
                        case 0:
                            Console.WriteLine("开启成功");
                            break;
                        case 114514:
                            Console.WriteLine("开启失败，请查看是否安装 vJoy 驱动n");
                            break;
                    }
                } 
                catch(Win32Exception ex)
                {
                    if(ex.NativeErrorCode == 1223)
                    {
                        Console.WriteLine("自动开启 vJoy 驱动失败，请手动到设备管理器->人体工学设备中启用\n");
                    }
                }
            }
            else
            {    
                Console.WriteLine("Vendor: {0}\nProduct :{1}\nVersion Number:{2}\n",
                joystick.GetvJoyManufacturerString(),
                joystick.GetvJoyProductString(),
                joystick.GetvJoySerialNumberString());
            }
        }
    }
}
