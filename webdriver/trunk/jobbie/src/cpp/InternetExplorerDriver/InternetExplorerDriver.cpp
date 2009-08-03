/*
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.
Portions copyright 2007 ThoughtWorks, Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

#include "StdAfx.h"
#include "utils.h"
#include "InternalCustomMessage.h"
#include "errorcodes.h"
#include "jsxpath.h"
#include "logging.h"
#include <mshtml.h>
#include <SHLGUID.h>

using namespace std;
IeThread* g_IE_Thread = NULL;

InternetExplorerDriver::InternetExplorerDriver() : p_IEthread(NULL)
{
	if (NULL == gSafe) {
		gSafe = new safeIO();
	}
	SCOPETRACER
	speed = 0;
	
	p_IEthread = ThreadFactory();
	p_IEthread->pIED = this;

	ResetEvent(p_IEthread->sync_LaunchIE);
	p_IEthread->PostThreadMessageW(_WD_START, 0, 0);
	WaitForSingleObject(p_IEthread->sync_LaunchIE, 60000);

	closeCalled = false;
}

InternetExplorerDriver::InternetExplorerDriver(InternetExplorerDriver *other)
{
	ScopeTracer D(("Constructor_from_other"));
	this->p_IEthread = other->p_IEthread;
}

InternetExplorerDriver::~InternetExplorerDriver()
{
	SCOPETRACER
	close();
}

IeThread* InternetExplorerDriver::ThreadFactory()
{
	SCOPETRACER
	if(!g_IE_Thread) 
	{
		// Spawning the GUI worker thread, which will instantiate the ActiveX component
		g_IE_Thread = p_IEthread = new IeThread();
		p_IEthread->hThread = CreateThread (NULL, 0, (DWORD (__stdcall *)(LPVOID)) (IeThread::runProcessStatic), 
					(void *)p_IEthread, 0, NULL);

		p_IEthread->pIED = this;
		ResetEvent(p_IEthread->sync_LaunchThread);
		ResumeThread(p_IEthread->hThread); 
		WaitForSingleObject(p_IEthread->sync_LaunchThread, 60000);
	}

	return g_IE_Thread;
}

void InternetExplorerDriver::close()
{
	SCOPETRACER
	if (closeCalled)
		return;

	closeCalled = true;

	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_QUIT_IE,)
}

bool isIeServerWindow(HWND hwnd) 
{
	// 15 = "Internet Explorer_Server\0"
	char name[25];
	if (GetClassNameA(hwnd, name, 25) == 0) {
		return true;
	}

	if (strcmp("Internet Explorer_Server", name) != 0) {
		return true;
	}

	return false;
}

BOOL CALLBACK findServerWindows(HWND hwnd, LPARAM arg) 
{
	HRESULT hr = CoInitialize(NULL);
	if (FAILED(hr)) {
		LOG(WARN) << "Coinitialization failed: " << hr;
		return TRUE;
	}

	// http://support.microsoft.com/kb/249232	
	HINSTANCE library = LoadLibrary(L"oleacc.dll");
	if (!library) {
		LOG(WARN) << "No oleacc library";
		return TRUE;
	}

   UINT nMsg = RegisterWindowMessageW(L"WM_HTML_GETOBJECT");
   LRESULT lresult;
   if (SendMessageTimeoutA(hwnd, nMsg, 0L, 0L, SMTO_ABORTIFHUNG, 1000, (PDWORD_PTR)&lresult) == 0) {
	   LOG(WARN) << "Timed out sending message for html object";
	   FreeLibrary(library);
	   return TRUE;
   }
   LPFNOBJECTFROMLRESULT object = reinterpret_cast<LPFNOBJECTFROMLRESULT>(GetProcAddress(library, "ObjectFromLresult"));
   if (!object) {
	   LOG(WARN) << "Unable to begin to access document from window handle";
	   FreeLibrary(library);
	   return TRUE;
   }

   CComPtr<IHTMLDocument2> document;
   (* object)(lresult, IID_IHTMLDocument2, 0, reinterpret_cast<void **>(&document));
   if (!document) {
	   LOG(DEBUG) << "Unable to access document from window handle";
	   FreeLibrary(library);
	   return TRUE;
   } 

   CComPtr<IHTMLWindow2> window;
   if (!SUCCEEDED(document->get_parentWindow(&window))) {
	   LOG(WARN) << "Unable to access parent window from window handle";
	   FreeLibrary(library);
	   return TRUE;
   }

	// http://support.microsoft.com/kb/257717
	CComQIPtr<IServiceProvider> provider(window);
	if (!provider) {
		LOG(WARN) << "Cannot extract service provider";
		FreeLibrary(library);
		return TRUE;
	}
	CComPtr<IServiceProvider> childProvider;
	hr = provider->QueryService(SID_STopLevelBrowser, IID_IServiceProvider, reinterpret_cast<void **>(&childProvider));
	if (FAILED(hr)) {
		LOG(WARN) << "Cannot extract service provider from top-level";
		FreeLibrary(library);
		return TRUE;
	}
	IWebBrowser2* browser;
	hr = childProvider->QueryService(SID_SWebBrowserApp, IID_IWebBrowser2, reinterpret_cast<void **>(&browser));

	if (SUCCEEDED(hr)) {
		((vector<IWebBrowser2*>*)arg)->push_back(browser);
	}

	FreeLibrary(library);
	return TRUE;
}

BOOL CALLBACK findTopLevelWindows(HWND hwnd, LPARAM arg)
{
	// Could this be an IE instance?
	// 8 == "IeFrame\0"
	// 21 == "Shell DocObject View\0";
	char name[21];
	if (GetClassNameA(hwnd, name, 21) == 0) {
		// No match found. Skip
		return TRUE;
	}
	
	if (strcmp("IEFrame", name) != 0 && strcmp("Shell DocObject View", name) != 0) {
		return TRUE;
	}

	EnumChildWindows(hwnd, findServerWindows, arg);

	return TRUE;
}

void InternetExplorerDriver::quit() 
{
	// Discover all the IWebBrowser2 instances out there
	// and kill them
	vector<IWebBrowser2*> browsers;
	EnumWindows(findTopLevelWindows, (LPARAM)&browsers);

	for(vector<IWebBrowser2*>::iterator curr = browsers.begin(); 
        curr != browsers.end();
        curr++) {
		(*curr)->Quit();
		(*curr)->Release();
	}
}

bool InternetExplorerDriver::getVisible()
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GETVISIBLE,)
	return data.output_bool_;
}

void InternetExplorerDriver::setVisible(bool isVisible) 
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_SETVISIBLE, (int)isVisible)
}

LPCWSTR InternetExplorerDriver::getCurrentUrl()
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GETCURRENTURL,)
	return data.output_string_.c_str();
}

LPCWSTR InternetExplorerDriver::getPageSource()
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GETPAGESOURCE,)
	return data.output_string_.c_str();
}

LPCWSTR InternetExplorerDriver::getTitle()
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GETTITLE,)
	return data.output_string_.c_str();
}

void InternetExplorerDriver::get(const wchar_t *url)
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GETURL, url)
}

void InternetExplorerDriver::goForward() 
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GOFORWARD,)
}

void InternetExplorerDriver::goBack()
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GOBACK,)
}

std::wstring InternetExplorerDriver::getHandle()
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GET_HANDLE,);
	return data.output_string_.c_str();
}

ElementWrapper* InternetExplorerDriver::getActiveElement()
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GETACTIVEELEMENT,)
	
	return new ElementWrapper(this, data.output_html_element_);
}

int InternetExplorerDriver::selectElementByXPath(IHTMLElement *pElem, const wchar_t *input_string, ElementWrapper** element)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTBYXPATH)

	if (data.error_code != SUCCESS) { return data.error_code; };

	*element = new ElementWrapper(this, data.output_html_element_);
	return SUCCESS;
}

std::vector<ElementWrapper*>* InternetExplorerDriver::selectElementsByXPath(IHTMLElement *pElem, const wchar_t *input_string)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTSBYXPATH)

	std::vector<ElementWrapper*> *toReturn = new std::vector<ElementWrapper*>();

	if(data.output_long_) {std::wstring Err(L"Cannot find elements by Xpath"); throw Err;}

	std::vector<IHTMLElement*>& allElems = data.output_list_html_element_;
	std::vector<IHTMLElement*>::const_iterator cur, end = allElems.end();
	for(cur = allElems.begin();cur < end; cur++)
	{
		IHTMLElement* elem = *cur;
		toReturn->push_back(new ElementWrapper(this, elem));
	}
	return toReturn;
}

int InternetExplorerDriver::selectElementById(IHTMLElement *pElem, const wchar_t *input_string, ElementWrapper** element) 
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTBYID)

	if (data.error_code != SUCCESS) { return data.error_code; }

	*element = new ElementWrapper(this, data.output_html_element_);	
	return SUCCESS;
}

std::vector<ElementWrapper*>* InternetExplorerDriver::selectElementsById(IHTMLElement *pElem, const wchar_t *input_string)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTSBYID)

	if(1 == data.output_long_) {std::wstring Err(L"Cannot find elements by Id"); throw Err;}

	std::vector<ElementWrapper*> *toReturn = new std::vector<ElementWrapper*>();

	std::vector<IHTMLElement*>& allElems = data.output_list_html_element_;
	std::vector<IHTMLElement*>::const_iterator cur, end = allElems.end();
	for(cur = allElems.begin();cur < end; cur++)
	{
		IHTMLElement* elem = *cur;
		toReturn->push_back(new ElementWrapper(this, elem));
	}
	return toReturn;
}

int InternetExplorerDriver::selectElementByLink(IHTMLElement *pElem, const wchar_t *input_string, ElementWrapper** element)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTBYLINK)

	if (data.error_code == SUCCESS) { 
		*element = new ElementWrapper(this, data.output_html_element_);
	}

	return data.error_code;
}

std::vector<ElementWrapper*>* InternetExplorerDriver::selectElementsByPartialLink(IHTMLElement *pElem, const wchar_t *input_string)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTSBYPARTIALLINK)

	if(1 == data.output_long_) {std::wstring Err(L"Cannot find elements by Link"); throw Err;}

	std::vector<ElementWrapper*> *toReturn = new std::vector<ElementWrapper*>();

	std::vector<IHTMLElement*>& allElems = data.output_list_html_element_;
	std::vector<IHTMLElement*>::const_iterator cur, end = allElems.end();
	for(cur = allElems.begin();cur < end; cur++)
	{
		IHTMLElement* elem = *cur;
		toReturn->push_back(new ElementWrapper(this, elem));
	}
	return toReturn;
}

int InternetExplorerDriver::selectElementByPartialLink(IHTMLElement *pElem, const wchar_t *input_string, ElementWrapper** element)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTBYPARTIALLINK)

	if (data.error_code == SUCCESS) { 
		*element = new ElementWrapper(this, data.output_html_element_);
	}

	return data.error_code;
}

std::vector<ElementWrapper*>* InternetExplorerDriver::selectElementsByLink(IHTMLElement *pElem, const wchar_t *input_string)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTSBYLINK)

	if(1 == data.output_long_) {std::wstring Err(L"Cannot find elements by Link"); throw Err;}

	std::vector<ElementWrapper*> *toReturn = new std::vector<ElementWrapper*>();

	std::vector<IHTMLElement*>& allElems = data.output_list_html_element_;
	std::vector<IHTMLElement*>::const_iterator cur, end = allElems.end();
	for(cur = allElems.begin();cur < end; cur++)
	{
		IHTMLElement* elem = *cur;
		toReturn->push_back(new ElementWrapper(this, elem));
	}
	return toReturn;
}

int InternetExplorerDriver::selectElementByName(IHTMLElement *pElem, const wchar_t *input_string, ElementWrapper** element) 
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTBYNAME)

	if (data.error_code == SUCCESS) { 
		*element = new ElementWrapper(this, data.output_html_element_);
	}

	return data.error_code;
}

std::vector<ElementWrapper*>* InternetExplorerDriver::selectElementsByName(IHTMLElement *pElem, const wchar_t *input_string)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTSBYNAME)

	if(1 == data.output_long_) {std::wstring Err(L"Cannot find elements by Name"); throw Err;}

	std::vector<ElementWrapper*> *toReturn = new std::vector<ElementWrapper*>();

	std::vector<IHTMLElement*>& allElems = data.output_list_html_element_;
	std::vector<IHTMLElement*>::const_iterator cur, end = allElems.end();
	for(cur = allElems.begin();cur < end; cur++)
	{
		IHTMLElement* elem = *cur;
		toReturn->push_back(new ElementWrapper(this, elem));
	}
	return toReturn;
}

int InternetExplorerDriver::selectElementByTagName(IHTMLElement *pElem, const wchar_t *input_string, ElementWrapper** element) 
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTBYTAGNAME)

	if (data.error_code == SUCCESS) { 
		*element = new ElementWrapper(this, data.output_html_element_);
	}

	return data.error_code;
}

std::vector<ElementWrapper*>* InternetExplorerDriver::selectElementsByTagName(IHTMLElement *pElem, const wchar_t *input_string)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTSBYTAGNAME)

	if(1 == data.output_long_) {std::wstring Err(L"Cannot find elements by tag name"); throw Err;}

	std::vector<ElementWrapper*> *toReturn = new std::vector<ElementWrapper*>();

	std::vector<IHTMLElement*>& allElems = data.output_list_html_element_;
	std::vector<IHTMLElement*>::const_iterator cur, end = allElems.end();
	for(cur = allElems.begin();cur < end; cur++)
	{
		IHTMLElement* elem = *cur;
		toReturn->push_back(new ElementWrapper(this, elem));
	}
	return toReturn;
}

int InternetExplorerDriver::selectElementByClassName(IHTMLElement *pElem, const wchar_t *input_string, ElementWrapper** element) 
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTBYCLASSNAME)

	if (data.error_code == SUCCESS) { 
		*element = new ElementWrapper(this, data.output_html_element_);
	}

	return data.error_code;
}

std::vector<ElementWrapper*>* InternetExplorerDriver::selectElementsByClassName(IHTMLElement *pElem, const wchar_t *input_string)
{
	SCOPETRACER
	SEND_MESSAGE_ABOUT_ELEM(_WD_SELELEMENTSBYCLASSNAME)

	if(1 == data.output_long_) {std::wstring Err(L"Cannot find elements by ClassName"); throw Err;}

	std::vector<ElementWrapper*> *toReturn = new std::vector<ElementWrapper*>();

	std::vector<IHTMLElement*>& allElems = data.output_list_html_element_;
	std::vector<IHTMLElement*>::const_iterator cur, end = allElems.end();
	for(cur = allElems.begin();cur < end; cur++)
	{
		IHTMLElement* elem = *cur;
		toReturn->push_back(new ElementWrapper(this, elem));
	}
	return toReturn;
}

void InternetExplorerDriver::waitForNavigateToFinish() 
{
	SCOPETRACER
	DataMarshaller& data = prepareCmData();
	p_IEthread->m_EventToNotifyWhenNavigationCompleted = data.synchronization_flag_;
	sendThreadMsg(_WD_WAITFORNAVIGATIONTOFINISH, data);
}

bool InternetExplorerDriver::switchToFrame(LPCWSTR pathToFrame) 
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_SWITCHTOFRAME, pathToFrame)
	return data.output_bool_;
}

LPCWSTR InternetExplorerDriver::getCookies()
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_GETCOOKIES,)
	return data.output_string_.c_str();
}

int InternetExplorerDriver::addCookie(const wchar_t *cookieString)
{
	SCOPETRACER
	SEND_MESSAGE_WITH_MARSHALLED_DATA(_WD_ADDCOOKIE, cookieString)

	return data.error_code;
}



CComVariant& InternetExplorerDriver::executeScript(const wchar_t *script, SAFEARRAY* args, bool tryAgain)
{
	SCOPETRACER
	DataMarshaller& data = prepareCmData(script);
	data.input_safe_array_ = args;
	sendThreadMsg(_WD_EXECUTESCRIPT, data);

	return data.output_variant_;
}

void InternetExplorerDriver::setSpeed(int speed)
{
	this->speed = speed;
}

int InternetExplorerDriver::getSpeed()
{
	return speed;
}


/////////////////////////////////////////////////////////////

bool InternetExplorerDriver::sendThreadMsg(UINT msg, DataMarshaller& data)
{
	ResetEvent(data.synchronization_flag_);
	// NOTE(alexis.j.vuillemin): do not do here data.resetOutputs()
	//   it has to be performed FROM the worker thread (see ON_THREAD_COMMON).
	p_IEthread->PostThreadMessageW(msg, 0, 0);
	DWORD res = WaitForSingleObject(data.synchronization_flag_, 120000);
	data.resetInputs();
	if(WAIT_TIMEOUT == res)
	{
		safeIO::CoutA("Unexpected TIME OUT.");
		p_IEthread->m_EventToNotifyWhenNavigationCompleted = NULL;
		std::wstring Err(L"Error: had to TIME OUT as a request to the worker thread did not complete after 2 min.");
		if(p_IEthread->m_HeartBeatListener != NULL)
		{
			PostMessage(p_IEthread->m_HeartBeatListener, _WD_HB_CRASHED, 0 ,0 );
		}
		throw Err;
	}
	if(data.exception_caught_)
	{
		safeIO::CoutA("Caught exception from worker thread.");
		p_IEthread->m_EventToNotifyWhenNavigationCompleted = NULL;
		std::wstring Err(data.output_string_);
		throw Err;
	}
	return true;
}

inline DataMarshaller& InternetExplorerDriver::prepareCmData()
{
	return commandData();
}

DataMarshaller& InternetExplorerDriver::prepareCmData(LPCWSTR str)
{
	DataMarshaller& data = prepareCmData();
	data.input_string_ = str;
	return data;
}

DataMarshaller& InternetExplorerDriver::prepareCmData(IHTMLElement *pElem, LPCWSTR str)
{
	DataMarshaller& data = prepareCmData(str);
	data.input_html_element_ = pElem;
	return data;
}

DataMarshaller& InternetExplorerDriver::prepareCmData(int v)
{
	DataMarshaller& data = prepareCmData();
	data.input_long_ = (long) v;
	return data;
}

