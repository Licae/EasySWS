using System;

namespace enablevjoy
{
    class Program
    {
        static string vJoyInstanceId;
        static Guid hdiGuid = new("{745a17a0-74d3-11d0-b6fe-00a0c90f57da}");
        static int Main(string[] args)
        {
            try
            {
                var deviceInfo = DeviceInstallation.EnumDevices(hdiGuid);

                foreach (var device in deviceInfo)
                {
                    if (device.Description == "vJoy Device")
                    {
                        vJoyInstanceId = device.InstanceId;
                        break;
                    }
                }
                DeviceInstallation.EnableDevice(hdiGuid, vJoyInstanceId);
                return 0;
            } 
            catch
            {
                return 114514;
            }
        }
    }
}
