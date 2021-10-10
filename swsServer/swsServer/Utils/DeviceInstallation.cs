using Microsoft.Win32.SafeHandles;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Runtime.InteropServices;
using System.Security;
using System.Text;

namespace ConsoleApp1
{
    [Flags]
    internal enum SetupDiGetClassDevsFlags
    {
        Default = 0x01,
        Present = 0x02,
        AllClasses = 0x04,
        Profile = 0x08,
        DeviceInterface = 0x10
    }

    internal enum DiFunction
    {
        SelectDevice = 0x01,
        InstallDevice = 0x02,
        AssignResources = 0x03,
        Properties = 0x04,
        Remove = 0x05,
        FirstTimeSetup = 0x06,
        FoundDevice = 0x07,
        SelectClassDrivers = 0x08,
        ValidateClassDrivers = 0x09,
        InstallClassDrivers = 0x0A,
        CalcDiskSpace = 0x0B,
        DestroyPrivateData = 0x0C,
        ValidateDriver = 0x0D,
        Detect = 0x0F,
        InstallWizard = 0x10,
        DestroyWizardData = 0x11,
        PropertyChange = 0x12,
        EnableClass = 0x13,
        DetectVerify = 0x14,
        InstallDeviceFiles = 0x15,
        UnRemove = 0x16,
        SelectBestCompatDrv = 0x17,
        AllowInstall = 0x18,
        RegisterDevice = 0x19,
        NewDeviceWizardPreSelect = 0x1A,
        NewDeviceWizardSelect = 0x1B,
        NewDeviceWizardPreAnalyze = 0x1C,
        NewDeviceWizardPostAnalyze = 0x1D,
        NewDeviceWizardFinishInstall = 0x1E,
        Unused1 = 0x1F,
        InstallInterfaces = 0x20,
        DetectCancel = 0x21,
        RegisterCoInstallers = 0x22,
        AddPropertyPageAdvanced = 0x23,
        AddPropertyPageBasic = 0x24,
        Reserved1 = 0x25,
        Troubleshooter = 0x26,
        PowerMessageWake = 0x27,
        AddRemotePropertyPageAdvanced = 0x28,
        UpdateDriverUI = 0x29,
        Reserved2 = 0x30
    }

    internal enum StateChangeAction
    {
        Enable = 1,
        Disable = 2,
        PropChange = 3,
        Start = 4,
        Stop = 5
    }

    [Flags]
    internal enum Scopes
    {
        Global = 1,
        ConfigSpecific = 2,
        ConfigGeneral = 4
    }

    internal enum SetupAPIError
    {
        NoAssociatedClass = unchecked((int)0xE0000200),
        ClassMismatch = unchecked((int)0xE0000201),
        DuplicateFound = unchecked((int)0xE0000202),
        NoDriverSelected = unchecked((int)0xE0000203),
        KeyDoesNotExist = unchecked((int)0xE0000204),
        InvalidDevinstName = unchecked((int)0xE0000205),
        InvalidClass = unchecked((int)0xE0000206),
        DevinstAlreadyExists = unchecked((int)0xE0000207),
        DevinfoNotRegistered = unchecked((int)0xE0000208),
        InvalidRegProperty = unchecked((int)0xE0000209),
        NoInf = unchecked((int)0xE000020A),
        NoSuchHDevinst = unchecked((int)0xE000020B),
        CantLoadClassIcon = unchecked((int)0xE000020C),
        InvalidClassInstaller = unchecked((int)0xE000020D),
        DiDoDefault = unchecked((int)0xE000020E),
        DiNoFileCopy = unchecked((int)0xE000020F),
        InvalidHwProfile = unchecked((int)0xE0000210),
        NoDeviceSelected = unchecked((int)0xE0000211),
        DevinfolistLocked = unchecked((int)0xE0000212),
        DevinfodataLocked = unchecked((int)0xE0000213),
        DiBadPath = unchecked((int)0xE0000214),
        NoClassInstallParams = unchecked((int)0xE0000215),
        FileQueueLocked = unchecked((int)0xE0000216),
        BadServiceInstallSect = unchecked((int)0xE0000217),
        NoClassDriverList = unchecked((int)0xE0000218),
        NoAssociatedService = unchecked((int)0xE0000219),
        NoDefaultDeviceInterface = unchecked((int)0xE000021A),
        DeviceInterfaceActive = unchecked((int)0xE000021B),
        DeviceInterfaceRemoved = unchecked((int)0xE000021C),
        BadInterfaceInstallSect = unchecked((int)0xE000021D),
        NoSuchInterfaceClass = unchecked((int)0xE000021E),
        InvalidReferenceString = unchecked((int)0xE000021F),
        InvalidMachineName = unchecked((int)0xE0000220),
        RemoteCommFailure = unchecked((int)0xE0000221),
        MachineUnavailable = unchecked((int)0xE0000222),
        NoConfigMgrServices = unchecked((int)0xE0000223),
        InvalidPropPageProvider = unchecked((int)0xE0000224),
        NoSuchDeviceInterface = unchecked((int)0xE0000225),
        DiPostProcessingRequired = unchecked((int)0xE0000226),
        InvalidCOInstaller = unchecked((int)0xE0000227),
        NoCompatDrivers = unchecked((int)0xE0000228),
        NoDeviceIcon = unchecked((int)0xE0000229),
        InvalidInfLogConfig = unchecked((int)0xE000022A),
        DiDontInstall = unchecked((int)0xE000022B),
        InvalidFilterDriver = unchecked((int)0xE000022C),
        NonWindowsNTDriver = unchecked((int)0xE000022D),
        NonWindowsDriver = unchecked((int)0xE000022E),
        NoCatalogForOemInf = unchecked((int)0xE000022F),
        DevInstallQueueNonNative = unchecked((int)0xE0000230),
        NotDisableable = unchecked((int)0xE0000231),
        CantRemoveDevinst = unchecked((int)0xE0000232),
        InvalidTarget = unchecked((int)0xE0000233),
        DriverNonNative = unchecked((int)0xE0000234),
        InWow64 = unchecked((int)0xE0000235),
        SetSystemRestorePoint = unchecked((int)0xE0000236),
        IncorrectlyCopiedInf = unchecked((int)0xE0000237),
        SceDisabled = unchecked((int)0xE0000238),
        UnknownException = unchecked((int)0xE0000239),
        PnpRegistryError = unchecked((int)0xE000023A),
        RemoteRequestUnsupported = unchecked((int)0xE000023B),
        NotAnInstalledOemInf = unchecked((int)0xE000023C),
        InfInUseByDevices = unchecked((int)0xE000023D),
        DiFunctionObsolete = unchecked((int)0xE000023E),
        NoAuthenticodeCatalog = unchecked((int)0xE000023F),
        AuthenticodeDisallowed = unchecked((int)0xE0000240),
        AuthenticodeTrustedPublisher = unchecked((int)0xE0000241),
        AuthenticodeTrustNotEstablished = unchecked((int)0xE0000242),
        AuthenticodePublisherNotTrusted = unchecked((int)0xE0000243),
        SignatureOSAttributeMismatch = unchecked((int)0xE0000244),
        OnlyValidateViaAuthenticode = unchecked((int)0xE0000245)
    }

    [StructLayout(LayoutKind.Sequential)]
    internal struct DeviceInfoData
    {
        public int Size;
        public Guid ClassGuid;
        public int DevInst;
        public IntPtr Reserved;
    }

    [StructLayout(LayoutKind.Sequential)]
    internal struct PropertyChangeParameters
    {
        public int Size;
        public DiFunction DiFunction;
        public StateChangeAction StateChange;
        public Scopes Scope;
        public int HwProfile;
    }

    internal static class NativeMethods
    {
        [DllImport("setupapi.dll", CallingConvention = CallingConvention.Winapi, SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool SetupDiCallClassInstaller(
            DiFunction installFunction, SafeDeviceInfoSetHandle deviceInfoSet,
            [In] ref DeviceInfoData deviceInfoData);

        [DllImport("setupapi.dll", CallingConvention = CallingConvention.Winapi, SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool SetupDiEnumDeviceInfo(
            SafeDeviceInfoSetHandle deviceInfoSet, int memberIndex,
            ref DeviceInfoData deviceInfoData);

        [DllImport("setupapi.dll", CallingConvention = CallingConvention.Winapi, SetLastError = true,
            CharSet = CharSet.Auto)]
        public static extern SafeDeviceInfoSetHandle SetupDiGetClassDevs(
            [In] ref Guid classGuid,
            [MarshalAs(UnmanagedType.LPWStr)] string enumerator,
            IntPtr hwndParent, SetupDiGetClassDevsFlags flags);

        [DllImport("setupapi.dll", CallingConvention = CallingConvention.Winapi, SetLastError = true,
            CharSet = CharSet.Auto)]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool SetupDiGetDeviceInstanceId(
           SafeDeviceInfoSetHandle deviceInfoSet,
           ref DeviceInfoData did,
           [MarshalAs(UnmanagedType.LPWStr)] StringBuilder deviceInstanceId,
           int deviceInstanceIdSize,
           out int requiredSize
        );

        [SuppressUnmanagedCodeSecurity()]
        [DllImport("setupapi.dll", CallingConvention = CallingConvention.Winapi, SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool SetupDiDestroyDeviceInfoList(IntPtr deviceInfoSet);

        [DllImport("setupapi.dll", CallingConvention = CallingConvention.Winapi, SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool SetupDiSetClassInstallParams(
            SafeDeviceInfoSetHandle deviceInfoSet,
            [In] ref DeviceInfoData deviceInfoData,
            [In] ref PropertyChangeParameters classInstallParams,
            int classInstallParamsSize);

        [DllImport("setupapi.dll", CallingConvention = CallingConvention.Winapi, SetLastError = true,
            CharSet = CharSet.Auto)]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool SetupDiGetDeviceRegistryProperty(
            SafeDeviceInfoSetHandle deviceInfoSet,
            [In] ref DeviceInfoData deviceInfoData,
            int property,
            out int propertyRegDataType,
            IntPtr propertyBuffer,
            int propertyBufferSize,
            out int requiredSize);

        [DllImport("cfgmgr32.dll", SetLastError = true)]
        public static extern int CM_Get_DevNode_Status(
            out int status, out int probNum, int devInst, int flags
        );

    }

    internal class SafeDeviceInfoSetHandle : SafeHandleZeroOrMinusOneIsInvalid
    {
        public SafeDeviceInfoSetHandle() : base(true) { }

        protected override bool ReleaseHandle()
        {
            return NativeMethods.SetupDiDestroyDeviceInfoList(handle);
        }
    }

    public struct DeviceInfo
    {
        public string Class;
        public Guid ClassGuid;
        public string Description;
        public string Manufacturer;
        public string InstanceId;
    }

    public class DeviceInstallation
    {
        public static DeviceInfo[] EnumDevices(Guid classGuid)
        {
            List<DeviceInfo> deviceInfos = new();

            SafeDeviceInfoSetHandle diSetHandle = null;
            try
            {
                diSetHandle = NativeMethods.SetupDiGetClassDevs(
                    ref classGuid, null, IntPtr.Zero, SetupDiGetClassDevsFlags.Present);
                DeviceInfoData[] diData = GetDeviceInfoData(diSetHandle);
                for (int index = 0; index < diData.Length; index++)
                {
                    DeviceInfoData infoData = diData[index];
                    DeviceInfo info = new()
                    {
                        Class = GetDeviceProperty(
                            diSetHandle, ref infoData, 0x00000007) as string,
                        ClassGuid = new Guid(GetDeviceProperty(
                            diSetHandle, ref infoData, 0x00000008) as string),
                        Description = GetDeviceProperty(
                            diSetHandle, ref infoData, 0x00000000) as string,
                        Manufacturer = GetDeviceProperty(
                            diSetHandle, ref infoData, 0x0000000B) as string,
                        InstanceId = GetDeviceInstanceId(diSetHandle, ref infoData)
                    };

                    deviceInfos.Add(info);
                }
            }
            finally
            {
                diSetHandle?.Close();
            }

            return deviceInfos.ToArray();
        }

        public static void EnableDevice(Guid classGuid, string instanceId)
        {
            SetDeviceEnabled(classGuid, instanceId, true);
        }

        public static void DisableDevice(Guid classGuid, string instanceId)
        {
            SetDeviceEnabled(classGuid, instanceId, false);
        }

        public static void RestartDevice(Guid classGuid, string instanceId)
        {
            SafeDeviceInfoSetHandle diSetHandle = null;
            try
            {
                diSetHandle = NativeMethods.SetupDiGetClassDevs(
                    ref classGuid, null, IntPtr.Zero, SetupDiGetClassDevsFlags.Present);
                DeviceInfoData[] diData = GetDeviceInfoData(diSetHandle);
                int index = GetIndexOfInstance(diSetHandle, diData, instanceId);
                DevicePropertyChange(diSetHandle, diData[index], StateChangeAction.PropChange);
            }
            finally
            {
                diSetHandle?.Close();
            }
        }

        public static bool IsDeviceEnabled(Guid classGuid, string instanceId)
        {
            SafeDeviceInfoSetHandle diSetHandle = null;
            try
            {
                diSetHandle = NativeMethods.SetupDiGetClassDevs(
                    ref classGuid, null, IntPtr.Zero, SetupDiGetClassDevsFlags.Present);
                DeviceInfoData[] diData = GetDeviceInfoData(diSetHandle);
                int index = GetIndexOfInstance(diSetHandle, diData, instanceId);
                if (NativeMethods.CM_Get_DevNode_Status(
                    out int status, out int probNum, diData[index].DevInst, 0) == 0)
                    return probNum == 0;
            }
            finally
            {
                diSetHandle?.Close();
            }

            return false;
        }

        private static object GetDeviceProperty(
            SafeDeviceInfoSetHandle deviceInfoSet, ref DeviceInfoData deviceInfoData, int property)
        {
            object ret = null;

            if (!NativeMethods.SetupDiGetDeviceRegistryProperty(
                deviceInfoSet, ref deviceInfoData, property, out int type, IntPtr.Zero, 0, out int requiredSize)
                && Marshal.GetLastWin32Error() == 122)
            {
                IntPtr buffer = Marshal.AllocHGlobal(requiredSize);
                if (NativeMethods.SetupDiGetDeviceRegistryProperty(
                    deviceInfoSet, ref deviceInfoData, property, out _, buffer, requiredSize, out _))
                {
                    switch (type)
                    {
                        case 1:
                            ret = Marshal.PtrToStringAuto(buffer);
                            break;
                        default:
                            byte[] bufBytes = new byte[requiredSize];
                            Marshal.Copy(buffer, bufBytes, 0, requiredSize);
                            ret = bufBytes;
                            break;
                    }
                }
                Marshal.FreeHGlobal(buffer);
            }

            return ret;
        }

        private static string GetDeviceInstanceId(
            SafeDeviceInfoSetHandle deviceInfoSet, ref DeviceInfoData deviceInfoData)
        {
            if (!NativeMethods.SetupDiGetDeviceInstanceId(
                deviceInfoSet, ref deviceInfoData, null, 0, out int requiredSize)
                && Marshal.GetLastWin32Error() == 122)
            {
                StringBuilder sb = new(requiredSize);
                if (NativeMethods.SetupDiGetDeviceInstanceId(
                    deviceInfoSet, ref deviceInfoData, sb, requiredSize, out _))
                    return sb.ToString();
            }

            return null;
        }

        private static void SetDeviceEnabled(Guid classGuid, string instanceId, bool enable)
        {
            SafeDeviceInfoSetHandle diSetHandle = null;
            try
            {
                diSetHandle = NativeMethods.SetupDiGetClassDevs(
                    ref classGuid, null, IntPtr.Zero, SetupDiGetClassDevsFlags.Present);
                DeviceInfoData[] diData = GetDeviceInfoData(diSetHandle);
                int index = GetIndexOfInstance(diSetHandle, diData, instanceId);
                DevicePropertyChange(
                    diSetHandle, diData[index],
                    enable ? StateChangeAction.Enable : StateChangeAction.Disable);
            }
            finally
            {
                diSetHandle?.Close();
            }
        }

        private static DeviceInfoData[] GetDeviceInfoData(SafeDeviceInfoSetHandle handle)
        {
            List<DeviceInfoData> data = new();

            DeviceInfoData did = new();
            int didSize = Marshal.SizeOf(did);
            did.Size = didSize;
            int index = 0;

            while (NativeMethods.SetupDiEnumDeviceInfo(handle, index, ref did))
            {
                data.Add(did);
                index++;
                did = new();
                did.Size = didSize;
            }

            return data.ToArray();
        }

        private static int GetIndexOfInstance(
            SafeDeviceInfoSetHandle handle, DeviceInfoData[] diData, string instanceId)
        {
            for (int index = 0; index < diData.Length; index++)
            {
                if (instanceId == GetDeviceInstanceId(handle, ref diData[index]))
                    return index;
            }

            return -1;
        }

        private static void DevicePropertyChange(
            SafeDeviceInfoSetHandle handle, DeviceInfoData diData, StateChangeAction action)
        {
            PropertyChangeParameters param = new()
            {
                Size = 8,
                DiFunction = DiFunction.PropertyChange,
                Scope = Scopes.Global,
                StateChange = action
            };

            if (!NativeMethods.SetupDiSetClassInstallParams(
                handle, ref diData, ref param, Marshal.SizeOf(param)))
                throw new Win32Exception(Marshal.GetLastWin32Error());

            if (!NativeMethods.SetupDiCallClassInstaller(
                DiFunction.PropertyChange, handle, ref diData))
            {
                int err = Marshal.GetLastWin32Error();
                if (err == (int)SetupAPIError.NotDisableable)
                    throw new ArgumentException("The specified device cannot be disabled.");
                else if (err >= (int)SetupAPIError.NoAssociatedClass
                    && err <= (int)SetupAPIError.OnlyValidateViaAuthenticode)
                    throw new Win32Exception("SetupAPI Error: " + ((SetupAPIError)err).ToString());
                else
                    throw new Win32Exception(Marshal.GetLastWin32Error());
            }
        }
    }
}
