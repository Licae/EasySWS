using System;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Threading;
using System.Threading.Tasks;
using vJoyInterfaceWrap;

namespace swsServer
{
    static class Joystick
    {
        private static vJoy _joystick = new ();
        private static vJoy.JoystickState _joystickState = new ();

        static public void Init()
        {
            if (!_joystick.vJoyEnabled())
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
                    if(ex.NativeErrorCode == 1223) // Windows 的错误码
                    {
                        Console.WriteLine("自动开启 vJoy 驱动失败，请手动到设备管理器->人体工学设备中启用\n");
                    }
                    Environment.Exit(666);
                }
            }
            else
            {
                Console.WriteLine($"vJoy 驱动正在运行！vJoy 版本: {_joystick.GetvJoyVersion()}\n");
            }

            _joystickState.bDevice = GetDeviceId();

            _joystickState.AxisX = 1;
            _joystickState.AxisY = 2;
            _joystickState.AxisZ = 3;
            _joystickState.AxisXRot = 4;
            _joystickState.AxisYRot = 5;
            _joystickState.AxisZRot = 6;
            _joystickState.Slider = 7;
            _joystickState.Dial = 8;
            _joystick.AcquireVJD(_joystickState.bDevice);
        }
        static public void DetectData(byte[] data)
        {
            var type = data[1];

            switch(type)
            {
                case 1: // buttons
                    var value = (uint)data[5] * 1000 + (uint)data[4] * 100 + (uint)data[3] * 10 + (uint)data[2];
                    Console.WriteLine(value);
                    _joystickState.Buttons = value;
                    _joystick.UpdateVJD(_joystickState.bDevice, ref _joystickState);
                    break;
            }
        }



        private static Byte GetDeviceId()
        {
            Byte deviceId = 0;
            Boolean acquire = false;
            for (uint id = 1; id <= 16; id++)
            {
                VjdStat status = _joystick.GetVJDStatus(id);
                if (status is VjdStat.VJD_STAT_FREE or VjdStat.VJD_STAT_OWN)
                {
                    if (_joystick.GetVJDButtonNumber(id) >= 8)
                    {
                        bool satisfy = true;
                        for (int axis = (int)HID_USAGES.HID_USAGE_X;
                            axis <= (int)HID_USAGES.HID_USAGE_SL1; axis++)
                        {
                            if (!_joystick.GetVJDAxisExist(id, (HID_USAGES)axis))
                            {
                                satisfy = false;
                                break;
                            }
                        }
                        if (satisfy)
                        {
                            deviceId = (byte)id;
                            acquire = status is VjdStat.VJD_STAT_FREE;
                            break;
                        }
                    }
                }
            }
            return deviceId;
        }

    }
}
