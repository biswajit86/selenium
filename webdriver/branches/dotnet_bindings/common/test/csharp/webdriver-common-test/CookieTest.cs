﻿using System;
using System.Collections.Generic;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;
using OpenQa.Selenium.Internal;

namespace OpenQa.Selenium
{
    [TestFixture]
    public class CookieTest
    {
        [Test]
        public void CanCreateAWellFormedCookie()
        {
            new ReturnedCookie("Fish", "cod", "", "", DateTime.Now, false);
        }

        [Test]
        [ExpectedException(typeof(ArgumentOutOfRangeException))]
        public void ShouldThrowAnExceptionWhenSemiColonExistsInTheCookieAttribute()
        {
            new ReturnedCookie("hi;hi", "value", null, null, DateTime.Now, false);
        }

        [Test]
        [ExpectedException(typeof(ArgumentOutOfRangeException))]
        public void ShouldThrowAnExceptionTheNameIsNull()
        {
            new ReturnedCookie(null, "value", null, null, DateTime.Now, false);

        }

        [Test]
        public void CookiesShouldAllowSecureToBeSet()
        {
            Cookie cookie = new ReturnedCookie("name", "value", "", "/", DateTime.Now, true);
            Assert.IsTrue(cookie.Secure);
        }
    }
}
