using System;
using System.Collections.Generic;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;
using OpenQa.Selenium.Environment;

namespace OpenQa.Selenium
{
    [TestFixture]
    public class CookieImplementationTest : DriverTestFixture
    {
        [Test]
        public void ShouldAddCookie()
        {
            driver.Url = macbethPage;
            IOptions options = driver.Manage();
            Cookie cookie = new Cookie("cookie", "monster");
            options.AddCookie(cookie);
            Assert.That(options.GetCookies().ContainsKey(cookie.Name),
                "Cookie was not added successfully");
        }

        [Test]
        public void ShouldDeleteCookie()
        {
            driver.Url = macbethPage;
            IOptions options = driver.Manage();
            Cookie cookieToDelete = new Cookie("answer", "42");
            Cookie cookieToKeep = new Cookie("canIHaz", "Cheeseburguer");
            options.AddCookie(cookieToDelete);
            options.AddCookie(cookieToKeep);
            Dictionary<String, Cookie> cookies = options.GetCookies();
            options.DeleteCookie(cookieToDelete);
            cookies = options.GetCookies();
            Assert.IsFalse(cookies.ContainsKey(cookieToDelete.Name),
                "Cookie was not deleted successfully");
            Assert.That(cookies.ContainsKey(cookieToKeep.Name),
                "Valid cookie was not returned");
        }

        [Test]
        public void ShouldDeleteCookieNamed()
        {
            driver.Url = macbethPage;
            IOptions options = driver.Manage();
            Cookie cookieToDelete = new Cookie("answer", "42");
            Cookie cookieToKeep = new Cookie("question", "dunno");
            options.AddCookie(cookieToDelete);
            options.AddCookie(cookieToKeep);
            options.DeleteCookieNamed(cookieToDelete.Name);
            Dictionary<String, Cookie> cookies = options.GetCookies();
            Assert.IsTrue(cookies.ContainsKey(cookieToKeep.Name),
                "Valid cookie was not returned");
            Cookie deletedCookie;
            cookies.TryGetValue(cookieToDelete.Name, out deletedCookie);
            Assert.That(deletedCookie, Is.Null,
                "Cookie was not deleted successfully: ");
        }

        [Test]
        public void ShouldAddCookieToCurrentDomain()
        {
            driver.Url = macbethPage;
            IOptions options = driver.Manage();
            Cookie cookie = new Cookie("Marge", "Simpson", "/", "");
            options.AddCookie(cookie);
            Dictionary<String, Cookie> cookies = options.GetCookies();
            Assert.That(cookies.ContainsKey(cookie.Name),
                "Valid cookie was not returned");
        }

        [Test]
        public void ShouldAddCookieToCurrentDomainAndPath()
        {
            driver.Url = macbethPage;
            IOptions options = driver.Manage();
            Cookie cookie = new Cookie("Homer", "Simpson", "/" + EnvironmentManager.Instance.UrlBuilder.Path, EnvironmentManager.Instance.UrlBuilder.HostName);
            options.AddCookie(cookie);
            Dictionary<String, Cookie> cookies = options.GetCookies();
            Assert.That(cookies.ContainsKey(cookie.Name),
                "Valid cookie was not returned");
        }

        [Test]
        public void ShouldNotShowCookieAddedToDifferentDomain()
        {
            driver.Url = macbethPage;
            IOptions options = driver.Manage();
            Cookie cookie = new Cookie("Bart", "Simpson", EnvironmentManager.Instance.UrlBuilder.Path, EnvironmentManager.Instance.UrlBuilder.HostName + ".com");
            options.AddCookie(cookie);
            Dictionary<String, Cookie> cookies = options.GetCookies();
            Assert.IsFalse(cookies.ContainsKey(cookie.Name),
                "Invalid cookie was returned");
        }

        [Test]
        public void ShouldNotShowCookieAddedToDifferentPath()
        {
            driver.Url = macbethPage;
            IOptions options = driver.Manage();
            Cookie cookie = new Cookie("Lisa", "Simpson", EnvironmentManager.Instance.UrlBuilder.Path + "IDoNotExist", EnvironmentManager.Instance.UrlBuilder.HostName);
            options.AddCookie(cookie);
            Dictionary<String, Cookie> cookies = options.GetCookies();
            Assert.IsFalse(cookies.ContainsKey(cookie.Name),
                "Invalid cookie was returned");
        }

        [Test]
        //TODO(andre.nogueira): Revisit after correct exceptions are created
        [ExpectedException(typeof(Exception))]
        public void ShouldThrowExceptionWhenAddingCookieToNonExistingDomain()
        {
            //TODO(andre.nogueira): This fails if the machine is not connected to the internet
            //IE blocks asking if I want to work offline or not.
            driver.Url = "www.doesnotexist.comx";
            IOptions options = driver.Manage();
            Cookie cookie = new Cookie("question", "dunno");
            options.AddCookie(cookie);
        }
    }
}
