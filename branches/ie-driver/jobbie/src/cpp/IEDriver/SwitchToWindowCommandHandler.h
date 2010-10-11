#pragma once
#include "WebDriverCommandHandler.h"
#include "BrowserManager.h"

class SwitchToWindowCommandHandler :
	public WebDriverCommandHandler
{
public:

	SwitchToWindowCommandHandler(void)
	{
	}

	virtual ~SwitchToWindowCommandHandler(void)
	{
	}

protected:

	void ExecuteInternal(BrowserManager *manager, std::map<std::string, std::string> locatorParameters, std::map<std::string, std::string> commandParameters, WebDriverResponse * response)
	{
		if (locatorParameters.find("name") == locatorParameters.end())
		{
			response->m_statusCode = 400;
			response->m_value = "name";
		}
		else
		{
			std::wstring foundBrowserHandle = L"";
			std::string desiredName = locatorParameters["name"];
			std::map<std::wstring, BrowserWrapper>::iterator end = manager->m_trackedBrowsers.end();
			for (std::map<std::wstring, BrowserWrapper>::iterator it = manager->m_trackedBrowsers.begin(); it != end; ++it)
			{
				std::string browserName = it->second.getWindowName();
				if (browserName == desiredName)
				{
					foundBrowserHandle = it->first;
					break;
				}

				std::string browserHandle = CW2A(it->first.c_str());
				if (browserHandle == desiredName)
				{
					foundBrowserHandle = it->first;
					break;
				}
			}

			if (foundBrowserHandle == L"")
			{
				response->m_statusCode = ENOSUCHWINDOW;
			}
			else
			{
				// The NoCommand value will act as a no-op, causing the
				// driver to wait until the target browsers finish processing
				// before returning.
				WebDriverCommand waitCommand;
				waitCommand.m_commandValue = CommandValue::NoCommand;
				manager->DispatchCommand(&waitCommand);
				manager->m_currentBrowser = foundBrowserHandle;
				manager->DispatchCommand(&waitCommand);
			}
		}
	}
};
