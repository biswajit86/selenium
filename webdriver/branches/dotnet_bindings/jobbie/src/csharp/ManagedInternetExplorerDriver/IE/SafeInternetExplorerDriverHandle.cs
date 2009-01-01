using Microsoft.Win32.SafeHandles;
using System.Runtime.InteropServices;
using System;

namespace OpenQa.Selenium.IE
{
    internal class SafeInternetExplorerDriverHandle : SafeHandleZeroOrMinusOneIsInvalid
    {
        internal SafeInternetExplorerDriverHandle()
            : base(true)
        {
        }

        [DllImport("InternetExplorerDriver")]
        private static extern void wdFreeDriver(IntPtr driver);

        [DllImport("InternetExplorerDriver")]
        private static extern void wdClose(IntPtr driver);

        protected override bool ReleaseHandle()
        {
            wdClose(handle);
            wdFreeDriver(handle);
            // TODO(simonstewart): Are we really always successful?
            return true;
        }
    }
}
