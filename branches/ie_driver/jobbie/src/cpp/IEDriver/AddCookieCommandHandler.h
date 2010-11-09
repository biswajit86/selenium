#ifndef WEBDRIVER_IE_ADDCOOKIECOMMANDHANDLER_H_
#define WEBDRIVER_IE_ADDCOOKIECOMMANDHANDLER_H_

#include "BrowserManager.h"
#include <ctime>

namespace webdriver {

class AddCookieCommandHandler : public WebDriverCommandHandler {
public:
	AddCookieCommandHandler(void) {
	}

	virtual ~AddCookieCommandHandler(void) {
	}

protected:
	void AddCookieCommandHandler::ExecuteInternal(BrowserManager *manager, std::map<std::string, std::string> locator_parameters, std::map<std::string, Json::Value> command_parameters, WebDriverResponse * response)
	{
		if (command_parameters.find("cookie") == command_parameters.end()) {
			response->set_status_code(400);
			response->m_value = "cookie";
		}

		Json::Value cookie_value = command_parameters["cookie"];
		std::string cookie_string(cookie_value["name"].asString() + "=" + cookie_value["value"].asString() + "; ");
		cookie_value.removeMember("name");
		cookie_value.removeMember("value");

		bool is_secure(cookie_value["secure"].asBool());
		if (is_secure) {
			cookie_string += "secure; ";
		}
		cookie_value.removeMember("secure");

		Json::Value expiry = cookie_value.get("expiry", Json::Value::null);
		if (!expiry.isNull()) {
			cookie_value.removeMember("expiry");
			if (expiry.isDouble()) {
				time_t expiration_time = expiry.asDouble();
				char raw_formatted_time[30];
				tm * time_info;
				time_info = gmtime(&expiration_time);
				std::string month = this->GetMonthName(time_info->tm_mon);
				std::string weekday = this->GetWeekdayName(time_info->tm_wday);
				std::string format_string = weekday + ", %d " + month + " %Y %H:%M:%S GMT";
				strftime(raw_formatted_time, 30 , format_string.c_str(), time_info);
				std::string formatted_time(&raw_formatted_time[0]);
				cookie_string += "expires=" + formatted_time + "; ";
			}

			// If a test sends both "expiry" and "expires", remove "expires"
			// from the cookie so that it doesn't get added when the string
			// properties of the JSON object are processed.
			Json::Value expires_value = cookie_value.get("expires", Json::Value::null);
			if (!expires_value.isNull()) {
				cookie_value.removeMember("expires");
			}
		}

		Json::Value::iterator it = cookie_value.begin();
		for (; it != cookie_value.end(); ++it) {
			std::string key = it.key().asString();
			std::string value = cookie_value[key].asString();
			if (value != "") {
				cookie_string += key + "=" + cookie_value[key].asString() + "; ";
			}
		}

		BrowserWrapper *browser_wrapper;
		manager->GetCurrentBrowser(&browser_wrapper);

		std::wstring cookie(CA2W(cookie_string.c_str(), CP_UTF8));
		int status_code = browser_wrapper->AddCookie(cookie);
		if (status_code != SUCCESS) {
			response->m_value["message"] = L"Unable to add cookie to page";
		}

		response->set_status_code(status_code);
	}

private:
	std::string AddCookieCommandHandler::GetMonthName(int month_name) {
		// NOTE: can cookie dates used with put_cookie be localized?
		// If so, this function is not needed and a simple call to 
		// strftime() will suffice.
		switch (month_name) {
			case 0:
				return "Jan";
			case 1:
				return "Feb";
			case 2:
				return "Mar";
			case 3:
				return "Apr";
			case 4:
				return "May";
			case 5:
				return "Jun";
			case 6:
				return "Jul";
			case 7:
				return "Aug";
			case 8:
				return "Sep";
			case 9:
				return "Oct";
			case 10:
				return "Nov";
			case 11:
				return "Dec";
		}
	}

	std::string AddCookieCommandHandler::GetWeekdayName(int weekday_name) {
		// NOTE: can cookie dates used with put_cookie be localized?
		// If so, this function is not needed and a simple call to 
		// strftime() will suffice.
		switch (weekday_name) {
			case 0:
				return "Sun";
			case 1:
				return "Mon";
			case 2:
				return "Tue";
			case 3:
				return "Wed";
			case 4:
				return "Thu";
			case 5:
				return "Fri";
			case 6:
				return "Sat";
		}
	}
};

} // namespace webdriver

#endif // WEBDRIVER_IE_ADDCOOKIECOMMANDHANDLER_H_