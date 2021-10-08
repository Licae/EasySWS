using System;
using vJoyInterfaceWrap;


namespace swsServer
{
    static class vjoyUtils
    {
        static vJoy joystick = new vJoy();
        static public void Init()
        {

            if (!joystick.vJoyEnabled())
            {
                Console.WriteLine("VJoy 驱动未开启，请检查设备管理器");
                return;
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
